// Converters/FlagEnumToVisibilityConverter.cs
// 중복 enum(WorkflowBadge가 ViewModels/Domain 양쪽에 있을 때도 동작하도록 "제네릭" 플래그 컨버터)
// MarkupExtension + IValueConverter → XAML 리소스 등록 없이 인라인 사용 가능
using System;
using System.Globalization;
using System.Windows;
using System.Windows.Data;
using System.Windows.Markup;

namespace ErpWpf.App.Converters
{
    [MarkupExtensionReturnType(typeof(FlagEnumToVisibilityConverter))]
    public sealed class FlagEnumToVisibilityConverter : MarkupExtension, IValueConverter
    {
        private static readonly FlagEnumToVisibilityConverter Instance = new();

        // 표시/비표시 상태를 바꾸고 싶으면 아래를 조정(기본: Visible/Collapsed)
        public Visibility WhenTrue { get; set; } = Visibility.Visible;
        public Visibility WhenFalse { get; set; } = Visibility.Collapsed;

        public override object ProvideValue(IServiceProvider serviceProvider) => Instance;

        // ConverterParameter에는 enum 플래그 이름을 전달(예: "Created", "Approved", "Released", "OnHold", "Canceled")
        public object Convert(object value, Type targetType, object parameter, CultureInfo culture)
        {
            if (value is null || parameter is null) return WhenFalse;

            // enum 인스턴스가 아니면 실패
            if (value is not Enum enumValue) return WhenFalse;

            var enumType = enumValue.GetType();

            // parameter 문자열을 현재 enum 타입 기준으로 파싱(대소문자 무시)
            try
            {
                var flagObj = Enum.Parse(enumType, parameter.ToString()!, ignoreCase: true);

                // enum.HasFlag는 동일한 enum 타입이면 작동 → 타입 일반화로 중복 enum 문제 회피
                return enumValue.HasFlag((Enum)flagObj) ? WhenTrue : WhenFalse;
            }
            catch
            {
                return WhenFalse;
            }
        }

        public object ConvertBack(object value, Type targetType, object parameter, CultureInfo culture)
            => throw new NotSupportedException();
    }
}