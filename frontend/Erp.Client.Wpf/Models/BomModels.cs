// Models/BomModels.cs (중첩/internal 제거, 모두 public 타입으로 정리)
using System.Collections.Generic;

namespace Erp.Client.Wpf.Models
{
    public record BomLineResponse(
        string BomLineId,
        string BomId,
        string? ParentLineId,
        string ComponentProductId,
        string ComponentCode,
        string ComponentName,
        decimal Qty,
        decimal ScrapRate,
        string? Note
    );

    public class BomTreeNode
    {
        public string BomLineId { get; set; } = "";
        public string? ParentLineId { get; set; }
        public string ComponentProductId { get; set; } = "";
        public string? ComponentCode { get; set; }
        public string? ComponentName { get; set; }
        public decimal Qty { get; set; }
        public decimal ScrapRate { get; set; }
        public string? Note { get; set; }
        public List<BomTreeNode> Children { get; set; } = new();
        public override string ToString()
        {
            var code = string.IsNullOrWhiteSpace(ComponentCode) ? ComponentProductId : ComponentCode;
            var name = string.IsNullOrWhiteSpace(ComponentName) ? "" : $" {ComponentName}";
            return $"{code}{name} (Qty={Qty:0.######}, Scrap={ScrapRate:0.######})";
        }
    }

    public record BomTreeNodeResponse(
        string BomLineId,
        string? ParentLineId,
        string ComponentProductId,
        string? ComponentCode,
        string? ComponentName,
        decimal Qty,
        decimal ScrapRate,
        string? Note,
        List<BomTreeNodeResponse> Children
    );
}