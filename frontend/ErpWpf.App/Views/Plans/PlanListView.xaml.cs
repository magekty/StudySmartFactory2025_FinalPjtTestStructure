// ErpWpf.App/Views/Plans/PlanListView.xaml.cs
using System;
using System.ComponentModel;
using System.Windows;
using System.Windows.Controls;
using ErpWpf.App.ViewModels.Plans;
using Microsoft.Extensions.DependencyInjection;

namespace ErpWpf.App.Views.Plans;

public partial class PlanListView : UserControl
{
    private readonly PlanListViewModel? _vm;
    private readonly IServiceProvider? _sp;

    public PlanListView()
    {
        InitializeComponent();

        // 디자이너에서는 DI/런타임 의존 로직 건너뜀
        if (DesignerProperties.GetIsInDesignMode(this))
        {
            DataContext = null;
            return;
        }

        _sp = App.HostSvc.Services;
        _vm = ActivatorUtilities.CreateInstance<PlanListViewModel>(_sp);
        DataContext = _vm;

        Loaded += async (_, __) => await _vm!.LoadAsync();
    }

    private async void OnLoad(object sender, RoutedEventArgs e)
    {
        if (_vm is null) return;
        _vm.IncludeDeleted = (ChkDeleted.IsChecked == true);
        _vm.ItemFilter = ItemFilterBox.Text?.Trim() ?? "";
        await _vm.LoadAsync();
    }

    private void SinceChanged(object sender, SelectionChangedEventArgs e)
    {
        if (_vm is null) return;
        if (CboSince.SelectedItem is ComboBoxItem it && int.TryParse(it.Tag?.ToString(), out var hrs))
            _vm.SetSinceHoursCommand.Execute(hrs);
    }

    private void OnNew(object sender, RoutedEventArgs e)
    {
        if (_vm is null) return;
        var dlg = ActivatorUtilities.CreateInstance<PlanEditDialog>(_sp!);
        dlg.Owner = Application.Current.MainWindow;
        if (dlg.ShowDialog() == true) _ = _vm.LoadAsync();
    }

    private void OnEdit(object sender, RoutedEventArgs e)
    {
        if (_vm is null) return;
        if (PlansGrid.SelectedItem is not PlanRow row) return;
        var dlg = ActivatorUtilities.CreateInstance<PlanEditDialog>(_sp!);
        dlg.Owner = Application.Current.MainWindow;
        dlg.LoadFrom(row);
        if (dlg.ShowDialog() == true) _ = _vm.LoadAsync();
    }
}