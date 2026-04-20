package tools;

import com.openai.models.chat.completions.ChatCompletionMessageFunctionToolCall;

public interface Tool {

    String execute(ChatCompletionMessageFunctionToolCall toolCall);
}
