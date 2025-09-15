using System.Windows;
using System.Windows.Controls;

namespace Erp.Client.Wpf.Utils;

public static class Spacing
{
    public static readonly DependencyProperty HorizontalSpacingProperty =
        DependencyProperty.RegisterAttached(
            "HorizontalSpacing", typeof(double), typeof(Spacing),
            new PropertyMetadata(0.0, OnHorizontalSpacingChanged));

    public static void SetHorizontalSpacing(DependencyObject element, double value) => element.SetValue(HorizontalSpacingProperty, value);
    public static double GetHorizontalSpacing(DependencyObject element) => (double)element.GetValue(HorizontalSpacingProperty);

    private static void OnHorizontalSpacingChanged(DependencyObject d, DependencyPropertyChangedEventArgs e)
    {
        if (d is not Panel panel) return;
        panel.Loaded += (_, __) =>
        {
            var spacing = (double)e.NewValue;
            var children = panel.Children.Cast<UIElement>().ToList();
            for (int i = 0; i < children.Count; i++)
            {
                if (children[i] is FrameworkElement fe)
                {
                    var m = fe.Margin;
                    // 마지막 요소는 우측 여백 제거
                    fe.Margin = new Thickness(m.Left, m.Top, i == children.Count - 1 ? 0 : spacing, m.Bottom);
                }
            }
        };
    }
}