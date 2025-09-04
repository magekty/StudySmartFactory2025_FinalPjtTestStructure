using ErpWpf.App.ViewModels.Plans;
using System.Windows;

namespace ErpWpf.App.Views.Plans;

public partial class PlanEditDialog : Window
{
    private readonly PlanEditViewModel _vm;

    public PlanEditDialog(ErpApi api, TimeUtil time)
    {
        InitializeComponent(); // ← CS0103 해결: x:Class/Build Action=Page 필수
        _vm = new PlanEditViewModel(api, time);
        DataContext = _vm;
    }

    public void LoadFrom(PlanRow row) => _vm.LoadFrom(row);

    private async void OnSave(object sender, RoutedEventArgs e)
    {
        var ok = await _vm.SaveAsync();
        if (ok) { DialogResult = true; Close(); }
    }

    private void OnCancel(object sender, RoutedEventArgs e)
    {
        DialogResult = false; Close();
    }
}