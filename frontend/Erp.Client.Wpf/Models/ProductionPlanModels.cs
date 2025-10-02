// Models/ProductionPlanModels.cs
using System;
using System.Collections.Generic;

namespace Erp.Client.Wpf.Models
{
    public class ProductionPlanSummaryDto
    {
        public string planId { get; set; } = "";
        public string planCode { get; set; } = "";
        public string productId { get; set; } = "";
        public string? productCode { get; set; }
        public string? productName { get; set; }
        public DateTime startDate { get; set; }
        public DateTime endDate { get; set; }
        public decimal qty { get; set; }
        public string status { get; set; } = "DRAFT";
        public DateTime? createdAt { get; set; }
    }

    public class ProductionPlanDetailDto
    {
        public string planId { get; set; } = "";
        public string planCode { get; set; } = "";
        public string productId { get; set; } = "";
        public string? productCode { get; set; }
        public string? productName { get; set; }
        public DateTime startDate { get; set; }
        public DateTime endDate { get; set; }
        public decimal qty { get; set; }
        public string status { get; set; } = "DRAFT";
        public string? note { get; set; }
        public long version { get; set; }
    }

    public class CreateProductionPlanRequest
    {
        public string planCode { get; set; } = "";
        public string productId { get; set; } = "";
        public decimal qty { get; set; }
        public string startDate { get; set; } = ""; // yyyy-MM-dd
        public string endDate { get; set; } = "";   // yyyy-MM-dd
        public string status { get; set; } = "DRAFT";
        public string? note { get; set; }
    }

    public class UpdateProductionPlanRequest
    {
        public decimal? qty { get; set; }
        public string? startDate { get; set; } // yyyy-MM-dd
        public string? endDate { get; set; }   // yyyy-MM-dd
        public string? note { get; set; }
        public long version { get; set; }
    }

    public class ChangeStatusRequest
    {
        public string nextStatus { get; set; } = "";
        public string actor { get; set; } = "system";
        public long version { get; set; }
    }

    // 🚨 1.1. MES 전송 요청 모델 추가
    // 백엔드에서 Plan ID는 URL 경로로 받고, 본문(Body)에는 요청자 정보만 보낸다고 가정합니다.
    public class SendToMesRequest
    {
        public string modifier { get; set; } = "erp_user"; // 실제 로그인 사용자명으로 대체 필요
    }
}