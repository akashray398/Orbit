using Microsoft.Extensions.Configuration;
using OrbitProxy.Services;
using Xunit;

namespace OrbitProxy.Tests;

public class TokenAuthServiceTests
{
    [Fact]
    public void GenerateToken_ProducesValidSignature()
    {
        var inMemorySettings = new Dictionary<string, string?> {
            {"ORBIT_CLIENT_TOKEN_SECRET", "test_secret_key_32_bytes_minimum_len"},
            {"ENABLE_DEV_PLAY_INTEGRITY_BYPASS", "true"}
        };

        IConfiguration config = new ConfigurationBuilder()
            .AddInMemoryCollection(inMemorySettings)
            .Build();

        var service = new TokenAuthService(config);
        var authResponse = service.GenerateToken("install_test_123");

        Assert.NotNull(authResponse.Token);
        Assert.True(authResponse.ExpiresAt > DateTimeOffset.UtcNow.ToUnixTimeSeconds());

        var isValid = service.ValidateToken($"Bearer {authResponse.Token}", out var installId);
        Assert.True(isValid);
        Assert.Equal("install_test_123", installId);
    }
}
