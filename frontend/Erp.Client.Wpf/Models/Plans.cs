namespace Erp.Client.Wpf.Models;

public record PlanCreateResponse(
    string Id,
    string PlanCode,
    string ProductId,
    decimal Qty,
    DateOnly StartDate,
    DateOnly EndDate,
    string Status,
    string? Note
);

public record PlanStatusResponse(
    string PlanId,
    string PlanCode,
    string ProductId,
    string Status,
    string? Note,
    DateTime ModifiedAt,
    string ModifiedBy
);