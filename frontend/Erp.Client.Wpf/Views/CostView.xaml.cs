// Views/CostView.xaml.cs (DataContext 주입)
using System.Windows.Controls;
using Erp.Client.Wpf.Services;
using Erp.Client.Wpf.ViewModels;

namespace Erp.Client.Wpf.Views
{
    public partial class CostView : UserControl
    {
        public CostView(ApiClient api)
        {
            InitializeComponent();
            DataContext = new CostViewModel(api);
        }
    }
}