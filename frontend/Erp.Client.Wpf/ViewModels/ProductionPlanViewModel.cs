// ViewModels/ProductionPlanViewModel.cs
using Erp.Client.Wpf.Models;
using Erp.Client.Wpf.Services;
using Erp.Client.Wpf.Views;
using System.Windows;
using System.Collections.ObjectModel;
using System.ComponentModel;
using System.Runtime.CompilerServices;
using System.Windows.Input;

namespace Erp.Client.Wpf.ViewModels
{
    public class ProductionPlanViewModel : INotifyPropertyChanged
    {
        private readonly ApiClient _api;
        public event PropertyChangedEventHandler? PropertyChanged;
        private void OnChanged([CallerMemberName] string? n = null) => PropertyChanged?.Invoke(this, new PropertyChangedEventArgs(n));
        private void Requery() => CommandManager.InvalidateRequerySuggested();

        public ProductionPlanViewModel(ApiClient api)
        {
            _api = api;
            StatusOptions = new ObservableCollection<StatusOption>
            {
                new("전체","ALL"), new("임시","DRAFT"), new("확정","CONFIRMED"), new("대기","PENDING"), new("진행중","IN_PRODUCTION"), new("완료","COMPLETED"), new("취소","CANCELED")
            };
            _selectedStatus = StatusOptions[0];
            FromDate = DateTime.Today.AddDays(-7);
            ToDate = DateTime.Today.AddDays(30);

            SearchCommand = new RelayCommand(async _ => { Page = 0; await LoadAsync(); }, _ => !IsBusy);
            PrevPageCommand = new RelayCommand(async _ => { if (CanPrev) { Page--; await LoadAsync(); } }, _ => CanPrev && !IsBusy);
            NextPageCommand = new RelayCommand(async _ => { if (CanNext) { Page++; await LoadAsync(); } }, _ => CanNext && !IsBusy);

            NewCommand = new RelayCommand(_ => New(), _ => !IsBusy);
            CreateCommand = new RelayCommand(async _ => await CreateAsync(), _ => CanCreate);
            UpdateCommand = new RelayCommand(async _ => await UpdateAsync(), _ => CanUpdate);
            ConfirmCommand = new RelayCommand(async _ => await ChangeStatusAsync("CONFIRMED"), _ => CanConfirm);
            CancelCommand = new RelayCommand(async _ => await ChangeStatusAsync("CANCELED"), _ => CanCancel);
            DeleteCommand = new RelayCommand(async _ => await DeleteAsync(), _ => CanDelete);

            OpenProductPickerCommand = new RelayCommand(_ => OpenProductPicker(), _ => !IsBusy);

            _ = LoadAsync();
        }
        private void RaiseGuards()
        {
            OnChanged(nameof(CanCreate));
            OnChanged(nameof(CanUpdate));
            OnChanged(nameof(CanConfirm));
            OnChanged(nameof(CanCancel));
            OnChanged(nameof(CanDelete));
            System.Windows.Input.CommandManager.InvalidateRequerySuggested();
        }
        private bool _isBusy;
        public bool IsBusy
        {
            get => _isBusy;
            set
            {
                if (_isBusy == value) return;
                _isBusy = value;
                OnChanged();
                RaiseGuards(); // CanCreate 등 재계산 + Command 재평가
            }
        }

        // 검색/필터
        private string? _keyword;
        public string? Keyword { get => _keyword; set { _keyword = value; OnChanged(); } }

        public ObservableCollection<StatusOption> StatusOptions { get; }
        private StatusOption _selectedStatus;
        public StatusOption SelectedStatus { get => _selectedStatus; set { _selectedStatus = value; OnChanged(); _ = LoadAsync(); } }

        private DateTime? _fromDate;
        public DateTime? FromDate { get => _fromDate; set { _fromDate = value; OnChanged(); } }

        private DateTime? _toDate;
        public DateTime? ToDate { get => _toDate; set { _toDate = value; OnChanged(); } }

        // 페이지
        private int _page;
        public int Page { get => _page; set { _page = value; OnChanged(); UpdatePageInfo(); } }
        public int Size { get; set; } = 20;
        private int _totalPages;
        public int TotalPages { get => _totalPages; set { _totalPages = value; OnChanged(); UpdatePageInfo(); } }
        private long _totalElements;
        public long TotalElements { get => _totalElements; set { _totalElements = value; OnChanged(); UpdatePageInfo(); } }
        public bool CanPrev => Page > 0;
        public bool CanNext => Page + 1 < TotalPages;
        private string _pageInfo = "";
        public string PageInfo { get => _pageInfo; private set { _pageInfo = value; OnChanged(); } }
        private void UpdatePageInfo() => PageInfo = $"페이지 {Page + 1} / {Math.Max(TotalPages, 1)} • 총 {TotalElements}건";

        // 목록/선택/상세
        public ObservableCollection<ProductionPlanSummaryDto> Plans { get; } = new();
        private ProductionPlanSummaryDto? _selectedPlan;
        public ProductionPlanSummaryDto? SelectedPlan
        {
            get => _selectedPlan;
            set
            {
                if (_selectedPlan == value) return;
                _selectedPlan = value;
                OnChanged();
                _ = LoadDetailAsync();
                RaiseGuards();
            }
        }

        private ProductionPlanDetailDto _detail = new()
        {
            startDate = DateTime.Today,
            endDate = DateTime.Today.AddDays(7),
            qty = 1,
            status = "DRAFT",
            version = 0
        };
        public ProductionPlanDetailDto Detail
        {
            get => _detail;
            set
            {
                if (ReferenceEquals(_detail, value)) { _detail = value; OnChanged(); RaiseGuards(); return; }
                _detail = value;
                OnChanged();
                RaiseGuards();
            }
        }

        // 명령
        public ICommand SearchCommand { get; }
        public ICommand PrevPageCommand { get; }
        public ICommand NextPageCommand { get; }
        public ICommand NewCommand { get; }
        public ICommand CreateCommand { get; }
        public ICommand UpdateCommand { get; }
        public ICommand ConfirmCommand { get; }
        public ICommand CancelCommand { get; }
        public ICommand DeleteCommand { get; }
        public ICommand OpenProductPickerCommand { get; }

        public bool CanCreate => !IsBusy && !string.IsNullOrWhiteSpace(Detail.planCode?.Trim()) && !string.IsNullOrWhiteSpace(Detail.productId?.Trim()) && Detail.qty > 0 && Detail.startDate != default && Detail.endDate != default;
        public bool CanUpdate => !IsBusy && !string.IsNullOrWhiteSpace(Detail.planId) && Detail.version >= 0;
        public bool CanConfirm => !IsBusy && SelectedPlan != null && SelectedPlan.status == "DRAFT";
        public bool CanCancel => !IsBusy && SelectedPlan != null && SelectedPlan.status != "CANCELED" && SelectedPlan.status != "COMPLETED" && SelectedPlan.status != "IN_PRODUCTION" && SelectedPlan.status != "PENDING";
        public bool CanDelete => !IsBusy && SelectedPlan != null && SelectedPlan.status != "COMPLETED" && SelectedPlan.status != "IN_PRODUCTION" && SelectedPlan.status != "PENDING";

        private async Task LoadAsync()
        {
            IsBusy = true;
            try
            {
                var q = Uri.EscapeDataString(Keyword ?? "");
                var status = SelectedStatus?.Value ?? "ALL";
                var from = FromDate?.ToString("yyyy-MM-dd") ?? "";
                var to = ToDate?.ToString("yyyy-MM-dd") ?? "";
                var url = $"/api/plans?q={q}&status={status}&from={from}&to={to}&page={Page}&size={Size}&sort=createdAt,desc";

                var res = await _api.GetAsync<Paged<ProductionPlanSummaryDto>>(url);
                Plans.Clear();
                foreach (var row in res?.content ?? new()) Plans.Add(row);
                TotalPages = res?.totalPages ?? 0;
                TotalElements = res?.totalElements ?? 0;
            }
            finally { IsBusy = false; }
        }

        private async Task LoadDetailAsync()
        {
            if (SelectedPlan == null) { Detail = new ProductionPlanDetailDto(); return; }
            IsBusy = true;
            try
            {
                var res = await _api.GetAsync<ProductionPlanDetailDto>($"/api/plans/{SelectedPlan.planId}");
                Detail = res ?? new ProductionPlanDetailDto();
            }
            finally { IsBusy = false; }
        }

        private void New()
        {
            Detail = new ProductionPlanDetailDto
            {
                planId = "",
                planCode = "",
                productId = "",
                qty = 1,
                startDate = DateTime.Today,
                endDate = DateTime.Today.AddDays(7),
                status = "DRAFT",
                version = 0
            };
        }

        private async Task CreateAsync()
        {
            if (!CanCreate) return;
            IsBusy = true;
            try
            {
                var req = new CreateProductionPlanRequest
                {
                    planCode = Detail.planCode,
                    productId = Detail.productId,
                    qty = Detail.qty,
                    startDate = Detail.startDate.ToString("yyyy-MM-dd"),
                    endDate = Detail.endDate.ToString("yyyy-MM-dd"),
                    status = Detail.status,
                    note = Detail.note
                };
                var created = await _api.PostAsync<ProductionPlanDetailDto>("/api/plans", req);
                Detail = created ?? Detail;
                await LoadAsync();
            }
            finally { IsBusy = false; }
        }

        private async Task UpdateAsync()
        {
            if (!CanUpdate) return;
            IsBusy = true;
            try
            {
                var req = new UpdateProductionPlanRequest
                {
                    qty = Detail.qty,
                    startDate = Detail.startDate.ToString("yyyy-MM-dd"),
                    endDate = Detail.endDate.ToString("yyyy-MM-dd"),
                    note = Detail.note,
                    version = Detail.version
                };
                var updated = await _api.PutAsync<ProductionPlanDetailDto>($"/api/plans/{Detail.planId}", req);
                Detail = updated ?? Detail;
                await LoadAsync();
            }
            finally { IsBusy = false; }
        }

        private async Task ChangeStatusAsync(string nextStatus)
        {
            if (SelectedPlan == null) return;
            IsBusy = true;
            try
            {
                var req = new ChangeStatusRequest
                {
                    nextStatus = nextStatus,
                    actor = "system",
                    version = Detail.version
                };
                var updated = await _api.PatchAsync<ProductionPlanDetailDto>($"/api/plans/{SelectedPlan.planId}/status", req);
                Detail = updated ?? Detail;
                await LoadAsync();
            }
            finally { IsBusy = false; }
        }

        private async Task DeleteAsync()
        {
            if (SelectedPlan == null) return;
            IsBusy = true;
            try
            {
                await _api.DeleteAsync($"/api/plans/{SelectedPlan.planId}");
                await LoadAsync();
                Detail = new ProductionPlanDetailDto();
            }
            finally { IsBusy = false; }
        }

        private void OpenProductPicker()
        {
            var dlg = new ProductSingleSelectDialog(_api)
            {
                Owner = Application.Current?.MainWindow
            };
            var ok = dlg.ShowDialog();
            if (ok != true || dlg.SelectedItem == null) return;

            Detail.productId = dlg.SelectedItem.productId;
            Detail.productName = dlg.SelectedItem.name;
            Detail.productCode = dlg.SelectedItem.productCode;

            CommandManager.InvalidateRequerySuggested();
            OnChanged(nameof(Detail));
            // RaiseGuards(); // 필요하면
        }

        public record StatusOption(string Label, string Value);
    }
}