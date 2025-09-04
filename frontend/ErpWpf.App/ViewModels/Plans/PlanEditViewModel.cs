using CommunityToolkit.Mvvm.ComponentModel;
using CommunityToolkit.Mvvm.Input;
using ErpWpf.App.Models;
using System.Net.Http.Json;
using System.Windows;

namespace ErpWpf.App.ViewModels.Plans;

public partial class PlanEditViewModel : ObservableObject
{
    private readonly ErpApi _api;
    private readonly TimeUtil _time;

    [ObservableProperty] private bool _isNew = true;

    [ObservableProperty] private string _planId = "";
    [ObservableProperty] private int _planLineNo;
    [ObservableProperty] private string _itemId = "";
    [ObservableProperty] private decimal _qty;
    [ObservableProperty] private string _unit = "EA";
    [ObservableProperty] private DateTime _dueLocalDate = DateTime.Now.Date;
    [ObservableProperty] private string _dueLocalTimeText = "09:00"; // HH:mm
    [ObservableProperty] private int? _priority;
    [ObservableProperty] private bool _isDeleted;

    public PlanEditViewModel(ErpApi api, TimeUtil time)
    {
        _api = api;
        _time = time;
    }

    public void LoadFrom(PlanRow row)
    {
        IsNew = false;
        PlanId = row.PlanId;
        PlanLineNo = row.PlanLineNo;
        ItemId = row.ItemId;
        Qty = row.Qty;
        Unit = row.Unit;

        if (DateTimeOffset.TryParse(row.DueDateUtc, out var odt))
        {
            var local = odt.ToLocalTime();
            DueLocalDate = local.Date;
            DueLocalTimeText = local.ToString("HH:mm");
        }
        Priority = row.Priority;
        IsDeleted = row.IsDeleted;
    }

    [RelayCommand]
    public async Task<bool> SaveAsync()
    {
        // 로컬 date + time 텍스트 결합 → UTC ISO
        if (!TimeSpan.TryParse(DueLocalTimeText, out var ts)) ts = TimeSpan.Zero;
        var local = new DateTime(DueLocalDate.Year, DueLocalDate.Month, DueLocalDate.Day, ts.Hours, ts.Minutes, 0, DateTimeKind.Local);
        var dto = new PlanDto
        {
            planId = PlanId.Trim(),
            planLineNo = PlanLineNo,
            itemId = ItemId.Trim(),
            qty = Qty,
            unit = string.IsNullOrWhiteSpace(Unit) ? "EA" : Unit.Trim(),
            dueDateUtc = _time.ToUtcIso(new DateTimeOffset(local)),
            priority = Priority,
            isDeleted = IsDeleted
        };

        var res = IsNew ? await _api.CreatePlanAsync(dto) : await _api.UpdatePlanAsync(dto);
        if (res.IsSuccessStatusCode) return true;

        var err = await res.Content.ReadFromJsonAsync<StdError>();
        var msg = err != null ? $"{err.code}: {err.message}" : await res.Content.ReadAsStringAsync();
        System.Windows.MessageBox.Show(msg, "오류", MessageBoxButton.OK, MessageBoxImage.Warning);
        return false;
    }
}