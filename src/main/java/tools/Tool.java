package tools;

import com.openai.models.chat.completions.ChatCompletionMessageFunctionToolCall;

public interface Tool<T> {

    T execute(ChatCompletionMessageFunctionToolCall toolCall);
}
