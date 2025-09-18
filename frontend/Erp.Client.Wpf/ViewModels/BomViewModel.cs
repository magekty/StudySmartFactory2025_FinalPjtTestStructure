// ViewModels/BomViewModel.cs
using System;
using System.Collections.ObjectModel;
using System.ComponentModel;
using System.Diagnostics;
using System.Linq;
using System.Runtime.CompilerServices;
using System.Threading.Tasks;
using System.Windows;
using System.Windows.Input;
using Erp.Client.Wpf.Models;
using Erp.Client.Wpf.Services;

namespace Erp.Client.Wpf.ViewModels
{
    public partial class BomViewModel : INotifyPropertyChanged, IDataErrorInfo
    {
        private readonly ApiClient _api;
        public BomViewModel(ApiClient api) { _api = api; }

        // 조회
        private string? _queryBomId;
        public string? QueryBomId { get => _queryBomId; set { _queryBomId = value; OnChanged(); CommandManager.InvalidateRequerySuggested(); } }

        public ObservableCollection<BomTreeNodeResponse> Tree { get; } = new();
        public ObservableCollection<BomLineResponse> Lines { get; } = new();

        // 선택 동기화 (그리드 <-> 트리)
        private BomLineResponse? _selectedLine;
        public BomLineResponse? SelectedLine
        {
            get => _selectedLine;
            set { _selectedLine = value; OnChanged(); SyncTreeSelectionFromLine(); }
        }

        private BomTreeNodeResponse? _selectedNode;
        public BomTreeNodeResponse? SelectedNode
        {
            get => _selectedNode;
            set { _selectedNode = value; OnChanged(); SyncLineSelectionFromTree(); }
        }

        // 추가 입력
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

        // Busy
        private bool _isBusy;
        public bool IsBusy { get => _isBusy; set { _isBusy = value; OnChanged(); CommandManager.InvalidateRequerySuggested(); } }

        // Commands
        public ICommand QueryCommand => new RelayCommand(async _ => { Debug.WriteLine("쿼리"); await QueryAsync(); }, _ => !IsBusy && string.IsNullOrWhiteSpace(this[nameof(QueryBomId)]));
        public ICommand AddLineCommand => new RelayCommand(async _ => await AddLineAsync(), _ => !IsBusy && CanAddLine());
        public ICommand AddChildCommand => new RelayCommand(async p => await AddChildAsync(p), p => !IsBusy && p is BomTreeNodeResponse && CanAddLineBase());
        public ICommand DeleteNodeCommand => new RelayCommand(async p => { Debug.WriteLine("삭제"); await DeleteNodeAsync(p); }, p => !IsBusy && p is BomTreeNodeResponse);
        public ICommand DeleteLineCommand => new RelayCommand(async p => await DeleteLineAsync(p), p => !IsBusy && p is BomLineResponse);

        public event PropertyChangedEventHandler? PropertyChanged;
        private void OnChanged([CallerMemberName] string? p = null) => PropertyChanged?.Invoke(this, new PropertyChangedEventArgs(p));

        private async Task QueryAsync()
        {
            if (string.IsNullOrWhiteSpace(QueryBomId)) return;
            IsBusy = true;
            try
            {
                Tree.Clear();
                Lines.Clear();

                var flat = await _api.GetAsync<System.Collections.Generic.List<BomLineResponse>>(Endpoints.BomLines(QueryBomId!.Trim()));
                if (flat != null) foreach (var l in flat) Lines.Add(l);

                var nodes = await _api.GetAsync<System.Collections.Generic.List<BomTreeNodeResponse>>(Endpoints.BomTree(QueryBomId!.Trim()));
                if (nodes != null) foreach (var n in nodes) Tree.Add(n);
                AddBomId = QueryBomId;
            }
            catch (Exception ex) { MessageBox.Show($"조회 실패: {ex.Message}"); }
            finally { IsBusy = false; }
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
            if (!CanAddLine()) return;

            var req = new BomLineCreateRequest(
                AddBomId!.Trim(),
                string.IsNullOrWhiteSpace(AddParentLineId) ? null : AddParentLineId!.Trim(),
                AddComponentProductId!.Trim(),
                decimal.Parse(AddQty),
                decimal.Parse(AddScrapRate),
                string.IsNullOrWhiteSpace(AddNote) ? null : AddNote
            );

            IsBusy = true;
            try
            {
                await _api.PostAsync<BomLineResponse>(Endpoints.CreateBomLine, req);
                if (!string.IsNullOrWhiteSpace(QueryBomId) && QueryBomId!.Trim() == AddBomId!.Trim())
                    await QueryAsync();
                MessageBox.Show("라인 추가 완료");
            }
            catch (Exception ex) { MessageBox.Show($"추가 실패: {ex.Message}"); }
            finally { IsBusy = false; }
        }

        private async Task AddChildAsync(object? parameter)
        {
            if (parameter is not BomTreeNodeResponse node) return;
            if (!decimal.TryParse(AddQty, out var qty) || qty <= 0) return;
            if (!decimal.TryParse(AddScrapRate, out var scrap) || scrap < 0) scrap = 0.000000m;

            var req = new BomLineCreateRequest(
                AddBomId!.Trim(),
                node.BomLineId,
                AddComponentProductId!.Trim(),
                qty,
                scrap,
                string.IsNullOrWhiteSpace(AddNote) ? null : AddNote
            );

            IsBusy = true;
            try
            {
                await _api.PostAsync<BomLineResponse>(Endpoints.CreateBomLine, req);
                if (!string.IsNullOrWhiteSpace(QueryBomId) && QueryBomId!.Trim() == AddBomId!.Trim())
                    await QueryAsync();
            }
            catch (Exception ex) { MessageBox.Show($"자식 추가 실패: {ex.Message}"); }
            finally { IsBusy = false; }
        }

        private async Task DeleteNodeAsync(object? parameter)
        {
            if (parameter is not BomTreeNodeResponse node) return;
            if (MessageBox.Show("선택 라인을 삭제할까요?", "확인", MessageBoxButton.YesNo, MessageBoxImage.Question) != MessageBoxResult.Yes) return;

            IsBusy = true;
            try
            {
                await _api.DeleteAsync(Endpoints.DeleteBomLine(node.BomLineId));
                await QueryAsync();
            }
            catch (Exception ex) { MessageBox.Show($"삭제 실패: {ex.Message}"); }
            finally { IsBusy = false; }
        }

        private async Task DeleteLineAsync(object? parameter)
        {
            if (parameter is not BomLineResponse sel) return;
            if (MessageBox.Show("선택 라인을 삭제할까요?", "확인", MessageBoxButton.YesNo, MessageBoxImage.Question) != MessageBoxResult.Yes) return;

            IsBusy = true;
            try
            {
                await _api.DeleteAsync(Endpoints.DeleteBomLine(sel.BomLineId));
                await QueryAsync();
            }
            catch (Exception ex) { MessageBox.Show($"삭제 실패: {ex.Message}"); }
            finally { IsBusy = false; }
        }

        // 선택 동기화
        private void SyncTreeSelectionFromLine()
        {
            if (SelectedLine == null) return;
            var target = FindNodeById(SelectedLine.BomLineId);
            if (target != null && !ReferenceEquals(SelectedNode, target))
                SelectedNode = target;
        }

        private void SyncLineSelectionFromTree()
        {
            if (SelectedNode == null) return;
            var line = Lines.FirstOrDefault(l => l.BomLineId == SelectedNode.BomLineId);
            if (line != null && !ReferenceEquals(SelectedLine, line))
            {
                _selectedLine = line;
                OnChanged(nameof(SelectedLine));
            }
            // 추가 입력 Parent 채움
            AddParentLineId = SelectedNode.BomLineId;
        }

        private BomTreeNodeResponse? FindNodeById(string id)
        {
            foreach (var root in Tree)
            {
                var r = Dfs(root, id);
                if (r != null) return r;
            }
            return null;
        }
        private BomTreeNodeResponse? Dfs(BomTreeNodeResponse n, string id)
        {
            if (n.BomLineId == id) return n;
            if (n.Children != null)
            {
                foreach (var c in n.Children)
                {
                    var r = Dfs(c, id);
                    if (r != null) return r;
                }
            }
            return null;
        }

        // IDataErrorInfo (실시간 검증 → 붉은 테두리/아이콘용)
        public string Error => string.Empty;
        public string this[string columnName]
        {
            get
            {
                switch (columnName)
                {
                    case nameof(QueryBomId): return string.IsNullOrWhiteSpace(QueryBomId) ? "필수" : string.Empty;
                    case nameof(AddBomId): return string.IsNullOrWhiteSpace(AddBomId) ? "필수" : string.Empty;
                    case nameof(AddComponentProductId): return string.IsNullOrWhiteSpace(AddComponentProductId) ? "필수" : string.Empty;
                    case nameof(AddQty): return !decimal.TryParse(AddQty, out var q) || q <= 0 ? "양수 필요" : string.Empty;
                    case nameof(AddScrapRate): return !decimal.TryParse(AddScrapRate, out var s) || s < 0 ? "0 이상" : string.Empty;
                }
                return string.Empty;
            }
        }
    }
}