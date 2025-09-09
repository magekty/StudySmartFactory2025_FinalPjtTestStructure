// 12) 등록 — HttpClientFactory 주입(호스트 초기화 위치)
// File: Global/ServicesRegistration.cs (또는 App.xaml.cs 초기화)
using System;
using ErpWpf.App.Adapters.Mes;
using ErpWpf.App.Config;
using ErpWpf.App.Ports;
using Microsoft.Extensions.DependencyInjection;

namespace ErpWpf.App;

public static class ServicesRegistration
{
    public static IServiceCollection AddAppClients(this IServiceCollection services, ApiEndpoints ep)
    {
        services.AddHttpClient<IMesWoStatusClient, MesWoStatusClient>(c => c.BaseAddress = new Uri(ep.MesBaseUrl));
        services.AddHttpClient<IMesBomClient, MesBomClient>(c => c.BaseAddress = new Uri(ep.MesBaseUrl));
        services.AddHttpClient<IMesInventoryClient, MesInventoryClient>(c => c.BaseAddress = new Uri(ep.MesBaseUrl));

        // 기존 ErpApi.* 구현을 IPlanRepository에 연결
        // services.AddScoped<IPlanRepository, PlanRepository>();

        return services;
    }
}