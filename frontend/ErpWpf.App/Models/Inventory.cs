namespace ErpWpf.App.Models.Inventory;

public sealed record AvailabilityDto(
    string ItemId,
    decimal OnHand,
    decimal Reserved,
    decimal Inbound,
    decimal Available // OnHand - Reserved + Inbound
);