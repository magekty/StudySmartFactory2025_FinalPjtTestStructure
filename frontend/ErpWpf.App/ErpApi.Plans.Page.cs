// ErpWpf.App/ErpApi.Plans.Page.cs
using ErpWpf.App.Models;
using System.Net.Http.Json;
using System.Text.Json;
using System.Threading;
using System.Threading.Tasks;

namespace ErpWpf.App;

public sealed partial class ErpApi
{
    public async Task<(List<PlanDto> items, bool last)> GetPlansPageAsync(
        string updatedSinceIsoUtc, int page, int size, CancellationToken ct = default)
    {
        var http = _f.CreateClient("erp");
        var url = $"/plans?updatedSince={Uri.EscapeDataString(updatedSinceIsoUtc)}&page={page}&size={size}";
        using var res = await http.GetAsync(url, ct);
        res.EnsureSuccessStatusCode();

        await using var stream = await res.Content.ReadAsStreamAsync(ct);
        using var doc = await JsonDocument.ParseAsync(stream, cancellationToken: ct);
        var root = doc.RootElement;

        // 1) 배열 응답: [ {...}, {...} ]
        if (root.ValueKind == JsonValueKind.Array)
        {
            var list = new List<PlanDto>(root.GetArrayLength());
            foreach (var e in root.EnumerateArray()) list.Add(MapPlan(e));
            bool isLast = list.Count < size; // 보수적 추정
            return (list, isLast);
        }

        // 2) 객체 응답: { content:[...], last:true } 또는 단일 오브젝트
        if (root.ValueKind == JsonValueKind.Object)
        {
            // 페이지 객체
            if (root.TryGetProperty("content", out var content) && content.ValueKind == JsonValueKind.Array)
            {
                var list = new List<PlanDto>(content.GetArrayLength());
                foreach (var e in content.EnumerateArray()) list.Add(MapPlan(e));
                bool isLast = root.TryGetProperty("last", out var lp) && lp.ValueKind == JsonValueKind.True;
                if (!root.TryGetProperty("last", out _)) isLast = list.Count < size;
                return (list, isLast);
            }
            // 단일 오브젝트
            return (new List<PlanDto> { MapPlan(root) }, true);
        }

        throw new InvalidOperationException("Unexpected /plans JSON shape.");
    }

    // 내부 snake_case/camelCase 혼용까지 안전 매핑
    private static PlanDto MapPlan(JsonElement e)
    {
        static bool S(JsonElement o, out string? v, params string[] n) { foreach (var x in n) { if (o.TryGetProperty(x, out var p) && p.ValueKind == JsonValueKind.String) { v = p.GetString(); return true; } } v = null; return false; }
        static bool I(JsonElement o, out int v, params string[] n) { foreach (var x in n) { if (o.TryGetProperty(x, out var p)) { if (p.ValueKind == JsonValueKind.Number && p.TryGetInt32(out var i)) { v = i; return true; } if (p.ValueKind == JsonValueKind.String && int.TryParse(p.GetString(), out var s)) { v = s; return true; } } } v = 0; return false; }
        static bool D(JsonElement o, out decimal v, params string[] n) { foreach (var x in n) { if (o.TryGetProperty(x, out var p)) { if (p.ValueKind == JsonValueKind.Number && p.TryGetDecimal(out var d)) { v = d; return true; } if (p.ValueKind == JsonValueKind.String && decimal.TryParse(p.GetString(), out var s)) { v = s; return true; } } } v = 0m; return false; }
        static bool B(JsonElement o, out bool v, params string[] n) { foreach (var x in n) { if (o.TryGetProperty(x, out var p)) { if (p.ValueKind == JsonValueKind.True) { v = true; return true; } if (p.ValueKind == JsonValueKind.False) { v = false; return true; } if (p.ValueKind == JsonValueKind.String && bool.TryParse(p.GetString(), out var b)) { v = b; return true; } } } v = false; return false; }

        var dto = new PlanDto();
        if (S(e, out var s, "planId", "plan_id")) dto.planId = s!;
        if (I(e, out var i, "planLineNo", "plan_line_no")) dto.planLineNo = i;
        if (S(e, out s, "itemId", "item_id")) dto.itemId = s!;
        if (D(e, out var dec, "qty")) dto.qty = dec;
        if (S(e, out s, "unit")) dto.unit = s ?? "EA";
        if (S(e, out s, "dueDateUtc", "due_date_utc")) dto.dueDateUtc = s ?? "";
        if (I(e, out i, "priority")) dto.priority = i;
        if (B(e, out var b, "isDeleted", "is_deleted")) dto.isDeleted = b;
        if (S(e, out s, "updatedAt", "updated_at")) dto.updatedAt = s;
        return dto;
    }
}