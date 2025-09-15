using System.Collections.ObjectModel;
using System.ComponentModel;
using System.Runtime.CompilerServices;
using System.Text.Json.Serialization;
using System.Threading.Tasks;
using Erp.Client.Wpf.Models;
using Erp.Client.Wpf.Services;

namespace Erp.Client.Wpf.ViewModels;

public class BomViewModel : INotifyPropertyChanged
{
    private readonly ApiClient _api;

    public event PropertyChangedEventHandler? PropertyChanged;
    private void OnChanged([CallerMemberName] string? p = null) => PropertyChanged?.Invoke(this, new PropertyChangedEventArgs(p));

    // 바인딩 프로퍼티
    public string? BomId { get; set; }
    public string? ParentLineId { get; set; } // 선택적(계층 BOM)
    public string? ComponentProductId { get; set; }
    public decimal Qty { get; set; } = 1.000000m;
    public decimal ScrapRate { get; set; } = 0.000000m;
    public string? Note { get; set; }

    private bool _isBusy;
    public bool IsBusy { get => _isBusy; set { _isBusy = value; OnChanged(); } }

    private string _status = "";
    public string StatusMessage { get => _status; set { _status = value; OnChanged(); } }

    public ObservableCollection<BomLineResponse> Lines { get; } = new();

    public BomViewModel(ApiClient api) { _api = api; }

    public async Task LoadLines()
    {
        if (string.IsNullOrWhiteSpace(BomId)) { StatusMessage = "BOM ID를 입력하세요"; return; }
        try
        {
            IsBusy = true;
            var path = Endpoints.BomLines(BomId!);
            var res = await _api.GetAsync<List<BomLineResponse>>(path);
            Lines.Clear();
            if (res != null)
                foreach (var l in res) Lines.Add(l);
            StatusMessage = $"라인 {Lines.Count}건 로드";
        }
        catch (Exception ex)
        {
            StatusMessage = $"로드 실패: {ex.Message}";
        }
        finally { IsBusy = false; }
    }

    public async Task AddLine()
    {
        if (string.IsNullOrWhiteSpace(BomId) || string.IsNullOrWhiteSpace(ComponentProductId))
        {
            StatusMessage = "BOM ID와 구성품(ProductId)은 필수";
            return;
        }
        try
        {
            IsBusy = true;
            var req = new BomLineCreateRequest(
                BomId!,
                string.IsNullOrWhiteSpace(ParentLineId) ? null : ParentLineId,
                ComponentProductId!,
                Qty,
                ScrapRate,
                Note
            );
            var res = await _api.PostAsync<BomLineResponse>(Endpoints.CreateBomLine, req);
            if (res != null) Lines.Add(res);
            StatusMessage = "라인 추가 완료";
        }
        catch (Exception ex)
        {
            StatusMessage = $"추가 실패: {ex.Message}";
        }
        finally { IsBusy = false; }
    }

    public async Task DeleteLine(BomLineResponse? selected)
    {
        if (selected == null) { StatusMessage = "삭제할 라인을 선택하세요"; return; }
        try
        {
            IsBusy = true;
            await _api.DeleteAsync(Endpoints.DeleteBomLine(selected.BomLineId));
            Lines.Remove(selected);
            StatusMessage = "라인 삭제 완료(논리 삭제)";
        }
        catch (Exception ex)
        {
            StatusMessage = $"삭제 실패: {ex.Message}";
        }
        finally { IsBusy = false; }
    }
}