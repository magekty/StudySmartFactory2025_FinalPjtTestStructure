// ErpWpf.App/ErpApi.cs
using System.Net.Http;
using System.Net.Http.Json;
using System.Threading.Tasks;

public record WoCreateReq(string workOrderNumber, string itemId, string processId, string equipmentId, decimal qty);
public record WoStatusReq(string status, string changedAt);
public record PerfCreateReq(
    string workOrderId, string itemId, string processId, string equipmentId,
    decimal producedQty, decimal defectQty, string startTime, string endTime, string? requestId
);

public sealed class ErpApi
{
    private readonly IHttpClientFactory _f;
    public ErpApi(IHttpClientFactory f) => _f = f;

    public async Task<HttpResponseMessage> CreateWorkOrderAsync(WoCreateReq req)
        => await _f.CreateClient("erp").PostAsJsonAsync("/erp/work-orders", req);

    public async Task<HttpResponseMessage> ChangeWoStatusAsync(string woId, WoStatusReq req)
        => await _f.CreateClient("erp").PutAsJsonAsync($"/erp/work-orders/{woId}/status", req);

    public async Task<HttpResponseMessage> CreatePerformanceAsync(PerfCreateReq req, string idempotencyKey)
    {
        var http = _f.CreateClient("erp");
        using var msg = new HttpRequestMessage(HttpMethod.Post, "/erp/performances")
        {
            Content = JsonContent.Create(req)
        };
        msg.Headers.TryAddWithoutValidation("X-Idempotency-Key", idempotencyKey);
        return await http.SendAsync(msg);
    }
}