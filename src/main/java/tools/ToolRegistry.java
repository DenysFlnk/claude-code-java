package tools;

import com.openai.models.chat.completions.ChatCompletionMessageToolCall;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class ToolRegistry {

    private final Map<String, Tool> tools = new HashMap<>();

    public ToolRegistry() {
        tools.put(ReadFileTool.class.getSimpleName(), new ReadFileTool());
    }

    public Optional<Tool> getTool(ChatCompletionMessageToolCall toolCall) {
        if (!toolCall.isFunction()) {
            return Optional.empty();
        }

        return Optional.ofNullable(tools.get(toolCall.function().get().function().name()));
    }
}
