using System;
using Newtonsoft.Json;

namespace ERP.Frontend.Models
{
    public class Material
    {
        [JsonProperty("materialId")]
        public int MaterialId { get; set; }

        [JsonProperty("name")]
        public string Name { get; set; }

        [JsonProperty("unit")]
        public string Unit { get; set; }

        [JsonProperty("cost")]
        public decimal Cost { get; set; }

        [JsonProperty("supplier")]
        public string Supplier { get; set; }
    }
}