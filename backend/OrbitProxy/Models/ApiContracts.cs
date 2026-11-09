using System.Text.Json.Nodes;
using System.Text.Json.Serialization;

namespace OrbitProxy.Models;

public record ContentPart
{
    [JsonPropertyName("type")]
    public string Type { get; init; } = "text";

    [JsonPropertyName("text")]
    public string? Text { get; init; }

    [JsonPropertyName("id")]
    public string? Id { get; init; }

    [JsonPropertyName("name")]
    public string? Name { get; init; }

    [JsonPropertyName("arguments")]
    public JsonNode? Arguments { get; init; }

    [JsonPropertyName("toolCallId")]
    public string? ToolCallId { get; init; }

    [JsonPropertyName("content")]
    public string? Content { get; init; }

    [JsonPropertyName("isError")]
    public bool? IsError { get; init; }
}

public record ChatMessageRequest(
    [property: JsonPropertyName("role")] string Role,
    [property: JsonPropertyName("content")] List<ContentPart> Content
);

public record ToolSpecRequest(
    [property: JsonPropertyName("name")] string Name,
    [property: JsonPropertyName("description")] string Description,
    [property: JsonPropertyName("parameters")] JsonNode Parameters
);

public record ChatStreamRequest(
    [property: JsonPropertyName("messages")] List<ChatMessageRequest> Messages,
    [property: JsonPropertyName("promptProfile")] string? PromptProfile,
    [property: JsonPropertyName("tools")] List<ToolSpecRequest>? Tools,
    [property: JsonPropertyName("maxOutputTokens")] int MaxOutputTokens = 1024,
    [property: JsonPropertyName("temperature")] float Temperature = 0.3f
);

public record AuthRegisterRequest(
    [property: JsonPropertyName("installId")] string InstallId,
    [property: JsonPropertyName("integrityToken")] string? IntegrityToken
);

public record AuthRegisterResponse(
    [property: JsonPropertyName("token")] string Token,
    [property: JsonPropertyName("expiresAt")] long ExpiresAt
);

public record SseTextDelta([property: JsonPropertyName("text")] string Text);

public record SseToolCall(
    [property: JsonPropertyName("id")] string Id,
    [property: JsonPropertyName("name")] string Name,
    [property: JsonPropertyName("arguments")] JsonNode Arguments
);

public record SseUsage(
    [property: JsonPropertyName("inputTokens")] int InputTokens,
    [property: JsonPropertyName("outputTokens")] int OutputTokens
);

public record SseDone([property: JsonPropertyName("stopReason")] string StopReason);

public record SseError(
    [property: JsonPropertyName("code")] string Code,
    [property: JsonPropertyName("message")] string Message
);
