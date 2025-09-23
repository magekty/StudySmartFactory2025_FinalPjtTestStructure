using Erp.Client.Wpf.Services;
using System;
using System.Windows;

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

            // 생산계획
            planHost.Content = new ProductionPlanView(_api);

            // 원가 - by plan (기존 유지)
            costHost.Content = new CostView(_api);

            // BOM (기존 유지)
            bomHost.Content = new BomView(_api);
        }
    }
}