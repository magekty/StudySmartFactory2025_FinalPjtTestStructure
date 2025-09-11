using System.Collections.ObjectModel;
using System.Windows.Input;
using ERP.Frontend.Models;
using ERP.Frontend.Services;
using System.Threading.Tasks;
using System.Numerics;

namespace ERP.Frontend.ViewModels
{
    public class CostViewModel : BaseViewModel
    {
        private ObservableCollection<ProductionPlan> _productionPlans;
        public ObservableCollection<ProductionPlan> ProductionPlans
        {
            get => _productionPlans;
            set => SetProperty(ref _productionPlans, value);
        }

        private ProductionPlan _selectedProductionPlan;
        public ProductionPlan SelectedProductionPlan
        {
            get => _selectedProductionPlan;
            set => SetProperty(ref _selectedProductionPlan, value);
        }

        private decimal _laborCostRate;
        public decimal LaborCostRate
        {
            get => _laborCostRate;
            set => SetProperty(ref _laborCostRate, value);
        }

        private decimal _manufacturingOverheadRate;
        public decimal ManufacturingOverheadRate
        {
            get => _manufacturingOverheadRate;
            set => SetProperty(ref _manufacturingOverheadRate, value);
        }

        private string _totalCostResult;
        public string TotalCostResult
        {
            get => _totalCostResult;
            set => SetProperty(ref _totalCostResult, value);
        }

        private readonly CostService _costService = new CostService();
        private readonly ProductionPlanService _productionPlanService = new ProductionPlanService();

        public ICommand LoadPlansCommand { get; }
        public ICommand CalculateCostCommand { get; }

        public CostViewModel()
        {
            LoadPlansCommand = new RelayCommand(async o => await LoadAllPlansAsync());
            CalculateCostCommand = new RelayCommand(async o => await CalculateCostAsync());
            LoadAllPlansAsync();
        }

        private async Task LoadAllPlansAsync()
        {
            var plans = await _productionPlanService.GetAllProductionPlansAsync();
            ProductionPlans = new ObservableCollection<ProductionPlan>(plans);
        }

        private async Task CalculateCostAsync()
        {
            if (SelectedProductionPlan == null)
            {
                TotalCostResult = "생산 계획을 선택하세요.";
                return;
            }

            try
            {
                var totalCost = await _costService.CalculateCostForPlanAsync(
                    SelectedProductionPlan.PlanId, LaborCostRate, ManufacturingOverheadRate);
                TotalCostResult = $"총 원가: {totalCost:C2}";
            }
            catch (Exception ex)
            {
                TotalCostResult = $"오류 발생: {ex.Message}";
            }
        }
    }
}