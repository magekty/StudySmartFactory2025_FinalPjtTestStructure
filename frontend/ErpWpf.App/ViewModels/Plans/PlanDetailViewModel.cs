// 10) ViewModels — 상세 VM(BOM/재고/부족)
// File: ViewModels/Plans/PlanDetailViewModel.cs
using ErpWpf.App.Models;
using ErpWpf.App.Models.Boms;
using ErpWpf.App.Models.Inventory;
using ErpWpf.App.Ports;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading;
using System.Threading.Tasks;

namespace ErpWpf.App.ViewModels.Plans;

public sealed class ShortageRow
{
    public required string ComponentId { get; init; }
    public required decimal ReqQty { get; init; }
    public required decimal Avail { get; init; }
    public decimal Shortage => Avail - ReqQty; // 음수면 부족
    public string Uom { get; init; } = "EA";
}

public sealed class PlanDetailViewModel
{
    private readonly IMesBomClient _bom;
    private readonly IMesInventoryClient _inv;

    public PlanDetailDto? Header { get; private set; }
    public BomDetailDto? Bom { get; private set; }
    public decimal? ReadyRatio { get; private set; }
    public IReadOnlyList<ShortageRow> Shortages { get; private set; } = Array.Empty<ShortageRow>();

    public PlanDetailViewModel(IMesBomClient bom, IMesInventoryClient inv)
    { _bom = bom; _inv = inv; }

    public async Task LoadAsync(PlanDetailDto plan, CancellationToken ct)
    {
        Header = plan;
        var bom = await _bom.GetBomByItemAsync(plan.ItemId, plan.BomRev, plan.BomAlt, ct);
        Bom = bom;

        var rows = new List<ShortageRow>();
        var readyRatios = new List<decimal>();

        foreach (var line in bom.Lines.Where(l => !l.IsDeleted && !l.IsUnknown))
        {
            var req = line.ScrapRate >= 1m ? decimal.MaxValue : (line.QtyPer * plan.Qty) / (1m - line.ScrapRate);
            AvailabilityDto av = await _inv.GetAvailabilityAsync(line.ComponentId, DateTimeOffset.UtcNow, ct);

            rows.Add(new ShortageRow
            {
                ComponentId = line.ComponentId,
                ReqQty = Math.Round(req, 3, MidpointRounding.AwayFromZero),
                Avail = Math.Round(av.Available, 3, MidpointRounding.AwayFromZero),
                Uom = line.Uom
            });

            if (req > 0) readyRatios.Add(av.Available / req);
        }

        Shortages = rows.OrderBy(r => r.Shortage).ToList();
        ReadyRatio = readyRatios.Count > 0 ? Math.Clamp(readyRatios.Min(), 0m, 1m) : null;
    }
}