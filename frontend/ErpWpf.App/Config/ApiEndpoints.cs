// 8) Config — 엔드포인트
// File: Config/ApiEndpoints.cs
namespace ErpWpf.App.Config;

public sealed class ApiEndpoints
{
    public string MesBaseUrl { get; init; } = "http://localhost:8080";
    public string ErpBaseUrl { get; init; } = "http://localhost:8082";
}