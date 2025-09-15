namespace Erp.Client.Wpf.Models;

public record ProductCreateRequest(
    string ProductCode,
    string Name,
    string Type,
    string Unit,
    string? Description
);

public record ProductResponse(
    string Id,
    string ProductCode,
    string Name,
    string Type,
    string Unit,
    string? Description
);