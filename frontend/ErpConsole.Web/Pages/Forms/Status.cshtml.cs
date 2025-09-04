// Pages/Forms/Status.cshtml.cs (상태 전이)
using ErpConsole.Services;
using Microsoft.AspNetCore.Mvc;
using Microsoft.AspNetCore.Mvc.RazorPages;

namespace ErpConsole.Pages.Forms;

public class StatusModel : PageModel
{
    private readonly ErpApi _erp;
    public StatusModel(ErpApi erp) { _erp = erp; }

    [BindProperty] public string WorkOrderId { get; set; } = "";
    [BindProperty] public string Status { get; set; } = "R"; // P->R or R->C
    public string? Result { get; private set; }

    public void OnGet() { }

    public async Task<IActionResult> OnPostAsync(CancellationToken ct)
    {
        var req = new WoStatusReq(Status, DateTimeOffset.UtcNow.ToString("yyyy-MM-dd'T'HH:mm:ss'Z'"));
        var (ok, body) = await _erp.ChangeWoStatusAsync(WorkOrderId, req, ct);
        Result = ok ? "OK: " + body : "ERR: " + body;
        return Page();
    }
}