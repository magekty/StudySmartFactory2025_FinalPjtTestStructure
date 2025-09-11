using System.Net.Http;
using System.Threading.Tasks;
using System.Collections.Generic;
using Newtonsoft.Json;
using ERP.Frontend.Models;

namespace ERP.Frontend.Services
{
    public class BomService : ApiService
    {
        public async Task<List<BomLine>> GetBomLinesByBomHeaderIdAsync(int bomId)
        {
            var response = await GetHttpClient().GetAsync($"boms/{bomId}/lines");
            response.EnsureSuccessStatusCode();
            var json = await response.Content.ReadAsStringAsync();
            return JsonConvert.DeserializeObject<List<BomLine>>(json);
        }
    }
}