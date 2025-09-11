using System.Windows.Input;
using System.Windows.Controls;
using ERP.Frontend.Views;

namespace ERP.Frontend.ViewModels
{
    public class MainViewModel : BaseViewModel
    {
        private UserControl _currentViewModel;
        public UserControl CurrentViewModel
        {
            get => _currentViewModel;
            set => SetProperty(ref _currentViewModel, value);
        }

        public ICommand ShowBomViewCommand { get; }
        public ICommand ShowProductionPlanViewCommand { get; }
        public ICommand ShowCostViewCommand { get; }

        public MainViewModel()
        {
            ShowBomViewCommand = new RelayCommand(o => CurrentViewModel = new BomView());
            ShowProductionPlanViewCommand = new RelayCommand(o => CurrentViewModel = new ProductionPlanView());
            ShowCostViewCommand = new RelayCommand(o => CurrentViewModel = new CostView());

            // 애플리케이션 시작 시 기본 뷰 설정
            CurrentViewModel = new BomView();
        }
    }

    // 간단한 ICommand 구현체 (RelayCommand.cs)
    public class RelayCommand : ICommand
    {
        private readonly Action<object> _execute;
        private readonly Func<object, bool> _canExecute;

        public event EventHandler CanExecuteChanged
        {
            add => CommandManager.RequerySuggested += value;
            remove => CommandManager.RequerySuggested -= value;
        }

        public RelayCommand(Action<object> execute, Func<object, bool> canExecute = null)
        {
            _execute = execute ?? throw new ArgumentNullException(nameof(execute));
            _canExecute = canExecute;
        }

        public bool CanExecute(object parameter) => _canExecute == null || _canExecute(parameter);
        public void Execute(object parameter) => _execute(parameter);
    }
}