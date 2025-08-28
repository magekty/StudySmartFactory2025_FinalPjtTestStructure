// src/Program.cs
using Microsoft.Extensions.Caching.Memory;
using System.Text.Json.Serialization;
using static ApiUtils;

var builder = WebApplication.CreateBuilder(args);

// Swagger
builder.Services.AddEndpointsApiExplorer();
builder.Services.AddSwaggerGen();

// Caches
builder.Services.AddMemoryCache();

// JSON 기본 옵션(필요 시)
builder.Services.ConfigureHttpJsonOptions(o =>
{
    o.SerializerOptions.DefaultIgnoreCondition = JsonIgnoreCondition.WhenWritingNull;
    o.SerializerOptions.Converters.Add(new JsonStringEnumConverter());
});

var app = builder.Build();

// Swagger
app.UseSwagger();
app.UseSwaggerUI();

// API Key 인증
//app.Use(async (ctx, next) =>
//{
//    var required = builder.Configuration.GetValue<string>("Auth:ApiKey");
//    if (!string.IsNullOrWhiteSpace(required))
//    {
//        if (!ctx.Request.Headers.TryGetValue("X-API-Key", out var got) || got != required)
//        {
//            ctx.Response.StatusCode = StatusCodes.Status401Unauthorized;
//            await ctx.Response.WriteAsJsonAsync(new { code = "UNAUTHORIZED", message = "UNAUTHORIZED", path = ctx.Request.Path, method = ctx.Request.Method });
//            return;
//        }
//    }
//    await next();
//});

// 멱등 미들웨어(In-Memory)
app.Use(async (ctx, next) =>
{
    var cache = ctx.RequestServices.GetRequiredService<IMemoryCache>();
    if (ctx.Request.Method is "POST" or "PUT")
    {
        var key = ctx.Request.Headers["X-Idempotency-Key"].ToString();
        if (!string.IsNullOrWhiteSpace(key))
        {
            if (cache.TryGetValue<(int, string)>(key, out var cached))
            {
                ctx.Response.StatusCode = cached.Item1;
                ctx.Response.ContentType = "application/json";
                await ctx.Response.WriteAsync(cached.Item2);
                return;
            }

            var originalBody = ctx.Response.Body;
            using var mem = new MemoryStream();
            ctx.Response.Body = mem;

            await next();

            mem.Position = 0;
            using var reader = new StreamReader(mem);
            var bodyText = await reader.ReadToEndAsync();
            mem.Position = 0;
            await mem.CopyToAsync(originalBody);
            ctx.Response.Body = originalBody;

            if (ctx.Response.StatusCode is >= 200 and < 300)
            {
                var ttlHours = builder.Configuration.GetValue("Idempotency:TTLHours", 72);
                cache.Set(key, (ctx.Response.StatusCode, bodyText), TimeSpan.FromHours(ttlHours));
            }
            return;
        }
    }
    await next();
});

// Health
app.MapGet("/", () => Results.Ok(new { ok = true, ts = DateTimeOffset.UtcNow }));

// MES → ERP: Work Order Create
app.MapPost("/erp/work-orders", (WorkOrderCreateDto dto, HttpRequest req) =>
{
    if (string.IsNullOrWhiteSpace(dto.WorkOrderId) || string.IsNullOrWhiteSpace(dto.ItemId) || dto.Qty < 0)
        return Bad("VALIDATION_ERROR", req);
    if (!new[] { "P", "R", "C" }.Contains(dto.Status))
        return Bad("VALIDATION_ERROR", req);
    if (dto.StartTs is not null && !IsUtc(dto.StartTs.Value))
        return Bad("VALIDATION_ERROR", req);

    return Results.Created($"/erp/work-orders/{dto.WorkOrderId}", new { workOrderId = dto.WorkOrderId });
});

// MES → ERP: Work Order Status
app.MapPut("/erp/work-orders/{workOrderId}/status", (string workOrderId, WorkOrderStatusDto dto, HttpRequest req) =>
{
    if (string.IsNullOrWhiteSpace(workOrderId))
        return Bad("VALIDATION_ERROR", req);
    if (!new[] { "P", "R", "C" }.Contains(dto.Status))
        return Bad("VALIDATION_ERROR", req);
    if (!IsUtc(dto.ChangedAt))
        return Bad("VALIDATION_ERROR", req);

    return Results.Ok(new { workOrderId, status = dto.Status });
});

// MES → ERP: Performance Create
app.MapPost("/erp/performances", (PerformanceCreateDto dto, HttpRequest req) =>
{
    if (string.IsNullOrWhiteSpace(dto.WorkOrderId) || dto.GoodQty < 0 || dto.DefectQty < 0)
        return Bad("VALIDATION_ERROR", req);
    if (!IsUtc(dto.StartTime) || !IsUtc(dto.EndTime))
        return Bad("VALIDATION_ERROR", req);
    if (dto.EndTime < dto.StartTime)
        return Bad("TIME_ORDER_INVALID", req);

    return Results.Created($"/erp/performances/{Guid.NewGuid():N}", new { workOrderId = dto.WorkOrderId });
});

// MES → ERP: Backflush (Shadow 대상 안내용)
app.MapPost("/erp/consumptions/backflush", (BackflushDto dto, HttpRequest req) =>
{
    if (string.IsNullOrWhiteSpace(dto.WorkOrderId))
        return Bad("VALIDATION_ERROR", req);
    if (dto.Lines is null || !dto.Lines.Any() || dto.Lines.Any(l => l.Qty < 0 || string.IsNullOrWhiteSpace(l.ComponentId) || string.IsNullOrWhiteSpace(l.Uom)))
        return Bad("VALIDATION_ERROR", req);

    return Results.Accepted($"/erp/consumptions/backflush/{Guid.NewGuid():N}", new { workOrderId = dto.WorkOrderId, shadow = "handled-by-MES" });
});

app.Run();