// Program.cs (발췌)
using System;
using System.Net;
using System.Net.Http.Headers;

var builder = WebApplication.CreateBuilder(args);

// 설정값
string erpBase = builder.Configuration["Erp:BaseUrl"] ?? "http://localhost:8081";
string erpApiKey = builder.Configuration["Erp:ApiKey"] ?? "change-me-demo-key";
string mesBase = builder.Configuration["Mes:BaseUrl"] ?? "http://localhost:8080";

builder.Services.AddRazorPages();

builder.Services.AddHttpClient("erp", c =>
{
    c.BaseAddress = new Uri(erpBase);
    c.DefaultRequestHeaders.Accept.Add(new MediaTypeWithQualityHeaderValue("application/json"));
    if (!string.IsNullOrWhiteSpace(erpApiKey))
        c.DefaultRequestHeaders.Add("X-API-Key", erpApiKey);
}).ConfigurePrimaryHttpMessageHandler(() => new SocketsHttpHandler
{
    AutomaticDecompression = DecompressionMethods.All
});

builder.Services.AddHttpClient("mes", c =>
{
    c.BaseAddress = new Uri(mesBase);
    c.DefaultRequestHeaders.Accept.Add(new MediaTypeWithQualityHeaderValue("application/json"));
}).ConfigurePrimaryHttpMessageHandler(() => new SocketsHttpHandler
{
    AutomaticDecompression = DecompressionMethods.All
});

// FE용 서비스 등록(웹 콘솔 OFF 시 미등록)
builder.Services.AddScoped<ErpConsole.Services.MesApi>();
builder.Services.AddScoped<ErpConsole.Services.ErpApi>();

var app = builder.Build();

if (!app.Environment.IsDevelopment())
{
    app.UseExceptionHandler("/Error");
    app.UseHsts();
}

app.UseHttpsRedirection();
app.UseStaticFiles();
app.UseRouting();

app.MapRazorPages();
// 웹 콘솔 OFF: 간단한 핑만(선택)
//app.MapGet("/", () => Results.Ok(new { console = "disabled", at = DateTimeOffset.UtcNow }));

app.Run();