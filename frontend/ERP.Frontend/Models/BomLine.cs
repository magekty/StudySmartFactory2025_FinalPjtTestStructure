using System;
using Newtonsoft.Json;

namespace ERP.Frontend.Models
{
    public class BomLine
    {
        [JsonProperty("lineId")]
        public int LineId { get; set; }

        [JsonProperty("bomHeader")]
        public BomHeader BomHeader { get; set; }

        [JsonProperty("itemId")]
        public int ItemId { get; set; }

        [JsonProperty("itemType")]
        public string ItemType { get; set; }

        [JsonProperty("quantity")]
        public decimal Quantity { get; set; }

        [JsonProperty("unit")]
        public string Unit { get; set; }

        [JsonProperty("parentLine")]
        public BomLine ParentLine { get; set; }

        // UI에서 계층 구조를 표시하기 위한 속성
        public string Name { get; set; }
    }
}