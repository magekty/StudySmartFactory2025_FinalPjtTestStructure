// Pages/Index.cshtml.cs (대시보드 카드 + Shadow 배지)
using ErpConsole.Services;
using Microsoft.AspNetCore.Mvc;
using Microsoft.AspNetCore.Mvc.RazorPages;

namespace ErpConsole.Pages;

public class IndexModel : PageModel
{
    private readonly MesApi _mes;
    public IndexModel(MesApi mes) { _mes = mes; }

    public Dictionary<string, long> ByStatus { get; private set; } = new();
    public ShadowView Shadow { get; private set; } = new(false, false, false, false, false);

    public async Task OnGet(CancellationToken ct)
    {
        var summary = await _mes.GetOutboxSummaryAsync(ct);
        ByStatus = summary.byStatus
            .GroupBy(x => x.status)
            .ToDictionary(g => g.Key, g => g.Sum(v => v.count));
        Shadow = await _mes.GetShadowAsync(ct);
    }
}