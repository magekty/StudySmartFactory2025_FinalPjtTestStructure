using System.Collections.ObjectModel;
using System.ComponentModel;
using System.Runtime.CompilerServices;
using Erp.Client.Wpf.Models;
using Erp.Client.Wpf.Services;

namespace Erp.Client.Wpf.ViewModels;

public class CostViewModel : INotifyPropertyChanged
{
    private readonly ApiClient _api;
    public event PropertyChangedEventHandler? PropertyChanged;

    public string? ProductId { get; set; }
    public string? PlanId { get; set; }
    public decimal Qty { get; set; } = 1.000000m;
    public decimal LaborRate { get; set; } = 0.100000m;
    public decimal OverheadRate { get; set; } = 0.100000m;
    public DateOnly BaseDate { get; set; } = DateOnly.FromDateTime(DateTime.UtcNow);

    private CostSnapshotResponse? _snapshot;
    public CostSnapshotResponse? Snapshot { get => _snapshot; set { _snapshot = value; OnChanged(); } }
    public ObservableCollection<CostSnapshotDetailResponse> Details { get; } = new();

    public CostViewModel(ApiClient api) => _api = api;

    public async Task CalcByProduct()
    {
        if (string.IsNullOrWhiteSpace(ProductId)) return;
        var path = Endpoints.CostByProduct(ProductId!, Qty.ToString("0.######"), LaborRate.ToString("0.######"), OverheadRate.ToString("0.######"), BaseDate.ToString("yyyy-MM-dd"));
        var res = await _api.GetAsync<CostSnapshotResponse>(path);
        Apply(res);
    }

    public async Task CalcByPlan()
    {
        if (string.IsNullOrWhiteSpace(PlanId)) return;
        var path = Endpoints.CostByPlan(PlanId!, LaborRate.ToString("0.######"), OverheadRate.ToString("0.######"));
        var res = await _api.GetAsync<CostSnapshotResponse>(path);
        Apply(res);
    }

    private void Apply(CostSnapshotResponse? res)
    {
        Snapshot = res;
        Details.Clear();
        if (res?.Details != null)
            foreach (var d in res.Details) Details.Add(d);
    }

    private void OnChanged([CallerMemberName] string? p = null) => PropertyChanged?.Invoke(this, new PropertyChangedEventArgs(p));
}