using OrbitProxy.Models;

namespace OrbitProxy.Services;

public record LlmStreamEvent(string EventType, string DataJson);

public interface ILlmProvider
{
    IAsyncEnumerable<LlmStreamEvent> StreamChatAsync(ChatStreamRequest request, CancellationToken cancellationToken);
}
