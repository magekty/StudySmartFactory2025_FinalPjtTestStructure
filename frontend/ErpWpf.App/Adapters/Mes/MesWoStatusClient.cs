using System;
using System.Collections.Generic;
using System.Threading;
using System.Threading.Tasks;
using ErpWpf.App.Net;
using ErpWpf.App.Ports;
using ErpWpf.App.Models.Boms;
using ErpWpf.App.Models.Inventory;

namespace ErpWpf.App.Adapters.Mes;

public sealed class MesWoStatusClient : TypedClientBase, IMesWoStatusClient
{
    public MesWoStatusClient(System.Net.Http.HttpClient http) : base(http, "ERP-FE") { }

    public async Task<IReadOnlyList<PlanWoStatuses>> GetStatusesAsync(IEnumerable<string> planIds, CancellationToken ct)
    {
        var ids = string.Join(",", planIds);
        var url = $"/mes/wos/status?planIds={Uri.EscapeDataString(ids)}";
        var result = await GetAsync<List<PlanWoStatuses>>(url, ct);
        return result ?? new List<PlanWoStatuses>();
    }
}