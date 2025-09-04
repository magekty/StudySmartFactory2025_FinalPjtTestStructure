// Pages/Index.cshtml.cs
// 충돌 제거: PageModel 클래스를 HomeModel로 변경하고, 필드/프로퍼티 중복 제거 + nullability 가드
using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading;
using System.Threading.Tasks;
using ErpConsole.Services;
using Microsoft.AspNetCore.Mvc.RazorPages;

namespace ErpConsole.Pages;

public sealed class HomeModel : PageModel
{
    private readonly MesApi _mes;

    public HomeModel(MesApi mes)
    {
        _mes = mes ?? throw new ArgumentNullException(nameof(mes));
    }

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