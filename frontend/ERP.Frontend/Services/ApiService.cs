using System.Net.Http;

namespace ERP.Frontend.Services
{
    public class ApiService
    {
        private readonly HttpClient _httpClient;
        private const string BaseUrl = "http://localhost:8080/api/v1/";

        public ApiService()
        {
            _httpClient = new HttpClient();
            _httpClient.BaseAddress = new System.Uri(BaseUrl);
        }

        public HttpClient GetHttpClient()
        {
            return _httpClient;
        }
    }
}