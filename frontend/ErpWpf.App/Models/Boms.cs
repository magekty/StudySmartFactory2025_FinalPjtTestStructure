namespace ErpWpf.App.Models.Boms;

public sealed record BomHeaderDto(string ItemId, string Revision, string Alt, System.DateTimeOffset UpdatedAt);

public sealed record BomLineDto(
    int LineNo,
    string ComponentId,
    decimal QtyPer,
    string Uom,
    decimal ScrapRate, // 0.0 ~ 1.0
    bool IsDeleted,
    bool IsUnknown
);

public sealed record BomDetailDto(BomHeaderDto Header, System.Collections.Generic.IReadOnlyList<BomLineDto> Lines);

