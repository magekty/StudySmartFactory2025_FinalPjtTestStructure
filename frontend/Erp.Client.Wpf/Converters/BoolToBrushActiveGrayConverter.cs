// Converters/BoolToBrushActiveGrayConverter.cs
using System;
using System.Globalization;
using System.Windows.Data;
using System.Windows.Media;

namespace Erp.Client.Wpf.Converters
{
    public class BoolToBrushActiveGrayConverter : IValueConverter
    {
        public static readonly BoolToBrushActiveGrayConverter Instance = new();
        public object Convert(object value, Type targetType, object parameter, CultureInfo culture)
        {
            if (value is bool b)
                return b ? new SolidColorBrush(Color.FromRgb(33, 150, 243)) : new SolidColorBrush(Color.FromRgb(158, 158, 158));
            return new SolidColorBrush(Color.FromRgb(158, 158, 158));
        }
        public object ConvertBack(object value, Type targetType, object parameter, CultureInfo culture)
            => throw new NotSupportedException();
    }
}