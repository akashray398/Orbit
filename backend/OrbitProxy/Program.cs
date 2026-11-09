using System.Text.Json;
using System.Threading.RateLimiting;
using Microsoft.AspNetCore.RateLimiting;
using OrbitProxy.Models;
using OrbitProxy.Services;

var builder = WebApplication.CreateBuilder(args);

builder.Services.AddHttpClient<ILlmProvider, AnthropicLlmProvider>();
builder.Services.AddSingleton<TokenAuthService>();

builder.Services.AddRateLimiter(options =>
{
    options.RejectionStatusCode = StatusCodes.Status429TooManyRequests;
    options.AddFixedWindowLimiter("PerClientPolicy", opt =>
    {
        opt.PermitLimit = 20;
        opt.Window = TimeSpan.FromMinutes(1);
        opt.QueueLimit = 0;
    });
});

var app = builder.Build();

app.UseRateLimiter();

app.MapGet("/health", () => Results.Ok(new { status = "ok", service = "OrbitProxy", timestamp = DateTimeOffset.UtcNow.ToUnixTimeSeconds() }));

app.MapPost("/v1/auth/register", (AuthRegisterRequest req, TokenAuthService authService) =>
{
    if (string.IsNullOrEmpty(req.InstallId))
    {
        return Results.BadRequest(new { error = "installId is required" });
    }
    var token = authService.GenerateToken(req.InstallId);
    return Results.Ok(token);
});

app.MapPost("/v1/chat/stream", async (HttpContext httpContext, ChatStreamRequest request, ILlmProvider llmProvider, TokenAuthService authService) =>
{
    var authHeader = httpContext.Request.Headers["Authorization"].FirstOrDefault();
    if (!authService.ValidateToken(authHeader, out _))
    {
        httpContext.Response.StatusCode = 401;
        await httpContext.Response.WriteAsync("Unauthorized token");
        return;
    }

    httpContext.Response.ContentType = "text/event-stream";
    httpContext.Response.Headers.CacheControl = "no-cache";

    var cancellationToken = httpContext.RequestAborted;

    await foreach (var evt in llmProvider.StreamChatAsync(request, cancellationToken))
    {
        if (cancellationToken.IsCancellationRequested) break;

        await httpContext.Response.WriteAsync($"event: {evt.EventType}\n", cancellationToken);
        await httpContext.Response.WriteAsync($"data: {evt.DataJson}\n\n", cancellationToken);
        await httpContext.Response.Body.FlushAsync(cancellationToken);
    }
}).RequireRateLimiting("PerClientPolicy");

app.Run("http://0.0.0.0:5000");
