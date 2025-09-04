// ErpWpf.App/TimeUtil.cs
using System;

public sealed class TimeUtil
{
    public string ToUtcIso(DateTimeOffset dto) =>
        dto.ToUniversalTime().ToString("yyyy-MM-dd'T'HH:mm:ss'Z'");
}