// Views/ProductionPlanView.xaml.cs
using System.Windows.Controls;
using Erp.Client.Wpf.Services;
using Erp.Client.Wpf.ViewModels;

namespace Erp.Client.Wpf.Views
{
    public partial class ProductionPlanView : UserControl
    {
        public ProductionPlanView(ApiClient api)
        {
            InitializeComponent();
            DataContext = new ProductionPlanViewModel(api);
        }
    }
}