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