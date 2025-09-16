namespace Erp.Client.Wpf.Models;

public record BomHeaderResponse(
    string Id,
    string ProductId,
    string Revision,
    bool Active,
    DateOnly EffectiveFrom,
    DateOnly? EffectiveTo,
    string? Note
);

public record BomLineCreateRequest(
    string BomId,
    string? ParentLineId,
    string ComponentProductId,
    decimal Qty,
    decimal ScrapRate,
    string? Note
);
