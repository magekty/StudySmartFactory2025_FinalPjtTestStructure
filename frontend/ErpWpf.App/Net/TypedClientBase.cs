// 3) Net — 타입드 클라이언트 공통
// File: Net/TypedClientBase.cs
using System;
using System.Net.Http;
using System.Net.Http.Json;
using System.Threading;
using System.Threading.Tasks;

namespace ErpWpf.App.Net;

public static class HttpHeaders
{
    public const string RequestId = "X-Request-Id";
    public const string ClientId = "X-Client-Id";
}

public abstract class TypedClientBase
{
    protected readonly HttpClient _http;
    protected readonly string _clientId;

    protected TypedClientBase(HttpClient http, string clientId)
    {
        _http = http;
        _clientId = clientId;
        _http.Timeout = TimeSpan.FromSeconds(5);
    }

    protected async Task<T?> GetAsync<T>(string url, CancellationToken ct)
    {
        using var req = new HttpRequestMessage(HttpMethod.Get, url);
        req.Headers.TryAddWithoutValidation(HttpHeaders.RequestId, Guid.NewGuid().ToString());
        req.Headers.TryAddWithoutValidation(HttpHeaders.ClientId, _clientId);

        Exception? last = null;
        for (int attempt = 0; attempt < 2; attempt++)
        {
            try
            {
                using var res = await _http.SendAsync(req, HttpCompletionOption.ResponseHeadersRead, ct);
                res.EnsureSuccessStatusCode();
                return await res.Content.ReadFromJsonAsync<T>(cancellationToken: ct);
            }
            catch (Exception ex) when (attempt == 0)
            {
                last = ex;
                await Task.Delay(200, ct);
            }
        }
        throw last ?? new HttpRequestException("GET failed");
    }
}