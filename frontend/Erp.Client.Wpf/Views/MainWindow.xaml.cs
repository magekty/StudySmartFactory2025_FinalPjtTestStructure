using System;
using System.Collections.Generic;
using System.Threading.Tasks;
using System.Windows;
using Erp.Client.Wpf.Models;
using Erp.Client.Wpf.Services;

namespace Erp.Client.Wpf.Views
{
    public partial class MainWindow : Window
    {
        private readonly ApiClient _api;

        public MainWindow()
        {
            InitializeComponent();

            var json = System.Text.Json.JsonDocument.Parse(System.IO.File.ReadAllText("AppSettings.json")).RootElement;
            var baseUrl = json.GetProperty("ApiBaseUrl").GetString() ?? "http://localhost:8081";
            var timeoutSec = json.TryGetProperty("TimeoutSeconds", out var t) ? t.GetInt32() : 15;
            _api = new ApiClient(baseUrl, TimeSpan.FromSeconds(timeoutSec));

            btnOpenCostWindow.Click += (_, __) => { var w = new CostPage(); w.Owner = this; w.Show(); };
            btnOpenBomWindow.Click += (_, __) => { var w = new BomPage(); w.Owner = this; w.Show(); };

            // 원가 - by product
            btnCalcByProduct.Click += async (_, __) => await CalcByProduct();
            btnSaveByProduct.Click += async (_, __) => await SaveByProduct();

            // 원가 - by plan
            btnCalcByPlan.Click += async (_, __) => await CalcByPlanByPlanId();
            btnSaveByPlan.Click += async (_, __) => await SaveByPlan();

            // BOM
            WireBomHandlers();
        }

        // Preview: by-product (기존 유지)
        private async Task CalcByProduct()
        {
            var pid = prod_ProductId.Text?.Trim();
            if (string.IsNullOrWhiteSpace(pid)) { MessageBox.Show("ProductId는 필수입니다."); return; }
            if (!decimal.TryParse(prod_Qty.Text, out var qty) || qty <= 0) { MessageBox.Show("Qty는 양수여야 합니다."); return; }
            if (!decimal.TryParse(prod_Labor.Text, out var labor) || labor < 0) labor = 0.100000m;
            if (!decimal.TryParse(prod_Oh.Text, out var oh) || oh < 0) oh = 0.100000m;

            var baseDate = (prod_BaseDate.SelectedDate ?? DateTime.UtcNow).ToString("yyyy-MM-dd");
            var path = Endpoints.CostByProduct(pid, qty.ToString("0.######"), labor.ToString("0.######"), oh.ToString("0.######"), baseDate);
            try
            {
                var res = await _api.GetAsync<CostSnapshotResponse>(path);
                gridCostDetails.ItemsSource = res?.Details ?? new List<CostSnapshotDetailResponse>();
            }
            catch (Exception ex) { MessageBox.Show($"by-product 미리보기 실패: {ex.Message}"); }
        }

        // Save: by-product (신규)
        private async Task SaveByProduct()
        {
            var pid = prod_ProductId.Text?.Trim();
            if (string.IsNullOrWhiteSpace(pid)) { MessageBox.Show("ProductId는 필수입니다."); return; }
            if (!decimal.TryParse(prod_Qty.Text, out var qty) || qty <= 0) { MessageBox.Show("Qty는 양수여야 합니다."); return; }
            if (!decimal.TryParse(prod_Labor.Text, out var labor) || labor < 0) labor = 0.100000m;
            if (!decimal.TryParse(prod_Oh.Text, out var oh) || oh < 0) oh = 0.100000m;

            DateOnly? baseDate = null;
            var dp = prod_BaseDate.SelectedDate;
            if (dp.HasValue) baseDate = DateOnly.FromDateTime(dp.Value);

            var body = new SaveCostByProductRequest(
                pid, qty, labor, oh, baseDate, "WPF 저장"
            );

            try
            {
                var res = await _api.PostAsync<CostSnapshotResponse>(Endpoints.SaveCostByProduct, body);
                MessageBox.Show(res?.SnapshotId is not null
                    ? $"저장 성공: {res.SnapshotId}"
                    : "저장 성공(스냅샷 ID 없음?)");
            }
            catch (Exception ex) { MessageBox.Show($"by-product 저장 실패: {ex.Message}"); }
        }

        // Preview: by-plan (기존 유지)
        private async Task CalcByPlanByPlanId()
        {
            var planId = plan_PlanId.Text?.Trim();
            if (string.IsNullOrWhiteSpace(planId)) { MessageBox.Show("PlanId를 입력하세요."); return; }
            if (!decimal.TryParse(plan_Labor.Text, out var labor) || labor < 0) labor = 0.100000m;
            if (!decimal.TryParse(plan_Oh.Text, out var oh) || oh < 0) oh = 0.100000m;

            var path = Endpoints.CostByPlan(planId, labor.ToString("0.######"), oh.ToString("0.######"));
            try
            {
                var res = await _api.GetAsync<CostSnapshotResponse>(path);
                gridCostDetails.ItemsSource = res?.Details ?? new List<CostSnapshotDetailResponse>();
            }
            catch (Exception ex) { MessageBox.Show($"by-plan 미리보기 실패: {ex.Message}"); }
        }

        // Save: by-plan (신규)
        private async Task SaveByPlan()
        {
            var planId = plan_PlanId.Text?.Trim();
            if (string.IsNullOrWhiteSpace(planId)) { MessageBox.Show("PlanId는 필수입니다."); return; }
            if (!decimal.TryParse(plan_Labor.Text, out var labor) || labor < 0) labor = 0.100000m;
            if (!decimal.TryParse(plan_Oh.Text, out var oh) || oh < 0) oh = 0.100000m;

            var body = new SaveCostByPlanRequest(planId, labor, oh, "WPF 저장");
            try
            {
                var res = await _api.PostAsync<CostSnapshotResponse>(Endpoints.SaveCostByPlan, body);
                MessageBox.Show(res?.SnapshotId is not null
                    ? $"저장 성공: {res.SnapshotId}"
                    : "저장 성공(스냅샷 ID 없음?)");
            }
            catch (Exception ex) { MessageBox.Show($"by-plan 저장 실패: {ex.Message}"); }
        }


        // BOM
        public class BomTreeNode
        {
            public string BomLineId { get; set; } = "";
            public string? ParentLineId { get; set; }
            public string ComponentProductId { get; set; } = "";
            public string? ComponentCode { get; set; }
            public string? ComponentName { get; set; }
            public decimal Qty { get; set; }
            public decimal ScrapRate { get; set; }
            public string? Note { get; set; }
            public List<BomTreeNode> Children { get; } = new();
            public override string ToString()
            {
                var code = string.IsNullOrWhiteSpace(ComponentCode) ? ComponentProductId : ComponentCode;
                var name = string.IsNullOrWhiteSpace(ComponentName) ? "" : $" {ComponentName}";
                return $"{code}{name} (Qty={Qty:0.######}, Scrap={ScrapRate:0.######})";
            }
        }

        private List<BomLineResponse> _lastQueriedLines = new();

        private async Task BomQuery()
        {
            var bomId = bom_QueryBomId.Text?.Trim();
            if (string.IsNullOrWhiteSpace(bomId)) { MessageBox.Show("조회 BOM ID는 필수입니다."); return; }
            try
            {
                var res = await _api.GetAsync<List<BomLineResponse>>(Endpoints.BomLines(bomId));
                _lastQueriedLines = res ?? new List<BomLineResponse>();
                bom_Grid.ItemsSource = _lastQueriedLines;
                BuildBomTree(_lastQueriedLines);
            }
            catch (Exception ex) { MessageBox.Show($"BOM 라인 조회 실패: {ex.Message}"); }
        }

        private void BuildBomTree(List<BomLineResponse> lines)
        {
            // parentLineId = null 이 root
            var byParent = new Dictionary<string, List<BomLineResponse>>();
            foreach (var l in lines)
            {
                var key = l.ParentLineId ?? "ROOT";
                if (!byParent.TryGetValue(key, out var list)) byParent[key] = list = new List<BomLineResponse>();
                list.Add(l);
            }

            var roots = new List<BomTreeNode>();
            if (byParent.TryGetValue("ROOT", out var rootLines))
            {
                foreach (var rl in rootLines)
                    roots.Add(BuildNode(rl, byParent));
            }
            bom_Tree.ItemsSource = roots;
        }

        private BomTreeNode BuildNode(BomLineResponse line, Dictionary<string, List<BomLineResponse>> byParent)
        {
            var node = new BomTreeNode
            {
                BomLineId = line.BomLineId,
                ParentLineId = line.ParentLineId,
                ComponentProductId = line.ComponentProductId,
                ComponentCode = line.ComponentCode,
                ComponentName = line.ComponentName,
                Qty = line.Qty,
                ScrapRate = line.ScrapRate,
                Note = line.Note
            };
            if (byParent.TryGetValue(line.BomLineId, out var childs))
            {
                foreach (var c in childs)
                    node.Children.Add(BuildNode(c, byParent));
            }
            return node;
        }

        private void OnBomTreeSelected(object sender, RoutedPropertyChangedEventArgs<object> e)
        {
            if (e.NewValue is BomTreeNode node)
            {
                bom_AddParent.Text = node.BomLineId; // ParentLineId 자동 주입
            }
        }

        private async Task BomAddNew()
        {
            var bomId = bom_AddBomId.Text?.Trim();
            var compId = bom_AddComp.Text?.Trim();
            if (string.IsNullOrWhiteSpace(bomId) || string.IsNullOrWhiteSpace(compId))
            {
                MessageBox.Show("추가 BOM ID / Component ProductId는 필수입니다."); return;
            }
            if (!decimal.TryParse(bom_AddQty.Text, out var qty) || qty <= 0) { MessageBox.Show("Qty는 양수여야 합니다."); return; }
            if (!decimal.TryParse(bom_AddScrap.Text, out var scrap) || scrap < 0) scrap = 0.000000m;

            var req = new BomLineCreateRequest(
                bomId,
                string.IsNullOrWhiteSpace(bom_AddParent.Text) ? null : bom_AddParent.Text.Trim(),
                compId,
                qty,
                scrap,
                string.IsNullOrWhiteSpace(bom_AddNote.Text) ? null : bom_AddNote.Text
            );

            try
            {
                await _api.PostAsync<BomLineResponse>(Endpoints.CreateBomLine, req);
                // 추가 후: 조회 BOM ID와 추가 BOM ID가 다를 수 있으니, 각자 독립 유지.
                // 사용자 편의: 만약 둘이 같다면 자동 새로고침
                if (!string.IsNullOrWhiteSpace(bom_QueryBomId.Text) && bom_QueryBomId.Text.Trim() == bomId)
                    await BomQuery();
                MessageBox.Show("라인 추가 완료");
            }
            catch (Exception ex) { MessageBox.Show($"BOM 라인 추가 실패: {ex.Message}"); }
        }

        // 이벤트 연결
        private void WireBomHandlers()
        {
            btnBomQuery.Click += async (_, __) => await BomQuery();
            btnCopyQueryBomToAdd.Click += (_, __) => { bom_AddBomId.Text = bom_QueryBomId.Text; };
            btnBomAddNew.Click += async (_, __) => await BomAddNew();
        }
    }
}