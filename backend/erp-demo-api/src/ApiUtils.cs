// src/ApiUtils.cs
using Microsoft.AspNetCore.Http;

public static class ApiUtils
{
    public static bool IsUtc(DateTimeOffset t) => t.Offset == TimeSpan.Zero;

    public static IResult Bad(string code, HttpRequest r)
        => Results.BadRequest(new { code, message = code, path = r.Path, method = r.Method });
}