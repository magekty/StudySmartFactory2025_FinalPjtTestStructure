// 0) Domain — 상태/배지/쿼리/PagedResult
// File: Domain/Plans/Status.cs
namespace ErpWpf.App.Domain.Plans;

public enum PlanStatus { P, R, C } // Planned / Running / Completed

[System.Flags]
public enum WorkflowBadge
{
    None = 0,
    Created = 1 << 0,
    Approved = 1 << 1,
    Released = 1 << 2,
    OnHold = 1 << 3,
    Canceled = 1 << 4
}

public sealed record PlanQuery(
    System.DateTimeOffset From,
    System.DateTimeOffset To,
    string? ItemId,
    bool IncludeDeleted,
    int Page = 0,
    int Size = 50,
    string? StatusFilter = null // "P|R|C"
);

public sealed record PagedResult<T>(System.Collections.Generic.IReadOnlyList<T> Items, int Page, int Size, int TotalCount);