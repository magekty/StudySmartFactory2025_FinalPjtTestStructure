namespace Erp.Client.Wpf.Models;

public record CostSnapshotDetailResponse(
    long? SnapshotDetailId,
    string? ComponentProductId,
    string? ComponentCode,
    string? ComponentName,
    int Level,
    decimal BaseQty,
    decimal ScrapRate,
    decimal ExplodedQty,
    decimal UnitCost,
    decimal MaterialCost
);

public record CostSnapshotResponse(
    string? SnapshotId,
    string? PlanId,
    string? ProductId,
    decimal Qty,
    decimal TotalMaterial,
    decimal Labor,
    decimal Overhead,
    decimal TotalCost,
    decimal LaborRate,
    decimal OverheadRate,
    string Method,
    DateTime CalculatedAt,
    string? Note,
    List<CostSnapshotDetailResponse> Details
);

public record SaveCostByProductRequest(
    string ProductId,
    decimal Qty,
    decimal LaborRate,
    decimal OverheadRate,
    DateOnly? BaseDate,
    string? Note
);

public record SaveCostByPlanRequest(
    string PlanId,
    decimal LaborRate,
    decimal OverheadRate,
    string? Note
);