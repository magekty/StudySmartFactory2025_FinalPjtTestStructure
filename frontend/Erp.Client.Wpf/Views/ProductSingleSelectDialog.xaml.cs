// Views/ProductSingleSelectDialog.xaml.cs
using Erp.Client.Wpf.Models;
using Erp.Client.Wpf.Services;
using System.Collections.Generic;
using System.Drawing;
using System.Threading.Tasks;
using System.Windows;
using System.Windows.Controls;
using System.Windows.Input;

namespace Erp.Client.Wpf.Views
{
    public partial class ProductSingleSelectDialog : Window
    {
        private readonly ApiClient _api;
        public ProductSingleSelectDialog(ApiClient api)
        {
            InitializeComponent();
            _api = api;

            btnSearch.Click += async (_, __) => await SearchAsync();
            btnOk.Click += (_, __) => {
                if (grid.SelectedItem is ProductLite vm) { SelectedItem = vm; DialogResult = true; }
                else MessageBox.Show("품목을 선택하세요.");
            };
        }

        public ProductLite? SelectedItem { get; private set; }

        private async Task SearchAsync()
        {
            var q = tbQuery.Text?.Trim() ?? "";
            var status = ((ComboBoxItem)cbStatus.SelectedItem)?.Tag?.ToString() ?? "ALL";
            var page = 0; var size = 100;
            // 엔드포인트는 프로젝트에 맞춰 변경
            var url = $"/api/products/search?q={Uri.EscapeDataString(q)}&status={status}&page={page}&size={size}&sort=productCode,asc";

            var res = await _api.GetAsync<Models.Paged<ProductLite>>(url);
            grid.ItemsSource = res?.content ?? new List<ProductLite>();
        }
    }

    // 가벼운 목록용 DTO
    public class ProductLite
    {
        public string productId { get; set; } = "";
        public string productCode { get; set; } = "";
        public string name { get; set; } = "";
        public string type { get; set; } = "";
        public string unit { get; set; } = "";
    }


}