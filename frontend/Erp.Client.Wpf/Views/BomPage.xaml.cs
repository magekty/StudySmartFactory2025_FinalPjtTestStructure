using System;
using System.Linq;
using System.Windows;
using System.Windows.Controls;
using Erp.Client.Wpf.Models;
using Erp.Client.Wpf.Services;
using Erp.Client.Wpf.ViewModels;

namespace Erp.Client.Wpf.Views;

public partial class BomPage : Window
{
    private readonly BomViewModel _vm;

    public BomPage()
    {
        InitializeComponent();

        // AppSettings.json에서 ApiBaseUrl 로드
        var json = System.Text.Json.JsonDocument.Parse(System.IO.File.ReadAllText("AppSettings.json")).RootElement;
        var baseUrl = json.GetProperty("ApiBaseUrl").GetString() ?? "http://localhost:8081";
        var timeoutSec = json.TryGetProperty("TimeoutSeconds", out var t) ? t.GetInt32() : 15;

        var api = new ApiClient(baseUrl, TimeSpan.FromSeconds(timeoutSec));
        _vm = new BomViewModel(api);
        DataContext = _vm;

        btnLoad.Click += async (_, __) =>
        {
            _vm.BomId = txtBomId.Text?.Trim();
            await _vm.LoadLines();
        };

        btnAdd.Click += async (_, __) =>
        {
            _vm.BomId = txtBomId.Text?.Trim();
            _vm.ParentLineId = string.IsNullOrWhiteSpace(txtParent.Text) ? null : txtParent.Text.Trim();
            _vm.ComponentProductId = txtComp.Text?.Trim();
            if (!decimal.TryParse(txtQty.Text, out var qty)) qty = 1.000000m;
            if (!decimal.TryParse(txtScrap.Text, out var scrap)) scrap = 0.000000m;
            _vm.Qty = qty;
            _vm.ScrapRate = scrap;
            _vm.Note = txtNote.Text;

            await _vm.AddLine();
        };
    }

    private async void OnDeleteLineClick(object sender, RoutedEventArgs e)
    {
        if (gridLines.SelectedItem is BomLineResponse selected)
        {
            var confirm = MessageBox.Show("선택한 라인을 삭제할까요?", "확인", MessageBoxButton.YesNo, MessageBoxImage.Question);
            if (confirm == MessageBoxResult.Yes)
            {
                await _vm.DeleteLine(selected);
            }
        }
    }
}