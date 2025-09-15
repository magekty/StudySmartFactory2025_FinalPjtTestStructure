// Services/Endpoints.cs
namespace Erp.Client.Wpf.Services;

public static class Endpoints
{
    public static string Health => "/health";

    // Product
    public static string CreateProduct => "/api/products";

    // Material Cost
    public static string UpsertMaterialCost => "/api/material-costs";
    public static string EffectiveMaterialCost(string productId, string baseDate) =>
        $"/api/material-costs/effective?productId={productId}&baseDate={baseDate}";

    // BOM
    public static string CreateBomHeader(string productId, string revision, bool active, string effectiveFrom) =>
        $"/api/boms?productId={productId}&revision={revision}&active={active}&effectiveFrom={effectiveFrom}";
    public static string BomLines(string bomId) => $"/api/boms/{bomId}/lines";
    public static string CreateBomLine => "/api/boms/lines";
    public static string DeleteBomLine(string lineId) => $"/api/boms/lines/{lineId}";

    // Plan
    public static string CreatePlan(string planCode, string productId, string qty, string startDate, string endDate, string status) =>
        $"/api/plans?planCode={planCode}&productId={productId}&qty={qty}&startDate={startDate}&endDate={endDate}&status={status}";
    public static string ChangePlanStatus(string planId, string nextStatus, string actor) =>
        $"/api/plans/{planId}/status?nextStatus={nextStatus}&actor={actor}";

    // Cost - Preview (GET)
    public static string CostByPlan(string planId, string laborRate, string overheadRate) =>
        $"/api/costs/by-plan/{planId}?laborRate={laborRate}&overheadRate={overheadRate}";
    public static string CostByProduct(string productId, string qty, string laborRate, string overheadRate, string baseDate) =>
        $"/api/costs/by-product/{productId}?qty={qty}&laborRate={laborRate}&overheadRate={overheadRate}&baseDate={baseDate}";

    // Cost - Save (POST)
    public static string SaveCostByProduct => "/api/costs/snapshots/by-product";
    public static string SaveCostByPlan => "/api/costs/snapshots/by-plan";
}