// App.xaml.cs
using System;
using System.Net;
using System.Net.Http.Headers;
using System.Windows;
using Microsoft.Extensions.Configuration;
using Microsoft.Extensions.DependencyInjection;
using Microsoft.Extensions.Hosting;
using System.Net.Http.Json;
using System.Net.Http;

namespace ErpWpf.App;

public partial class App : Application
{
    public static IHost HostSvc { get; private set; } = default!;

    protected override void OnStartup(StartupEventArgs e)
    {
        HostSvc = Host.CreateDefaultBuilder()
              .ConfigureAppConfiguration(cfg =>
              {
                  cfg.Sources.Clear();
                  cfg.SetBasePath(AppContext.BaseDirectory) // 실행 파일 위치 기준
                     .AddJsonFile("appsettings.json", optional: false, reloadOnChange: true);
              })
            .ConfigureServices((ctx, services) =>
            {
                var erpBase = ctx.Configuration["Erp:BaseUrl"] ?? "http://localhost:8081";
                var erpApiKey = ctx.Configuration["Erp:ApiKey"];

                // HttpClient(ERP): X-API-Key 주입
                services.AddHttpClient("erp", c =>
                {
                    c.BaseAddress = new Uri(erpBase);
                    c.DefaultRequestHeaders.Accept.Add(new MediaTypeWithQualityHeaderValue("application/json"));
                    if (!string.IsNullOrWhiteSpace(erpApiKey))
                        c.DefaultRequestHeaders.Add("X-API-Key", erpApiKey);
                }).ConfigurePrimaryHttpMessageHandler(() => new SocketsHttpHandler
                {
                    AutomaticDecompression = DecompressionMethods.All
                });

                // API 클라이언트 등록
                services.AddSingleton<ErpApi>();

                // 유틸/뷰모델/메인윈도
                services.AddSingleton<IdempotencyKeyGenerator>();
                services.AddSingleton<TimeUtil>();
                services.AddSingleton<MainWindow>();
            })
            .Build();

        HostSvc.Start();

        var win = HostSvc.Services.GetRequiredService<MainWindow>();
        win.Show();

        base.OnStartup(e);
    }

    protected override async void OnExit(ExitEventArgs e)
    {
        if (HostSvc != null) await HostSvc.StopAsync();
        HostSvc?.Dispose();
        base.OnExit(e);
    }
}