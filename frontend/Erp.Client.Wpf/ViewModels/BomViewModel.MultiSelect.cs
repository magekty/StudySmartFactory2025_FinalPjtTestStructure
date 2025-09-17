// ViewModels/BomViewModel.MultiSelect.cs (부분 확장: 모달 연동 + 일괄 추가)
using System;
using System.Threading.Tasks;
using System.Windows;
using System.Windows.Input;
using Erp.Client.Wpf.Models;
using Erp.Client.Wpf.Services;
using Erp.Client.Wpf.Views;

namespace Erp.Client.Wpf.ViewModels
{
    public partial class BomViewModel
    {
        // 상단 조회 섹션 표시용(품번/품명 레이블)
        private string? _selectedQueryProductName;
        public string? SelectedQueryProductName
        {
            get => _selectedQueryProductName;
            set { _selectedQueryProductName = value; OnChanged(); }
        }


        public ICommand OpenMultiSelectDialogCommand => new RelayCommand(async _ => await OpenMultiSelectDialogAsync(), _ => !IsBusy);

        private async Task OpenMultiSelectDialogAsync()
        {
            var dlg = new ProductMultiSelectDialog(_api)
            {
                Owner = Application.Current?.MainWindow
            };
            var ok = dlg.ShowDialog();
            if (ok != true || dlg.SelectedItems.Count == 0) return;

            var parentId = SelectedNode?.BomLineId; // 없으면 루트
            var added = 0; var failed = 0;

            IsBusy = true;
            try
            {
                foreach (var p in dlg.SelectedItems)
                {
                    try
                    {
                        var req = new BomLineCreateRequest(
                            AddBomId!.Trim(),
                            string.IsNullOrWhiteSpace(parentId) ? null : parentId,
                            p.productId, // 화면엔 숨김, 서버 전송용
                            decimal.Parse(AddQty),
                            decimal.TryParse(AddScrapRate, out var s) ? s : 0m,
                            string.IsNullOrWhiteSpace(AddNote) ? null : AddNote
                        );
                        await _api.PostAsync<BomLineResponse>(Endpoints.CreateBomLine, req);
                        added++;
                    }
                    catch
                    {
                        failed++;
                    }
                }

                if (!string.IsNullOrWhiteSpace(QueryBomId))
                    await QueryAsync();

                MessageBox.Show($"추가 완료: 성공 {added}건 / 실패 {failed}건");
            }
            finally
            {
                IsBusy = false;
            }
        }
    }
}