// ViewModels/CostViewModel.cs (MVVM: 실시간 검증 + 프리뷰/저장 + Busy + 결과 바인딩)
using System;
using System.Collections.ObjectModel;
using System.ComponentModel;
using System.Runtime.CompilerServices;
using System.Threading.Tasks;
using System.Windows;
using System.Windows.Input;
using Erp.Client.Wpf.Models;
using Erp.Client.Wpf.Services;

namespace Erp.Client.Wpf.ViewModels
{
    public class CostViewModel : INotifyPropertyChanged, IDataErrorInfo
    {
        private readonly ApiClient _api;
        public CostViewModel(ApiClient api) { _api = api; BaseDate = DateTime.Today; }

        // By Product 입력
        private string? _productId;
        public string? ProductId { get => _productId; set { _productId = value; OnChanged(); CommandManager.InvalidateRequerySuggested(); } }

        private string _qty = "1.000000";
        public string Qty { get => _qty; set { _qty = value; OnChanged(); CommandManager.InvalidateRequerySuggested(); } }

        private string _labor = "0.100000";
        public string Labor { get => _labor; set { _labor = value; OnChanged(); CommandManager.InvalidateRequerySuggested(); } }

        private string _overhead = "0.100000";
        public string Overhead { get => _overhead; set { _overhead = value; OnChanged(); CommandManager.InvalidateRequerySuggested(); } }

        private DateTime _baseDate;
        public DateTime BaseDate { get => _baseDate; set { _baseDate = value; OnChanged(); } }

        // By Plan 입력
        private string? _planId;
        public string? PlanId { get => _planId; set { _planId = value; OnChanged(); CommandManager.InvalidateRequerySuggested(); } }

        private string _planLabor = "0.100000";
        public string PlanLabor { get => _planLabor; set { _planLabor = value; OnChanged(); CommandManager.InvalidateRequerySuggested(); } }

        private string _planOverhead = "0.100000";
        public string PlanOverhead { get => _planOverhead; set { _planOverhead = value; OnChanged(); CommandManager.InvalidateRequerySuggested(); } }

        // Busy
        private bool _isBusy;
        public bool IsBusy { get => _isBusy; set { _isBusy = value; OnChanged(); CommandManager.InvalidateRequerySuggested(); } }

        // 결과
        public ObservableCollection<CostSnapshotDetailResponse> ResultItems { get; } = new();

        private CostSnapshotResponse? _lastResponse;
        public CostSnapshotResponse? LastResponse { get => _lastResponse; set { _lastResponse = value; OnChanged(); } }

        // Commands
        public ICommand CalcByProductCommand => new RelayCommand(async _ => await CalcByProductAsync(), _ => CanCalcByProduct());
        public ICommand SaveByProductCommand => new RelayCommand(async _ => await SaveByProductAsync(), _ => CanCalcByProduct());
        public ICommand CalcByPlanCommand => new RelayCommand(async _ => await CalcByPlanAsync(), _ => CanCalcByPlan());
        public ICommand SaveByPlanCommand => new RelayCommand(async _ => await SaveByPlanAsync(), _ => CanSaveByPlan());

        private bool CanCalcByProduct()
        {
            return !IsBusy
                   && string.IsNullOrWhiteSpace(this[nameof(ProductId)])
                   && string.IsNullOrWhiteSpace(this[nameof(Qty)])
                   && string.IsNullOrWhiteSpace(this[nameof(Labor)])
                   && string.IsNullOrWhiteSpace(this[nameof(Overhead)]);
        }

        private bool CanCalcByPlan()
        {
            return !IsBusy
                   && string.IsNullOrWhiteSpace(this[nameof(PlanId)])
                   && string.IsNullOrWhiteSpace(this[nameof(PlanLabor)])
                   && string.IsNullOrWhiteSpace(this[nameof(PlanOverhead)]);
        }

        private bool CanSaveByPlan() => CanCalcByPlan();

        private async Task CalcByProductAsync()
        {
            if (!decimal.TryParse(Qty, out var qty) || qty <= 0) return;
            if (!decimal.TryParse(Labor, out var labor) || labor < 0) labor = 0.100000m;
            if (!decimal.TryParse(Overhead, out var oh) || oh < 0) oh = 0.100000m;

            var baseDate = BaseDate.ToString("yyyy-MM-dd");
            var path = Endpoints.CostByProduct(ProductId!.Trim(),
                                               qty.ToString("0.######"),
                                               labor.ToString("0.######"),
                                               oh.ToString("0.######"),
                                               baseDate);
            IsBusy = true;
            try
            {
                var res = await _api.GetAsync<CostSnapshotResponse>(path);
                LastResponse = res;
                ResultItems.Clear();
                if (res?.Details != null)
                    foreach (var d in res.Details) ResultItems.Add(d);
            }
            catch (Exception ex)
            {
                MessageBox.Show($"by-product 미리보기 실패: {ex.Message}");
            }
            finally { IsBusy = false; }
        }

        private async Task SaveByProductAsync()
        {
            if (!decimal.TryParse(Qty, out var qty) || qty <= 0) return;
            if (!decimal.TryParse(Labor, out var labor) || labor < 0) labor = 0.100000m;
            if (!decimal.TryParse(Overhead, out var oh) || oh < 0) oh = 0.100000m;

            DateOnly? baseDate = DateOnly.FromDateTime(BaseDate);
            var body = new SaveCostByProductRequest(ProductId!.Trim(), qty, labor, oh, baseDate, "WPF 저장");

            IsBusy = true;
            try
            {
                var res = await _api.PostAsync<CostSnapshotResponse>(Endpoints.SaveCostByProduct, body);
                LastResponse = res;
                MessageBox.Show(res?.SnapshotId is not null ? $"저장 성공: {res!.SnapshotId}" : "저장 성공");
            }
            catch (Exception ex)
            {
                MessageBox.Show($"by-product 저장 실패: {ex.Message}");
            }
            finally { IsBusy = false; }
        }

        private async Task CalcByPlanAsync()
        {
            if (!decimal.TryParse(PlanLabor, out var labor) || labor < 0) labor = 0.100000m;
            if (!decimal.TryParse(PlanOverhead, out var oh) || oh < 0) oh = 0.100000m;

            var path = Endpoints.CostByPlan(PlanId!.Trim(),
                                            labor.ToString("0.######"),
                                            oh.ToString("0.######"));
            IsBusy = true;
            try
            {
                var res = await _api.GetAsync<CostSnapshotResponse>(path);
                LastResponse = res;
                ResultItems.Clear();
                if (res?.Details != null)
                    foreach (var d in res.Details) ResultItems.Add(d);
            }
            catch (Exception ex)
            {
                MessageBox.Show($"by-plan 미리보기 실패: {ex.Message}");
            }
            finally { IsBusy = false; }
        }

        private async Task SaveByPlanAsync()
        {
            if (!decimal.TryParse(PlanLabor, out var labor) || labor < 0) labor = 0.100000m;
            if (!decimal.TryParse(PlanOverhead, out var oh) || oh < 0) oh = 0.100000m;

            var body = new SaveCostByPlanRequest(PlanId!.Trim(), labor, oh, "WPF 저장");

            IsBusy = true;
            try
            {
                var res = await _api.PostAsync<CostSnapshotResponse>(Endpoints.SaveCostByPlan, body);
                LastResponse = res;
                MessageBox.Show(res?.SnapshotId is not null ? $"저장 성공: {res!.SnapshotId}" : "저장 성공");
            }
            catch (Exception ex)
            {
                MessageBox.Show($"by-plan 저장 실패: {ex.Message}");
            }
            finally { IsBusy = false; }
        }

        // IDataErrorInfo
        public string Error => string.Empty;
        public string this[string columnName]
        {
            get
            {
                switch (columnName)
                {
                    case nameof(ProductId): return string.IsNullOrWhiteSpace(ProductId) ? "필수" : string.Empty;
                    case nameof(Qty): return !decimal.TryParse(Qty, out var q) || q <= 0 ? "양수 필요" : string.Empty;
                    case nameof(Labor): return !decimal.TryParse(Labor, out var l) || l < 0 ? "0 이상" : string.Empty;
                    case nameof(Overhead): return !decimal.TryParse(Overhead, out var o) || o < 0 ? "0 이상" : string.Empty;
                    case nameof(PlanId): return string.IsNullOrWhiteSpace(PlanId) ? "필수" : string.Empty;
                    case nameof(PlanLabor): return !decimal.TryParse(PlanLabor, out var pl) || pl < 0 ? "0 이상" : string.Empty;
                    case nameof(PlanOverhead): return !decimal.TryParse(PlanOverhead, out var po) || po < 0 ? "0 이상" : string.Empty;
                }
                return string.Empty;
            }
        }

        public event PropertyChangedEventHandler? PropertyChanged;
        private void OnChanged([CallerMemberName] string? p = null) => PropertyChanged?.Invoke(this, new PropertyChangedEventArgs(p));
    }
}