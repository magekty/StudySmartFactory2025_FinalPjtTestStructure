// ErpWpf.App/Models/Plans.cs
// PlanDto/PlanPage 유지(PlanPage는 더 이상 직접 역직렬화에 사용하지 않지만, 필요 시 참조용으로 남김)
namespace ErpWpf.App.Models;

public sealed class PlanDto
{
    public string planId { get; set; } = "";
    public int planLineNo { get; set; }
    public string itemId { get; set; } = "";
    public decimal qty { get; set; }
    public string unit { get; set; } = "EA";
    public string dueDateUtc { get; set; } = ""; // "yyyy-MM-ddTHH:mm:ssZ"
    public int? priority { get; set; }
    public bool isDeleted { get; set; }
    public string? updatedAt { get; set; }
}

public sealed class PlanPage
{
    public List<PlanDto> content { get; set; } = new();
    public int page { get; set; }
    public int size { get; set; }
    public bool last { get; set; }
}