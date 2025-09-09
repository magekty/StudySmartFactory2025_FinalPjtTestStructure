// 5) Services — 상태 파생 로직
// File: Services/StatusDeriver.cs
using System;
using System.Collections.Generic;
using System.Linq;
using ErpWpf.App.Domain.Plans;

namespace ErpWpf.App.Services;

public static class StatusDeriver
{
    public static PlanStatus DeriveMainStatus(IReadOnlyList<string> woStatuses)
    {
        if (woStatuses is null || woStatuses.Count == 0) return PlanStatus.P;

        bool anyRunning = woStatuses.Any(s =>
            s.Equals("RUNNING", StringComparison.OrdinalIgnoreCase) ||
            s.Equals("RELEASED", StringComparison.OrdinalIgnoreCase) ||
            s.Equals("EXECUTING", StringComparison.OrdinalIgnoreCase));

        bool allCompleted = woStatuses.All(s =>
            s.Equals("COMPLETED", StringComparison.OrdinalIgnoreCase) ||
            s.Equals("CLOSED", StringComparison.OrdinalIgnoreCase));

        if (allCompleted) return PlanStatus.C;
        if (anyRunning) return PlanStatus.R;
        return PlanStatus.P;
    }

    public static WorkflowBadge NormalizeWorkflowBadges(IReadOnlyList<string> wfStates)
    {
        WorkflowBadge b = WorkflowBadge.None;
        foreach (var s in wfStates)
        {
            if (s.Equals("CREATED", StringComparison.OrdinalIgnoreCase)) b |= WorkflowBadge.Created;
            else if (s.Equals("APPROVED", StringComparison.OrdinalIgnoreCase)) b |= WorkflowBadge.Approved;
            else if (s.Equals("RELEASED", StringComparison.OrdinalIgnoreCase)) b |= WorkflowBadge.Released;
            else if (s.Equals("ON_HOLD", StringComparison.OrdinalIgnoreCase) || s.Equals("HOLD", StringComparison.OrdinalIgnoreCase)) b |= WorkflowBadge.OnHold;
            else if (s.Equals("CANCELED", StringComparison.OrdinalIgnoreCase) || s.Equals("CANCELLED", StringComparison.OrdinalIgnoreCase)) b |= WorkflowBadge.Canceled;
        }
        return b;
    }
}