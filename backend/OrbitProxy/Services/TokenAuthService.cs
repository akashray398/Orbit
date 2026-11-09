using System.Security.Cryptography;
using System.Text;
using OrbitProxy.Models;

namespace OrbitProxy.Services;

public class TokenAuthService
{
    private readonly byte[] _secretBytes;
    private readonly bool _devBypass;

    public TokenAuthService(IConfiguration config)
    {
        var secret = config["ORBIT_CLIENT_TOKEN_SECRET"] ?? "default_secret_key_32_bytes_long_orbit_proxy";
        _secretBytes = Encoding.UTF8.GetBytes(secret);
        _devBypass = string.Equals(config["ENABLE_DEV_PLAY_INTEGRITY_BYPASS"], "true", StringComparison.OrdinalIgnoreCase);
    }

    public AuthRegisterResponse GenerateToken(string installId)
    {
        var expiresAt = DateTimeOffset.UtcNow.AddHours(1).ToUnixTimeSeconds();
        var payload = $"{installId}:{expiresAt}";
        using var hmac = new HMACSHA256(_secretBytes);
        var hash = hmac.ComputeHash(Encoding.UTF8.GetBytes(payload));
        var signature = Convert.ToBase64String(hash);
        var token = Convert.ToBase64String(Encoding.UTF8.GetBytes($"{payload}:{signature}"));

        return new AuthRegisterResponse(token, expiresAt);
    }

    public bool ValidateToken(string? authHeader, out string installId)
    {
        installId = "";
        if (string.IsNullOrEmpty(authHeader) || !authHeader.StartsWith("Bearer ")) return _devBypass;

        var token = authHeader.Substring(7).Trim();
        try
        {
            var raw = Encoding.UTF8.GetString(Convert.FromBase64String(token));
            var parts = raw.Split(':');
            if (parts.Length != 3) return false;

            installId = parts[0];
            if (!long.TryParse(parts[1], out var expiresAt)) return false;

            if (DateTimeOffset.UtcNow.ToUnixTimeSeconds() > expiresAt) return false;

            var payload = $"{parts[0]}:{parts[1]}";
            using var hmac = new HMACSHA256(_secretBytes);
            var expectedHash = hmac.ComputeHash(Encoding.UTF8.GetBytes(payload));
            var expectedSignature = Convert.ToBase64String(expectedHash);

            return parts[2] == expectedSignature;
        }
        catch
        {
            return false;
        }
    }
}
