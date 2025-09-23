// Views/ProductSingleSelectDialog.xaml.cs
using System.Collections.Generic;
using System.Threading.Tasks;
using System.Windows;
using System.Windows.Controls;
using Erp.Client.Wpf.Services;

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
            // 엔드포인트는 프로젝트에 맞춰 변경
            var url = $"/api/products?q={System.Uri.EscapeDataString(q)}&status={status}&page=0&size=50&sort=productCode,asc";

            var res = await _api.GetAsync<Paged<ProductLite>>(url);
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

    public class Paged<T>
    {
        public List<T> content { get; set; } = new();
        public int totalPages { get; set; }
        public long totalElements { get; set; }
        public int size { get; set; }
        public int number { get; set; }
    }
}