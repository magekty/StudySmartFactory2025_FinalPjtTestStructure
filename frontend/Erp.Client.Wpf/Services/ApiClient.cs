using Erp.Client.Wpf.Utils;
using System.Net.Http;
using System.Net.Http.Headers;
using System.Net.Http.Json;
using System.Text;
using System.Text.Json;

namespace Erp.Client.Wpf.Services;

public class ApiClient
{
    private readonly HttpClient _http;
    private readonly JsonSerializerOptions _json;

    public ApiClient(string baseUrl, TimeSpan timeout, JsonSerializerOptions? json = null)
    {
        _http = new HttpClient { BaseAddress = new Uri(baseUrl), Timeout = timeout };
        _http.DefaultRequestHeaders.Accept.Add(new MediaTypeWithQualityHeaderValue("application/json"));
        _json = json ?? JsonOptions.Default;
    }

    public async Task<T?> GetAsync<T>(string path, CancellationToken ct = default)
    {
        using var res = await _http.GetAsync(path, ct);
        await EnsureSuccess(res);
        var json = await res.Content.ReadAsStringAsync(ct);
        return JsonSerializer.Deserialize<T>(json, _json);
    }

    public async Task<T?> PostAsync<T>(string path, object body, CancellationToken ct = default)
    {
        var content = new StringContent(JsonSerializer.Serialize(body, _json), Encoding.UTF8, "application/json");
        using var res = await _http.PostAsync(path, content, ct);
        await EnsureSuccess(res);
        var json = await res.Content.ReadAsStringAsync(ct);
        return JsonSerializer.Deserialize<T>(json, _json);
    }

    public async Task PostAsync(string path, object? body, CancellationToken ct = default)
    {
        var res = await _http.PostAsJsonAsync(path, body, ct);
        res.EnsureSuccessStatusCode();
    }

    public async Task<T?> PutAsync<T>(string path, object body, CancellationToken ct = default)
    {
        var res = await _http.PutAsJsonAsync(path, body, ct);
        res.EnsureSuccessStatusCode();
        return await res.Content.ReadFromJsonAsync<T>(cancellationToken: ct);
    }

    public async Task<T?> PatchAsync<T>(string path, object body, CancellationToken ct = default)
    {
        using var req = new HttpRequestMessage(HttpMethod.Patch, path)
        {
            Content = JsonContent.Create(body)
        };
        using var res = await _http.SendAsync(req, ct);
        res.EnsureSuccessStatusCode();
        return await res.Content.ReadFromJsonAsync<T>(cancellationToken: ct);
    }
    public async Task PatchAsync(string path, object body, CancellationToken ct = default)
    {
        using var req = new HttpRequestMessage(HttpMethod.Patch, path)
        {
            Content = JsonContent.Create(body)
        };
        using var res = await _http.SendAsync(req, ct);
        res.EnsureSuccessStatusCode();
    }

    public async Task DeleteAsync(string path, CancellationToken ct = default)
    {
        using var res = await _http.DeleteAsync(path, ct);
        await EnsureSuccess(res);
    }

    private static async Task EnsureSuccess(HttpResponseMessage res)
    {
        if (res.IsSuccessStatusCode) return;
        var msg = await res.Content.ReadAsStringAsync();
        throw new HttpRequestException($"API {(int)res.StatusCode} - {msg}");
    }
    /**
 * 🚨 1.2. MES 전송 API 메소드 추가
 * ERP 백엔드의 엔드포인트: POST /api/plans/{planId}/send-to-mes
 * 반환값: Plan 상세 DTO 또는 상태 업데이트 결과
 */
    public async Task<T?> PostToMesAsync<T>(string path, object body, CancellationToken ct = default)
    {
        // PUTAsJsonAsync 또는 PostAsJsonAsync를 사용하여 간결하게 구현
        using var res = await _http.PostAsJsonAsync(path, body, _json, ct);
        res.EnsureSuccessStatusCode();

        // 백엔드에서 PlanDetailDto를 반환한다고 가정하고 ReadFromJsonAsync 사용
        return await res.Content.ReadFromJsonAsync<T>(_json, ct);
    }
}