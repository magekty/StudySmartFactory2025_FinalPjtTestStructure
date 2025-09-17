// 1) ViewModels/BomViewModel.cs - Lazy 로딩 500 대응: 조회/추가/삭제 + Commands (그대로 사용)
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
<<<<<<< HEAD
    public partial class BomViewModel : INotifyPropertyChanged, IDataErrorInfo
=======
    public class BomViewModel : INotifyPropertyChanged
>>>>>>> parent of 89c1deb (250917_Bom_layout_change)
    {
        private readonly ApiClient _api;

        private string? _queryBomId;
        public string? QueryBomId { get => _queryBomId; set { _queryBomId = value; OnChanged(); CommandManager.InvalidateRequerySuggested(); } }

        public ObservableCollection<BomTreeNodeResponse> Tree { get; } = new();
        public ObservableCollection<BomLineResponse> Lines { get; } = new();

        private string? _addBomId;
        public string? AddBomId { get => _addBomId; set { _addBomId = value; OnChanged(); CommandManager.InvalidateRequerySuggested(); } }

        private string? _addParentLineId;
        public string? AddParentLineId { get => _addParentLineId; set { _addParentLineId = value; OnChanged(); } }

        private string? _addComponentProductId;
        public string? AddComponentProductId { get => _addComponentProductId; set { _addComponentProductId = value; OnChanged(); CommandManager.InvalidateRequerySuggested(); } }

        private string _addQty = "1.000000";
        public string AddQty { get => _addQty; set { _addQty = value; OnChanged(); CommandManager.InvalidateRequerySuggested(); } }

        private string _addScrapRate = "0.000000";
        public string AddScrapRate { get => _addScrapRate; set { _addScrapRate = value; OnChanged(); CommandManager.InvalidateRequerySuggested(); } }

        private string? _addNote;
        public string? AddNote { get => _addNote; set { _addNote = value; OnChanged(); } }

        public ICommand QueryCommand { get; }
        public ICommand CopyQueryToAddCommand { get; }
        public ICommand AddLineCommand { get; }
        public ICommand AddChildCommand { get; }
        public ICommand DeleteNodeCommand { get; }
        public ICommand DeleteLineCommand { get; }

        public BomViewModel(ApiClient api)
        {
            _api = api;

            QueryCommand = new RelayCommand(async _ => await QueryAsync(), _ => !string.IsNullOrWhiteSpace(QueryBomId));
            CopyQueryToAddCommand = new RelayCommand(_ => AddBomId = QueryBomId);
            AddLineCommand = new RelayCommand(async _ => await AddLineAsync(), _ => CanAddLine());
            //AddChildCommand = new RelayCommand(async p => { System.Diagnostics.Debug.WriteLine($"AddChild param={p?.GetType().FullName} Can={CanAddLineBase()}"); await AddChildAsync(p); }, p => p is BomTreeNodeResponse && CanAddLineBase());
            AddChildCommand = new RelayCommand(async p => { System.Diagnostics.Debug.WriteLine($"AddChildCommand CanExecute: {p?.GetType().Name ?? "null"}"); await AddChildAsync(p); }, p => p is BomTreeNodeResponse && CanAddLineBase());
            DeleteNodeCommand = new RelayCommand(async p => { System.Diagnostics.Debug.WriteLine($"DeleteNode CanExecute: {p?.GetType().Name ?? "null"}"); await DeleteNodeAsync(p); }, p => p is BomTreeNodeResponse);
            DeleteLineCommand = new RelayCommand(async p => await DeleteLineAsync(p), p => p is BomLineResponse);
        }

        public event PropertyChangedEventHandler? PropertyChanged;
        private void OnChanged([CallerMemberName] string? p = null) => PropertyChanged?.Invoke(this, new PropertyChangedEventArgs(p));

        private async Task QueryAsync()
        {
            var bomId = QueryBomId?.Trim();
            if (string.IsNullOrWhiteSpace(bomId)) { MessageBox.Show("BOM ID는 필수입니다."); return; }

            try
            {
                Tree.Clear();
                Lines.Clear();

                var flat = await _api.GetAsync<System.Collections.Generic.List<BomLineResponse>>(Endpoints.BomLines(bomId));
                if (flat != null) foreach (var l in flat) Lines.Add(l);

                var nodes = await _api.GetAsync<System.Collections.Generic.List<BomTreeNodeResponse>>(Endpoints.BomTree(bomId));
                if (nodes != null) foreach (var n in nodes) Tree.Add(n);
            }
            catch (Exception ex)
            {
                MessageBox.Show($"조회 실패: {ex.Message}");
            }
        }

        private bool CanAddLineBase()
        {
            return !string.IsNullOrWhiteSpace(AddBomId)
                   && !string.IsNullOrWhiteSpace(AddComponentProductId);
        }

        private bool CanAddLine()
        {
            return CanAddLineBase()
                   && decimal.TryParse(AddQty, out var q) && q > 0
                   && decimal.TryParse(AddScrapRate, out var s) && s >= 0;
        }

        private async Task AddLineAsync()
        {
            if (!CanAddLine()) { MessageBox.Show("필수 값 확인"); return; }

            var req = new BomLineCreateRequest(
                AddBomId!.Trim(),
                string.IsNullOrWhiteSpace(AddParentLineId) ? null : AddParentLineId!.Trim(),
                AddComponentProductId!.Trim(),
                decimal.Parse(AddQty),
                decimal.Parse(AddScrapRate),
                string.IsNullOrWhiteSpace(AddNote) ? null : AddNote
            );

            try
            {
                await _api.PostAsync<BomLineResponse>(Endpoints.CreateBomLine, req);
                if (!string.IsNullOrWhiteSpace(QueryBomId) && QueryBomId!.Trim() == AddBomId!.Trim())
                    await QueryAsync();
                MessageBox.Show("라인 추가 완료");
            }
            catch (Exception ex)
            {
                MessageBox.Show($"추가 실패: {ex.Message}");
            }
        }

        private async Task AddChildAsync(object? parameter)
        {
            System.Diagnostics.Debug.WriteLine($"AddChild in param={parameter} Can={CanAddLineBase()}");
            if (parameter is not BomTreeNodeResponse node) return;

            if (!decimal.TryParse(AddQty, out var qty) || qty <= 0) { MessageBox.Show("Qty는 양수"); return; }
            if (!decimal.TryParse(AddScrapRate, out var scrap) || scrap < 0) scrap = 0.000000m;

            var req = new BomLineCreateRequest(
                AddBomId!.Trim(),
                node.BomLineId,
                AddComponentProductId!.Trim(),
                qty,
                scrap,
                string.IsNullOrWhiteSpace(AddNote) ? null : AddNote
            );
            try
            {
                await _api.PostAsync<BomLineResponse>(Endpoints.CreateBomLine, req);
                if (!string.IsNullOrWhiteSpace(QueryBomId) && QueryBomId!.Trim() == AddBomId!.Trim())
                    await QueryAsync();
            }
            catch (Exception ex)
            {
                MessageBox.Show($"자식 추가 실패: {ex.Message}");
            }
        }

        private async Task DeleteNodeAsync(object? parameter)
        {
            if (parameter is not BomTreeNodeResponse node) return;

            var confirm = MessageBox.Show("선택 라인을 삭제할까요?", "확인", MessageBoxButton.YesNo, MessageBoxImage.Question);
            if (confirm != MessageBoxResult.Yes) return;

            try
            {
                await _api.DeleteAsync(Endpoints.DeleteBomLine(node.BomLineId));
                await QueryAsync();
            }
            catch (Exception ex)
            {
                MessageBox.Show($"삭제 실패: {ex.Message}");
            }
        }

        private async Task DeleteLineAsync(object? parameter)
        {
            if (parameter is not BomLineResponse sel) return;

            var confirm = MessageBox.Show("선택 라인을 삭제할까요?", "확인", MessageBoxButton.YesNo, MessageBoxImage.Question);
            if (confirm != MessageBoxResult.Yes) return;

            try
            {
                await _api.DeleteAsync(Endpoints.DeleteBomLine(sel.BomLineId));
                await QueryAsync();
            }
            catch (Exception ex)
            {
                MessageBox.Show($"삭제 실패: {ex.Message}");
            }
        }
    }
}