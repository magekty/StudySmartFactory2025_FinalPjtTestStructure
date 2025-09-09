// 2) Ports — 외부 연동 인터페이스(ERP 저장소 / MES 보조)
// File: Ports/Ports.cs
using ErpWpf.App.Domain.Plans;
using ErpWpf.App.Models;
using ErpWpf.App.Models.Boms;
using ErpWpf.App.Models.Inventory;
using System.Collections.Generic;
using System.Threading;
using System.Threading.Tasks;

namespace ErpWpf.App.Ports;

// 기존 ErpApi.* 구현체를 이 인터페이스로 바인딩하면 됨
public interface IPlanRepository
{
    Task<PagedResult<PlanListItem>> GetPlansAsync(PlanQuery query, CancellationToken ct);
    Task<PlanDetailDto> GetPlanAsync(string planId, CancellationToken ct);
    Task<string> CreateAsync(PlanDetailDto plan, CancellationToken ct);
    Task UpdateAsync(PlanDetailDto plan, CancellationToken ct);
    Task SoftDeleteAsync(string planId, string reason, CancellationToken ct);
}

// MES 보조 의존
public sealed record PlanWoStatuses(string PlanId, IReadOnlyList<string> WoStatuses);

public interface IMesWoStatusClient
{
    Task<IReadOnlyList<PlanWoStatuses>> GetStatusesAsync(IEnumerable<string> planIds, CancellationToken ct);
}

public interface IMesBomClient
{
    Task<BomDetailDto> GetBomByItemAsync(string itemId, string? rev, string? alt, CancellationToken ct);
}

public interface IMesInventoryClient
{
    Task<AvailabilityDto> GetAvailabilityAsync(string itemId, System.DateTimeOffset at, CancellationToken ct);
}