using Erp.Client.Wpf.Models;
using Erp.Client.Wpf.Services;
using System;
using System.Collections.Generic;
using System.Threading.Tasks;
using System.Windows;
using System.Windows.Controls;

namespace Erp.Client.Wpf.Views
{
    public partial class MainWindow : Window
    {
        private readonly ApiClient _api;
        private List<BomLineResponse> _flat = new();

        public MainWindow()
        {
            InitializeComponent();

            var json = System.Text.Json.JsonDocument.Parse(System.IO.File.ReadAllText("AppSettings.json")).RootElement;
            var baseUrl = json.GetProperty("ApiBaseUrl").GetString() ?? "http://localhost:8081";
            var timeoutSec = json.TryGetProperty("TimeoutSeconds", out var t) ? t.GetInt32() : 15;
            _api = new ApiClient(baseUrl, TimeSpan.FromSeconds(timeoutSec));

            btnOpenCostWindow.Click += (_, __) => { var w = new CostPage(); w.Owner = this; w.Show(); };

            // 원가 - by product
            btnCalcByProduct.Click += async (_, __) => await CalcByProduct();
            btnSaveByProduct.Click += async (_, __) => await SaveByProduct();

            // 원가 - by plan
            btnCalcByPlan.Click += async (_, __) => await CalcByPlanByPlanId();
            btnSaveByPlan.Click += async (_, __) => await SaveByPlan();

            // BOM
            bomHost.Content = new BomView(_api);
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
      

    }
}