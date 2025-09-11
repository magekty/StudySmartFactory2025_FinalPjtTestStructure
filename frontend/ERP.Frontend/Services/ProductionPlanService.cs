using System.Net.Http;
using System.Threading.Tasks;
using System.Collections.Generic;
using Newtonsoft.Json;
using ERP.Frontend.Models;

namespace ERP.Frontend.Services
{
    public class ProductionPlanService : ApiService
    {
        public async Task<List<ProductionPlan>> GetAllProductionPlansAsync()
        {
            var response = await GetHttpClient().GetAsync("plans");
            response.EnsureSuccessStatusCode();
            var json = await response.Content.ReadAsStringAsync();
            return JsonConvert.DeserializeObject<List<ProductionPlan>>(json);
        }
    }
}