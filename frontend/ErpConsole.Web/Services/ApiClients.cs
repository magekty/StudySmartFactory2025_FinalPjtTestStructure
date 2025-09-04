// Services/ApiClients.cs (HttpClient 래퍼: MES/ERP 호출)
using System.Net.Http.Json;

namespace ErpConsole.Services;

public record OutboxSummaryItem(string status, long count);
public record OutboxSummaryView(List<OutboxSummaryItem> byStatus);

public record OutboxRow(
    long outboxId,
    string eventType,
    string status,
    string idempotencyKey,
    string? workOrderId,
    string? lastError,
    DateTime createdAtUtc
);
public record OutboxListView(List<OutboxRow> rows);

public record ShadowView(bool enabled, bool workOrders, bool performances, bool backflush, bool cost);

// ERP DTOs
public record WoCreateReq(string workOrderNumber, string itemId, string processId, string equipmentId, decimal qty);
public record WoStatusReq(string status, string changedAt); // UTC ISO (…Z)
public record PerfCreateReq(
    string workOrderId, string itemId, string processId, string equipmentId,
    decimal producedQty, decimal defectQty, string startTime, string endTime, string? requestId
);

public class MesApi
{
    private readonly IHttpClientFactory _f;
    public MesApi(IHttpClientFactory f) => _f = f;

    public async Task<OutboxSummaryView> GetOutboxSummaryAsync(CancellationToken ct = default)
    {
        var http = _f.CreateClient("mes");
        return await http.GetFromJsonAsync<OutboxSummaryView>("/internal/outbox/summary", ct)
               ?? new OutboxSummaryView(new());
    }

    public async Task<OutboxListView> GetOutboxListAsync(string? status, string? eventType, int sinceMinutes, int limit, string? searchWo, CancellationToken ct = default)
    {
        var http = _f.CreateClient("mes");
        var url = $"/internal/outbox?sinceMinutes={sinceMinutes}&limit={limit}" +
                  (string.IsNullOrWhiteSpace(status) ? "" : $"&status={Uri.EscapeDataString(status)}") +
                  (string.IsNullOrWhiteSpace(eventType) ? "" : $"&eventType={Uri.EscapeDataString(eventType)}") +
                  (string.IsNullOrWhiteSpace(searchWo) ? "" : $"&searchWo={Uri.EscapeDataString(searchWo)}");
        return await http.GetFromJsonAsync<OutboxListView>(url, ct) ?? new OutboxListView(new());
    }

    public async Task<ShadowView> GetShadowAsync(CancellationToken ct = default)
    {
        var http = _f.CreateClient("mes");
        return await http.GetFromJsonAsync<ShadowView>("/internal/config/shadow", ct)
               ?? new ShadowView(false, false, false, false, false);
    }
}

public class ErpApi
{
    private readonly IHttpClientFactory _f;
    public ErpApi(IHttpClientFactory f) => _f = f;

    public async Task<(bool ok, string body)> CreateWorkOrderAsync(WoCreateReq req, CancellationToken ct = default)
    {
        var http = _f.CreateClient("erp");
        var res = await http.PostAsJsonAsync("/erp/work-orders", req, ct);
        var body = await res.Content.ReadAsStringAsync(ct);
        return (res.IsSuccessStatusCode, body);
    }

    public async Task<(bool ok, string body)> ChangeWoStatusAsync(string workOrderId, WoStatusReq req, CancellationToken ct = default)
    {
        var http = _f.CreateClient("erp");
        var res = await http.PutAsJsonAsync($"/erp/work-orders/{Uri.EscapeDataString(workOrderId)}/status", req, ct);
        var body = await res.Content.ReadAsStringAsync(ct);
        return (res.IsSuccessStatusCode, body);
    }

    public async Task<(bool ok, string body)> CreatePerformanceAsync(PerfCreateReq req, string idempotencyKey, CancellationToken ct = default)
    {
        var http = _f.CreateClient("erp");
        using var msg = new HttpRequestMessage(HttpMethod.Post, "/erp/performances")
        {
            Content = JsonContent.Create(req)
        };
        msg.Headers.TryAddWithoutValidation("X-Idempotency-Key", idempotencyKey);
        var res = await http.SendAsync(msg, ct);
        var body = await res.Content.ReadAsStringAsync(ct);
        return (res.IsSuccessStatusCode, body);
    }
}
