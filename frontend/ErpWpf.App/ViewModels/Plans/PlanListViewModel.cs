using System.Collections.ObjectModel;
using CommunityToolkit.Mvvm.ComponentModel;
using CommunityToolkit.Mvvm.Input;
using ErpWpf.App.Models;

namespace ErpWpf.App.ViewModels.Plans;

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
        UpdatedAt = d.updatedAt
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

    public ObservableCollection<PlanRow> Items { get; } = new();

    [ObservableProperty] private int _sinceHours = 24;
    [ObservableProperty] private string _itemFilter = "";
    [ObservableProperty] private bool _includeDeleted;
    [ObservableProperty] private string _lastSyncUtc = "";
    [ObservableProperty] private bool _isBusy;

    public PlanListViewModel(ErpApi api, TimeUtil time)
    {
        _api = api;
        _time = time;
        LastSyncUtc = _time.ToUtcIso(DateTimeOffset.UtcNow.AddHours(-SinceHours));
    }

    [RelayCommand]
    public async Task LoadAsync()
    {
        if (IsBusy) return;
        try
        {
            IsBusy = true;
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

                    Items.Add(PlanRow.From(d));
                    if (!string.IsNullOrWhiteSpace(d.updatedAt)) maxUpdated = d.updatedAt!;
                }

                if (last || chunk.Count < size) break;
                page++;
            }

            LastSyncUtc = maxUpdated;
        }
        finally { IsBusy = false; }
    }

    [RelayCommand]
    public void SetSinceHours(int hours)
    {
        SinceHours = hours;
        LastSyncUtc = _time.ToUtcIso(DateTimeOffset.UtcNow.AddHours(-SinceHours));
    }
}