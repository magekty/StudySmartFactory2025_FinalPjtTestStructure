using System.Windows;
using Erp.Client.Wpf.Services;
using Erp.Client.Wpf.ViewModels;

namespace Erp.Client.Wpf.Views;

public partial class CostPage : Window
{
    private readonly CostViewModel _vm;

    public CostPage()
    {
        InitializeComponent();
        var baseUrl = System.Text.Json.JsonDocument.Parse(System.IO.File.ReadAllText("AppSettings.json"))
            .RootElement.GetProperty("ApiBaseUrl").GetString()!;
        var timeoutSec = 15;
        var api = new ApiClient(baseUrl, TimeSpan.FromSeconds(timeoutSec));
        _vm = new CostViewModel(api);
        DataContext = _vm;

        btnByProduct.Click += async (_, __) =>
        {
            _vm.ProductId = txtProduct.Text?.Trim();
            _vm.Qty = decimal.Parse(txtQty.Text);
            _vm.LaborRate = decimal.Parse(txtLabor.Text);
            _vm.OverheadRate = decimal.Parse(txtOh.Text);
            await _vm.CalcByProduct();
        };

        btnByPlan.Click += async (_, __) =>
        {
            _vm.PlanId = txtPlan.Text?.Trim();
            _vm.LaborRate = decimal.Parse(txtLabor.Text);
            _vm.OverheadRate = decimal.Parse(txtOh.Text);
            await _vm.CalcByPlan();
        };
    }
}