// 9) 목록 ViewModel — 기존 코드와 호환 확장판 (P/R/C 상태·배지, MES 배치조회, TTL 캐시)
// 파일 위치 유지 권장: ErpWpf.App/ViewModels/Plans/PlanListViewModel.cs

using System;
using System.Collections.Generic;
using System.Collections.ObjectModel;
using System.Linq;
using System.Threading.Tasks;
using CommunityToolkit.Mvvm.ComponentModel;
using CommunityToolkit.Mvvm.Input;
using ErpWpf.App.Domain.Plans;
using ErpWpf.App.Models; // PlanDto, ErpApi, TimeUtil 등 기존 참조

namespace ErpWpf.App.ViewModels.Plans
{

    public sealed record PlanWoStatuses(string PlanId, IReadOnlyList<string> WoStatuses);

    public interface IMesWoStatusClient
    {
        Task<IReadOnlyList<PlanWoStatuses>> GetStatusesAsync(IEnumerable<string> planIds, System.Threading.CancellationToken ct);
    }

    public static class StatusRules
    {
        public static PlanStatus DeriveMain(IReadOnlyList<string>? woStatuses)
        {
            if (woStatuses is null || woStatuses.Count == 0) return PlanStatus.P;

            bool anyRunning = woStatuses.Any(s =>
                s.Equals("RUNNING", StringComparison.OrdinalIgnoreCase) ||
                s.Equals("RELEASED", StringComparison.OrdinalIgnoreCase) ||
                s.Equals("EXECUTING", StringComparison.OrdinalIgnoreCase));

            bool allCompleted = woStatuses.All(s =>
                s.Equals("COMPLETED", StringComparison.OrdinalIgnoreCase) ||
                s.Equals("CLOSED", StringComparison.OrdinalIgnoreCase));

            if (allCompleted) return PlanStatus.C;
            if (anyRunning) return PlanStatus.R;
            return PlanStatus.P;
        }
    }

    // 기존 PlanRow 확장: 상태/배지만 추가(기존 필드는 그대로 유지)
    public partial class PlanRow : ObservableObject
    {
        [ObservableProperty] private string _planId = "";
        [ObservableProperty] private int _planLineNo;
        [ObservableProperty] private string _itemId = "";
        [ObservableProperty] private decimal _qty;
        [ObservableProperty] private string _unit = "EA";
        [ObservableProperty] private string _dueDateUtc = "";
        [ObservableProperty] private int? _priority;
        [ObservableProperty] private bool _isDeleted;
        [ObservableProperty] private string? _updatedAt;

        // 신규: 메인 상태/워크플로 배지(표시용)
        [ObservableProperty] private PlanStatus _status = PlanStatus.P;
        [ObservableProperty] private WorkflowBadge _workflow = WorkflowBadge.None;

        // 편의 속성(취소/보류 배지 표시 등)
        public bool IsCanceled => (Workflow & WorkflowBadge.Canceled) != 0;
        public bool IsOnHold => (Workflow & WorkflowBadge.OnHold) != 0;

        public static PlanRow From(PlanDto d) => new()
        {
            PlanId = d.planId,
            PlanLineNo = d.planLineNo,
            ItemId = d.itemId,
            Qty = d.qty,
            Unit = d.unit,
            DueDateUtc = d.dueDateUtc,
            Priority = d.priority,
            IsDeleted = d.isDeleted,
            UpdatedAt = d.updatedAt,

            // 기본 파생값(상세/배치 조회 후 갱신됨)
            Status = PlanStatus.P,
            Workflow = d.isDeleted ? WorkflowBadge.Canceled : WorkflowBadge.None
        };

        public PlanDto ToDto() => new()
        {
            planId = PlanId,
            planLineNo = PlanLineNo,
            itemId = ItemId,
            qty = Qty,
            unit = Unit,
            dueDateUtc = DueDateUtc,
            priority = Priority,
            isDeleted = IsDeleted
        };
    }

    public partial class PlanListViewModel : ObservableObject
    {
        private readonly ErpApi _api;
        private readonly TimeUtil _time;

        // [선택] MES WO 상태 배치 조회 클라이언트(주입 안 되면 상태 파생 스킵)
        private readonly IMesWoStatusClient? _wo;

        // TTL 캐시(페이지 단위 상태 재사용)
        private readonly Dictionary<string, (IReadOnlyList<string> States, DateTimeOffset ExpireAt)> _statusCache = new();
        private readonly TimeSpan _statusTtl = TimeSpan.FromSeconds(60);

        public ObservableCollection<PlanRow> Items { get; } = new();

        [ObservableProperty] private int _sinceHours = 24;
        [ObservableProperty] private string _itemFilter = "";
        [ObservableProperty] private bool _includeDeleted;
        [ObservableProperty] private string _lastSyncUtc = "";
        [ObservableProperty] private bool _isBusy;

        // [선택] 상태 필터(P/R/C) — UI에서 사용 시 바인딩
        [ObservableProperty] private string? _statusFilter;

        public PlanListViewModel(ErpApi api, TimeUtil time, IMesWoStatusClient? wo = null)
        {
            _api = api;
            _time = time;
            _wo = wo;
            LastSyncUtc = _time.ToUtcIso(DateTimeOffset.UtcNow.AddHours(-SinceHours));
        }

        [RelayCommand]
        public async Task LoadAsync()
        {
            if (IsBusy) return;
            IsBusy = true;
            try
            {
                Items.Clear();

                var since = _time.ToUtcIso(DateTimeOffset.UtcNow.AddHours(-SinceHours));
                int page = 0, size = 100;
                string maxUpdated = since;

                while (true)
                {
                    var pageRes = await _api.GetPlansPageAsync(since, page, size);
                    var chunk = pageRes.items;
                    var last = pageRes.last;

                    if (chunk.Count == 0) break;

                    foreach (var d in chunk)
                    {
                        if (!IncludeDeleted && d.isDeleted) continue;
                        if (!string.IsNullOrWhiteSpace(ItemFilter) &&
                            !d.itemId.Contains(ItemFilter, StringComparison.OrdinalIgnoreCase)) continue;

                        var row = PlanRow.From(d);
                        Items.Add(row);

                        if (!string.IsNullOrWhiteSpace(d.updatedAt)) maxUpdated = d.updatedAt!;
                    }

                    if (last || chunk.Count < size) break;
                    page++;
                }

                // 상태 파생(현재 페이지 planId 묶음만 MES 배치 조회)
                await DeriveStatusesForVisibleAsync();

                // [선택] 상태 필터가 설정된 경우, 화면상 필터링 필요 시 여기서 적용(또는 View에서 CollectionView 필터 사용)
                if (!string.IsNullOrWhiteSpace(StatusFilter))
                {
                    var want = StatusFilter!.Trim().ToUpperInvariant();
                    var filtered = Items.Where(x => x.Status.ToString().Equals(want, StringComparison.OrdinalIgnoreCase)).ToList();
                    if (filtered.Count != Items.Count)
                    {
                        Items.Clear();
                        foreach (var r in filtered) Items.Add(r);
                    }
                }

                LastSyncUtc = maxUpdated;
            }
            finally
            {
                IsBusy = false;
            }
        }

        [RelayCommand]
        public void SetSinceHours(int hours)
        {
            SinceHours = hours;
            LastSyncUtc = _time.ToUtcIso(DateTimeOffset.UtcNow.AddHours(-SinceHours));
        }

        private async Task DeriveStatusesForVisibleAsync()
        {
            if (_wo is null || Items.Count == 0) return;

            var now = DateTimeOffset.UtcNow;
            var planIds = Items.Select(x => x.PlanId).Distinct().ToArray();

            // 캐시 미스만 모아서 배치 조회
            var miss = new List<string>(capacity: planIds.Length);
            foreach (var id in planIds)
            {
                if (!_statusCache.TryGetValue(id, out var entry) || entry.ExpireAt <= now)
                    miss.Add(id);
            }

            if (miss.Count > 0)
            {
                using var cts = new System.Threading.CancellationTokenSource(TimeSpan.FromSeconds(5));
                var list = await _wo.GetStatusesAsync(miss, cts.Token);
                foreach (var s in list)
                {
                    _statusCache[s.PlanId] = (s.WoStatuses, now + _statusTtl);
                }
            }

            // 파생 적용
            foreach (var r in Items)
            {
                IReadOnlyList<string>? states = null;
                if (_statusCache.TryGetValue(r.PlanId, out var entry) && entry.ExpireAt > now)
                    states = entry.States;

                r.Status = StatusRules.DeriveMain(states);
                // 워크플로 배지는 현재 소프트삭제만 반영(추후 승인/배포 등 원천 상태 연동 시 갱신)
                r.Workflow = r.IsDeleted ? (r.Workflow | WorkflowBadge.Canceled) : r.Workflow & ~WorkflowBadge.Canceled;
            }
        }
    }
}