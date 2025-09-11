using System;
using Newtonsoft.Json;

namespace ERP.Frontend.Models
{
    public class BomHeader
    {
        [JsonProperty("bomId")]
        public int BomId { get; set; }

        [JsonProperty("product")]
        public Product Product { get; set; }

        [JsonProperty("version")]
        public int Version { get; set; }

        [JsonProperty("description")]
        public string Description { get; set; }
    }
}