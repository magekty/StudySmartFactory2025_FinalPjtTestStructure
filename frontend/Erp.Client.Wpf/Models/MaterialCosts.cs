namespace Erp.Client.Wpf.Models;

public record MaterialCostUpsertRequest(
    string ProductId,
    decimal StdCost,
    DateOnly EffectiveFrom,
    DateOnly? EffectiveTo
);

public record MaterialCost(
    string Id,
    string ProductId,
    string Currency,
    decimal StdCost,
    DateOnly EffectiveFrom,
    DateOnly? EffectiveTo
);