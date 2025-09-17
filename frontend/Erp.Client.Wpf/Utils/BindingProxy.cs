// Views/BindingProxy.cs (컨텍스트 메뉴 바인딩 보조)
using System.Windows;
using System.Windows.Media;

namespace Erp.Client.Wpf.Utils
{
    public class BindingProxy : Freezable
    {
        protected override Freezable CreateInstanceCore() => new BindingProxy();

        public object Data
        {
            get => GetValue(DataProperty);
            set => SetValue(DataProperty, value);
        }

        public static readonly DependencyProperty DataProperty =
            DependencyProperty.Register("Data", typeof(object), typeof(BindingProxy), new UIPropertyMetadata(null));
    }
}