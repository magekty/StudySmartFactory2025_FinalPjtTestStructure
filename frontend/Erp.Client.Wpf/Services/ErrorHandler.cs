namespace Erp.Client.Wpf.Services;

public static class ErrorHandler
{
    public static string ToUserMessage(Exception ex)
    {
        // 간단한 버전: 서버 메시지 그대로
        return ex.Message;
    }
}