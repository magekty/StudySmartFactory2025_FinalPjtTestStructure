// Models/ProductListItem.cs
namespace Erp.Client.Wpf.Models
{
    public record ProductListItem(
        string productId,
        string productCode,
        string name,
        string description,
        string unit,
        bool active
    );
}

