using System.Collections.ObjectModel;
using System.Windows.Input;
using ERP.Frontend.Models;
using ERP.Frontend.Services;
using System.Threading.Tasks;

namespace ERP.Frontend.ViewModels
{
    public class ProductionPlanViewModel : BaseViewModel
    {
        private ObservableCollection<ProductionPlan> _productionPlans;
        public ObservableCollection<ProductionPlan> ProductionPlans
        {
            get => _productionPlans;
            set => SetProperty(ref _productionPlans, value);
        }

        private readonly ProductionPlanService _productionPlanService = new ProductionPlanService();

        public ICommand LoadPlansCommand { get; }

        public ProductionPlanViewModel()
        {
            LoadPlansCommand = new RelayCommand(async o => await LoadAllPlansAsync());
            LoadAllPlansAsync(); // 뷰모델 생성 시 데이터 로드
        }

        private async Task LoadAllPlansAsync()
        {
            var plans = await _productionPlanService.GetAllProductionPlansAsync();
            ProductionPlans = new ObservableCollection<ProductionPlan>(plans);
        }
    }
}