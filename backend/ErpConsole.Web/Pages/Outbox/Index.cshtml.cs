// Pages/Outbox/Index.cshtml.cs (목록/필터/검색)
using ErpConsole.Services;
using Microsoft.AspNetCore.Mvc;
using Microsoft.AspNetCore.Mvc.RazorPages;

namespace ErpConsole.Pages.Outbox;

public class IndexModel : PageModel
{
    private readonly MesApi _mes;
    public IndexModel(MesApi mes) { _mes = mes; }

    [BindProperty(SupportsGet = true)] public string? Status { get; set; }
    [BindProperty(SupportsGet = true)] public string? EventType { get; set; }
    [BindProperty(SupportsGet = true)] public int SinceMinutes { get; set; } = 10080; // 7일
    [BindProperty(SupportsGet = true)] public int Limit { get; set; } = 100;
    [BindProperty(SupportsGet = true)] public string? SearchWo { get; set; }

    public OutboxListView Data { get; private set; } = new(new());

    public async Task OnGet(CancellationToken ct)
    {
        Data = await _mes.GetOutboxListAsync(Status, EventType, SinceMinutes, Limit, SearchWo, ct);
    }
}