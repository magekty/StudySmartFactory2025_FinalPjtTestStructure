// src/Dtos.cs
public record PlanUpsertDto(string PlanId, int PlanLineNo, string ItemId, decimal Qty, DateTimeOffset DueDateUtc, int? Priority, string IdempotencyKey);
public record ItemQueryDto(DateTimeOffset? UpdatedSince);
public record BomQueryDto(DateTimeOffset? UpdatedSince);

public record WorkOrderCreateDto(string WorkOrderId, string WorkOrderNumber, string ItemId, decimal Qty, string Status, DateTimeOffset? StartTs, string IdempotencyKey);
public record WorkOrderStatusDto(string Status, DateTimeOffset ChangedAt, string IdempotencyKey);

public record PerformanceCreateDto(string WorkOrderId, string ItemId, string ProcessId, string EquipmentId, decimal GoodQty, decimal DefectQty, DateTimeOffset StartTime, DateTimeOffset EndTime, string IdempotencyKey);

public record BackflushDto(string WorkOrderId, IEnumerable<BackflushLine> Lines, string IdempotencyKey);
public record BackflushLine(string ComponentId, decimal Qty, string Uom);