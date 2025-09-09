using ErpWpf.App.Models.Boms;
using ErpWpf.App.Net;
using ErpWpf.App.Ports;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace ErpWpf.App.Adapters.Mes
{
    public sealed class MesBomClient : TypedClientBase, IMesBomClient
    {
        public MesBomClient(System.Net.Http.HttpClient http) : base(http, "ERP-FE") { }

        public Task<BomDetailDto> GetBomByItemAsync(string itemId, string? rev, string? alt, CancellationToken ct)
        {
            rev ??= "A"; alt ??= "STD";
            var url = $"/mes/boms/{Uri.EscapeDataString(itemId)}?rev={Uri.EscapeDataString(rev)}&alt={Uri.EscapeDataString(alt)}";
            return GetAsync<BomDetailDto>(url, ct)!;
        }
    }
}
