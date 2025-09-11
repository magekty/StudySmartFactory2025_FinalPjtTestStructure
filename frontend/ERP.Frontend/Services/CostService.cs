using System.Net.Http;
using System.Threading.Tasks;
using Newtonsoft.Json;
using System.Globalization;

namespace ERP.Frontend.Services
{
    public class CostService : ApiService
    {
        public async Task<decimal> CalculateCostForPlanAsync(int planId, decimal laborCostRate, decimal manufacturingOverheadRate)
        {
            var url = $"cost/{planId}?laborCostRate={laborCostRate.ToString(CultureInfo.InvariantCulture)}&manufacturingOverheadRate={manufacturingOverheadRate.ToString(CultureInfo.InvariantCulture)}";
            var response = await GetHttpClient().GetAsync(url);
            response.EnsureSuccessStatusCode();
            var json = await response.Content.ReadAsStringAsync();
            return JsonConvert.DeserializeObject<decimal>(json);
        }
    }
}