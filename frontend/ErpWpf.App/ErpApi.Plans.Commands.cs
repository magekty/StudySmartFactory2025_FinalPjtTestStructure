// ErpWpf.App/ErpApi.Plans.Commands.cs
// Plan 생성/수정 API(네임스페이스/partial 통일, Http.Json 사용)
using System.Net.Http;
using System.Net.Http.Json;
using System.Threading;
using System.Threading.Tasks;
using ErpWpf.App.Models;

namespace ErpWpf.App;

public sealed partial class ErpApi
{
    public async Task<HttpResponseMessage> CreatePlanAsync(PlanDto req, CancellationToken ct = default)
        => await _f.CreateClient("erp").PostAsJsonAsync("/plans", req, ct);

    public async Task<HttpResponseMessage> UpdatePlanAsync(PlanDto req, CancellationToken ct = default)
        => await _f.CreateClient("erp").PutAsJsonAsync("/plans", req, ct);
}