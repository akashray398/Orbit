using System.Net.Http.Headers;
using System.Runtime.CompilerServices;
using System.Text;
using System.Text.Json;
using System.Text.Json.Nodes;
using OrbitProxy.Models;

namespace OrbitProxy.Services;

public class AnthropicLlmProvider : ILlmProvider
{
    private readonly HttpClient _httpClient;
    private readonly string _apiKey;
    private readonly string _model;

    public AnthropicLlmProvider(HttpClient httpClient, IConfiguration config)
    {
        _httpClient = httpClient;
        _apiKey = config["LLM_API_KEY"] ?? "dummy_key_for_dev";
        _model = config["LLM_MODEL"] ?? "claude-3-5-sonnet-20241022";
    }

    public async IAsyncEnumerable<LlmStreamEvent> StreamChatAsync(
        ChatStreamRequest request,
        [EnumeratorCancellation] CancellationToken cancellationToken)
    {
        var anthropicMessages = new List<object>();

        foreach (var msg in request.Messages)
        {
            var contentList = new List<object>();
            foreach (var part in msg.Content)
            {
                if (part.Type == "text" && !string.IsNullOrEmpty(part.Text))
                {
                    contentList.Add(new { type = "text", text = part.Text });
                }
                else if (part.Type == "tool_call" && part.Id != null && part.Name != null && part.Arguments != null)
                {
                    contentList.Add(new { type = "tool_use", id = part.Id, name = part.Name, input = part.Arguments });
                }
                else if (part.Type == "tool_result" && part.ToolCallId != null && part.Content != null)
                {
                    contentList.Add(new { type = "tool_result", tool_use_id = part.ToolCallId, content = part.Content, is_error = part.IsError ?? false });
                }
            }
            anthropicMessages.Add(new { role = msg.Role == "user" ? "user" : "assistant", content = contentList });
        }

        var systemPrompt = "You are Orbit, a personal AI agent running on Android. Be concise, helpful, and direct. Ask before irreversible actions.";

        object? toolsPayload = null;
        if (request.Tools != null && request.Tools.Count > 0)
        {
            toolsPayload = request.Tools.Select(t => new
            {
                name = t.Name,
                description = t.Description,
                input_schema = t.Parameters
            }).ToList();
        }

        var payload = new
        {
            model = _model,
            messages = anthropicMessages,
            system = systemPrompt,
            max_tokens = Math.Min(request.MaxOutputTokens, 2048),
            temperature = request.Temperature,
            stream = true,
            tools = toolsPayload
        };

        var jsonStr = JsonSerializer.Serialize(payload);
        var httpRequest = new HttpRequestMessage(HttpMethod.Post, "https://api.anthropic.com/v1/messages")
        {
            Content = new StringContent(jsonStr, Encoding.UTF8, "application/json")
        };

        httpRequest.Headers.Add("x-api-key", _apiKey);
        httpRequest.Headers.Add("anthropic-version", "2023-06-01");

        HttpResponseMessage response;
        try
        {
            response = await _httpClient.SendAsync(httpRequest, HttpCompletionOption.ResponseHeadersRead, cancellationToken);
        }
        catch (Exception ex)
        {
            yield return new LlmStreamEvent("error", JsonSerializer.Serialize(new { code = "upstream", message = ex.Message }));
            yield break;
        }

        if (!response.IsSuccessStatusCode)
        {
            var errText = await response.Content.ReadAsStringAsync(cancellationToken);
            yield return new LlmStreamEvent("error", JsonSerializer.Serialize(new { code = "upstream_error", message = $"Anthropic status {(int)response.StatusCode}: {errText}" }));
            yield break;
        }

        using var stream = await response.Content.ReadAsStreamAsync(cancellationToken);
        using var reader = new StreamReader(stream);

        string? currentEvent = null;
        var partialToolId = "";
        var partialToolName = "";
        var partialToolJson = new StringBuilder();

        while (!reader.EndOfStream && !cancellationToken.IsCancellationRequested)
        {
            var line = await reader.ReadLineAsync(cancellationToken);
            if (string.IsNullOrEmpty(line)) continue;

            if (line.StartsWith("event: "))
            {
                currentEvent = line.Substring(7).Trim();
            }
            else if (line.StartsWith("data: ") && currentEvent != null)
            {
                var data = line.Substring(6).Trim();
                if (data == "[DONE]") break;

                try
                {
                    using var doc = JsonDocument.Parse(data);
                    var root = doc.RootElement;

                    if (currentEvent == "content_block_delta")
                    {
                        if (root.TryGetProperty("delta", out var delta))
                        {
                            if (delta.TryGetProperty("type", out var deltaType) && deltaType.GetString() == "text_delta")
                            {
                                var text = delta.GetProperty("text").GetString() ?? "";
                                yield return new LlmStreamEvent("text_delta", JsonSerializer.Serialize(new { text }));
                            }
                            else if (delta.TryGetProperty("type", out var toolDelta) && toolDelta.GetString() == "input_json_delta")
                            {
                                var jsonChunk = delta.GetProperty("partial_json").GetString() ?? "";
                                partialToolJson.Append(jsonChunk);
                            }
                        }
                    }
                    else if (currentEvent == "content_block_start")
                    {
                        if (root.TryGetProperty("content_block", out var block))
                        {
                            if (block.TryGetProperty("type", out var bType) && bType.GetString() == "tool_use")
                            {
                                partialToolId = block.GetProperty("id").GetString() ?? "";
                                partialToolName = block.GetProperty("name").GetString() ?? "";
                                partialToolJson.Clear();
                            }
                        }
                    }
                    else if (currentEvent == "content_block_stop")
                    {
                        if (!string.IsNullOrEmpty(partialToolId) && !string.IsNullOrEmpty(partialToolName))
                        {
                            var toolArgObj = JsonNode.Parse(partialToolJson.ToString().Trim()) ?? new JsonObject();
                            yield return new LlmStreamEvent("tool_call", JsonSerializer.Serialize(new
                            {
                                id = partialToolId,
                                name = partialToolName,
                                arguments = toolArgObj
                            }));
                            partialToolId = "";
                            partialToolName = "";
                            partialToolJson.Clear();
                        }
                    }
                    else if (currentEvent == "message_stop")
                    {
                        yield return new LlmStreamEvent("done", JsonSerializer.Serialize(new { stopReason = "end_turn" }));
                    }
                }
                catch
                {
                    // Ignore transient chunk parse errors
                }
            }
        }
    }
}
