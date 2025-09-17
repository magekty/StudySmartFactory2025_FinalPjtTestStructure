// Views/ProductMultiSelectDialog.xaml.cs
using System;
using System.Linq;
using System.Windows;
using System.Windows.Controls;
using Erp.Client.Wpf.Models;
using Erp.Client.Wpf.Services;
using System.Collections.Generic;

namespace Erp.Client.Wpf.Views
{
    public partial class ProductMultiSelectDialog : Window
    {
        private readonly ApiClient _api;
        public List<ProductListItem> SelectedItems { get; } = new();

        public ProductMultiSelectDialog(ApiClient api)
        {
            InitializeComponent();
            _api = api;

            btnSearch.Click += async (_, __) => await DoSearchAsync();
            btnApply.Click += (_, __) => ApplySelected();

            Loaded += async (_, __) => await DoSearchAsync();
        }

        private async System.Threading.Tasks.Task DoSearchAsync()
        {
            var q = tbQuery.Text?.Trim() ?? "";
            var status = ((ComboBoxItem)cbStatus.SelectedItem)?.Tag?.ToString() ?? "ALL";
            var page = 0; var size = 100;
            var url = $"/api/products/search?q={Uri.EscapeDataString(q)}&status={status}&page={page}&size={size}&sort=productCode,asc";
            try
            {
                var res = await _api.GetAsync<Paged<ProductListItem>>(url);
                grid.ItemsSource = (IEnumerable<ProductListItem>)(res?.content ?? new List<ProductListItem>());
                txtInfo.Text = $"총 {res?.totalElements ?? 0}건";
            }
            catch (Exception ex)
            {
                MessageBox.Show($"검색 실패: {ex.Message}");
            }
        }

        private void ApplySelected()
        {
            var sels = grid.SelectedItems.Cast<ProductListItem>().ToList();
            if (sels.Count == 0)
            {
                MessageBox.Show("선택된 품목이 없습니다.");
                return;
            }

            var inactiveCnt = sels.Count(x => !x.active);
            if (inactiveCnt > 0)
            {
                var r = MessageBox.Show($"비활성 품목 {inactiveCnt}건 포함. 계속할까요?", "확인", MessageBoxButton.YesNo, MessageBoxImage.Warning);
                if (r != MessageBoxResult.Yes) return;
            }
            if (sels.Count >= 200)
            {
                var r = MessageBox.Show($"선택 {sels.Count}건. 추가에 시간이 걸릴 수 있습니다. 계속할까요?", "확인", MessageBoxButton.YesNo, MessageBoxImage.Question);
                if (r != MessageBoxResult.Yes) return;
            }

            SelectedItems.Clear();
            SelectedItems.AddRange(sels);
            DialogResult = true;
        }
    }
}