# Dockerfile
FROM mcr.microsoft.com/dotnet/aspnet:8.0 AS base
WORKDIR /app
EXPOSE 8080
ENV ASPNETCORE_URLS=http://+:8080

FROM mcr.microsoft.com/dotnet/sdk:8.0 AS build
WORKDIR /src
COPY ./src/*.csproj ./src/
RUN dotnet restore ./src/ERP.Demo.csproj
COPY ./src/ ./src/
RUN dotnet publish ./src/ERP.Demo.csproj -c Release -o /app/publish

FROM base AS final
WORKDIR /app
COPY --from=build /app/publish .
ENV Auth__ApiKey=change-me-demo-key
ENTRYPOINT ["dotnet", "ERP.Demo.dll"]