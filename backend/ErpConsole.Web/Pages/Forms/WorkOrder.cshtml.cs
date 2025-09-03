// Pages/Forms/WorkOrder.cshtml.cs (지시 생성)
using ErpConsole.Services;
using Microsoft.AspNetCore.Mvc;
using Microsoft.AspNetCore.Mvc.RazorPages;

namespace ErpConsole.Pages.Forms;

public class WorkOrderModel : PageModel
{
    private readonly ErpApi _erp;
    public WorkOrderModel(ErpApi erp) { _erp = erp; }

    [BindProperty] public string WorkOrderNumber { get; set; } = $"WO-UI-{DateTimeOffset.UtcNow.ToUnixTimeSeconds()}";
    [BindProperty] public string ItemId { get; set; } = "I-0001";
    [BindProperty] public string ProcessId { get; set; } = "P-0001";
    [BindProperty] public string EquipmentId { get; set; } = "E-0001";
    [BindProperty] public decimal Qty { get; set; } = 100m;

    public string? Result { get; private set; }

    public void OnGet() { }

    public async Task<IActionResult> OnPostAsync(CancellationToken ct)
    {
        var req = new WoCreateReq(WorkOrderNumber, ItemId, ProcessId, EquipmentId, Qty);
        var (ok, body) = await _erp.CreateWorkOrderAsync(req, ct);
        Result = ok ? "OK: " + body : "ERR: " + body;
        return Page();
    }
}