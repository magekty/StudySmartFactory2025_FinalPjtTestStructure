// Views/BomView.xaml.cs (트리 선택을 VM.SelectedNode로 전달)
using System.Windows;
using System.Windows.Controls;
using Erp.Client.Wpf.Models;
using Erp.Client.Wpf.Services;
using Erp.Client.Wpf.ViewModels;

namespace Erp.Client.Wpf.Views
{
    public partial class BomView : UserControl
    {
        public BomView(ApiClient api)
        {
            InitializeComponent();
            DataContext = new BomViewModel(api);
        }

        private void TreeView_SelectedItemChanged(object sender, RoutedPropertyChangedEventArgs<object> e)
        {
            if (DataContext is BomViewModel vm && e.NewValue is BomTreeNodeResponse node)
                vm.SelectedNode = node;
        }
    }
}