using ErpWpf.App.Models.Inventory;
using ErpWpf.App.Net;
using ErpWpf.App.Ports;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace ErpWpf.App.Adapters.Mes
{
    public sealed class MesInventoryClient : TypedClientBase, IMesInventoryClient
    {
        public MesInventoryClient(System.Net.Http.HttpClient http) : base(http, "ERP-FE") { }

        public Task<AvailabilityDto> GetAvailabilityAsync(string itemId, DateTimeOffset at, CancellationToken ct)
        {
            var url = $"/mes/inventory/availability?itemId={Uri.EscapeDataString(itemId)}&at={Uri.EscapeDataString(at.UtcDateTime.ToString("o"))}";
            return GetAsync<AvailabilityDto>(url, ct)!;
        }
    }
}
