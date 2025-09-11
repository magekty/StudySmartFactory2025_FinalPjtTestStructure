using System;
using Newtonsoft.Json;

namespace ERP.Frontend.Models
{
    public class ProductionPlan
    {
        [JsonProperty("planId")]
        public int PlanId { get; set; }

        [JsonProperty("product")]
        public Product Product { get; set; }

        [JsonProperty("quantity")]
        public decimal Quantity { get; set; }

        [JsonProperty("plannedStart")]
        public DateTime PlannedStart { get; set; }

        [JsonProperty("plannedEnd")]
        public DateTime PlannedEnd { get; set; }

        [JsonProperty("status")]
        public string Status { get; set; }

        // UI를 위한 추가 속성
        public string ProductName => Product?.Name;
    }
}