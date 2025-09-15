using Erp.Client.Wpf.Utils;
using System.Net.Http;
using System.Net.Http.Headers;
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

    public async Task<T?> PatchAsync<T>(string path, CancellationToken ct = default)
    {
        var req = new HttpRequestMessage(new HttpMethod("PATCH"), path);
        using var res = await _http.SendAsync(req, ct);
        await EnsureSuccess(res);
        var json = await res.Content.ReadAsStringAsync(ct);
        return JsonSerializer.Deserialize<T>(json, _json);
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
}