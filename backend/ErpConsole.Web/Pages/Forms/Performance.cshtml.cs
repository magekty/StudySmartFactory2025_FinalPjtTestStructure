// Pages/Forms/Performance.cshtml.cs (실적 등록 + 멱등키)
using ErpConsole.Services;
using Microsoft.AspNetCore.Mvc;
using Microsoft.AspNetCore.Mvc.RazorPages;

namespace ErpConsole.Pages.Forms;

public class PerformanceModel : PageModel
{
    private readonly ErpApi _erp;
    public PerformanceModel(ErpApi erp) { _erp = erp; }

    [BindProperty] public string WorkOrderId { get; set; } = "";
    [BindProperty] public string ItemId { get; set; } = "I-0001";
    [BindProperty] public string ProcessId { get; set; } = "P-0001";
    [BindProperty] public string EquipmentId { get; set; } = "E-0001";
    [BindProperty] public decimal ProducedQty { get; set; } = 10m;
    [BindProperty] public decimal DefectQty { get; set; } = 0m;
    [BindProperty] public DateTimeOffset StartUtc { get; set; } = DateTimeOffset.UtcNow.AddMinutes(-30);
    [BindProperty] public DateTimeOffset EndUtc { get; set; } = DateTimeOffset.UtcNow;
    [BindProperty] public string? RequestId { get; set; }

    public string IdempotencyKey { get; private set; } = Guid.NewGuid().ToString();
    public string? Result { get; private set; }

    public void OnGet()
    {
        IdempotencyKey = Guid.NewGuid().ToString();
    }

    public async Task<IActionResult> OnPostAsync(CancellationToken ct)
    {
        var req = new PerfCreateReq(
            WorkOrderId, ItemId, ProcessId, EquipmentId,
            ProducedQty, DefectQty,
            StartUtc.ToUniversalTime().ToString("yyyy-MM-dd'T'HH:mm:ss'Z'"),
            EndUtc.ToUniversalTime().ToString("yyyy-MM-dd'T'HH:mm:ss'Z'"),
            string.IsNullOrWhiteSpace(RequestId) ? null : RequestId
        );

        var idem = Guid.NewGuid().ToString();
        var (ok, body) = await _erp.CreatePerformanceAsync(req, idem, ct);
        Result = ok ? $"OK ({idem}): {body}" : $"ERR ({idem}): {body}";
        IdempotencyKey = idem; // 화면에 마지막 키 노출
        return Page();
    }
}