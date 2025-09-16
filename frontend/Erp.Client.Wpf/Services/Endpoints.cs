// Services/Endpoints.cs
namespace Erp.Client.Wpf.Services;

public static class Endpoints
{
    // Health
    public static string Health => "/health";

    // Product
    public static string CreateProduct => "/api/products";
    public static string GetProduct(string productId) => $"/api/products/{productId}";
    public static string SearchProducts(string keyword) => $"/api/products?keyword={Uri.EscapeDataString(keyword)}";

    // Material Cost
    public static string UpsertMaterialCost => "/api/material-costs";
    public static string EffectiveMaterialCost(string productId, string baseDate) =>
        $"/api/material-costs/effective?productId={productId}&baseDate={baseDate}";
    public static string GetMaterialCosts(string productId) => $"/api/material-costs?productId={productId}";

    // BOM Header
    public static string CreateBomHeader(string productId, string revision, bool active, string effectiveFrom) =>
        $"/api/boms?productId={productId}&revision={Uri.EscapeDataString(revision)}&active={active}&effectiveFrom={effectiveFrom}";
    public static string GetBomHeader(string bomId) => $"/api/boms/{bomId}";
    public static string GetEffectiveBomHeader(string productId, string baseDate) =>
        $"/api/boms/effective?productId={productId}&baseDate={baseDate}";

    // BOM Lines
    public static string BomLines(string bomId) => $"/api/boms/{bomId}/lines";
    public static string BomTree(string bomId) => $"/api/boms/{bomId}/tree";             // 트리 조회(신규)
    public static string CreateBomLine => "/api/boms/lines";
    public static string DeleteBomLine(string lineId) => $"/api/boms/lines/{lineId}";

    // Plan
    public static string CreatePlan(string planCode, string productId, string qty, string startDate, string endDate, string status) =>
        $"/api/plans?planCode={Uri.EscapeDataString(planCode)}&productId={productId}&qty={qty}&startDate={startDate}&endDate={endDate}&status={Uri.EscapeDataString(status)}";
    public static string GetPlan(string planId) => $"/api/plans/{planId}";
    public static string ChangePlanStatus(string planId, string nextStatus, string actor) =>
        $"/api/plans/{planId}/status?nextStatus={Uri.EscapeDataString(nextStatus)}&actor={Uri.EscapeDataString(actor)}";

    // Cost - Preview (GET)
    public static string CostByPlan(string planId, string laborRate, string overheadRate) =>
        $"/api/costs/by-plan/{planId}?laborRate={laborRate}&overheadRate={overheadRate}";
    public static string CostByProduct(string productId, string qty, string laborRate, string overheadRate, string baseDate) =>
        $"/api/costs/by-product/{productId}?qty={qty}&laborRate={laborRate}&overheadRate={overheadRate}&baseDate={baseDate}";

    // Cost - Save (POST)
    public static string SaveCostByProduct => "/api/costs/snapshots/by-product";
    public static string SaveCostByPlan => "/api/costs/snapshots/by-plan";
}