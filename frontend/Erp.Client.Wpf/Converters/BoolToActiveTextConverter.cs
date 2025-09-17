// Converters/BoolToActiveTextConverter.cs
using System;
using System.Globalization;
using System.Windows.Data;

namespace Erp.Client.Wpf.Converters
{
    public class BoolToActiveTextConverter : IValueConverter
    {
        public static readonly BoolToActiveTextConverter Instance = new();
        public object Convert(object value, Type targetType, object parameter, CultureInfo culture)
            => value is bool b ? (b ? "활성" : "비활성") : "비활성";
        public object ConvertBack(object value, Type targetType, object parameter, CultureInfo culture)
            => throw new NotSupportedException();
    }
}