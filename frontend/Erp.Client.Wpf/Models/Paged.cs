// Models/Paged.cs
using System.Collections.Generic;

namespace Erp.Client.Wpf.Models
{
    public class Paged<T>
    {
        public List<T> content { get; set; } = new();
        public int number { get; set; }
        public int size { get; set; }
        public long totalElements { get; set; }
        public int totalPages { get; set; }
    }
}