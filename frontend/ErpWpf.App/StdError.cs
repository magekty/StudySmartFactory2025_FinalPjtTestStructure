// ErpWpf.App/StdError.cs (ERP 표준 400 바디)
public sealed class StdError
{
    public string code { get; set; } = "";
    public string message { get; set; } = "";
    public string path { get; set; } = "";
    public string method { get; set; } = "";
    public string timestamp { get; set; } = "";
}