import com.openai.client.OpenAIClient;
import com.openai.client.okhttp.OpenAIOkHttpClient;
import com.openai.models.chat.completions.ChatCompletion;
import com.openai.models.chat.completions.ChatCompletion.Choice;
import com.openai.models.chat.completions.ChatCompletionCreateParams;
import com.openai.models.chat.completions.ChatCompletionToolMessageParam;
import tools.ReadFileTool;
import tools.ToolRegistry;

void main(String[] args) {
    if (args.length < 2 || !"-p".equals(args[0])) {
        System.err.println("Usage: program -p <prompt>");
        System.exit(1);
    }

    String prompt = args[1];

    String apiKey = System.getenv("OPENROUTER_API_KEY");
    String baseUrl = System.getenv("OPENROUTER_BASE_URL");
    if (baseUrl == null || baseUrl.isEmpty()) {
        baseUrl = "https://openrouter.ai/api/v1";
    }

    if (apiKey == null || apiKey.isEmpty()) {
        throw new RuntimeException("OPENROUTER_API_KEY is not set");
    }

    OpenAIClient client = OpenAIOkHttpClient.builder()
        .apiKey(apiKey)
        .baseUrl(baseUrl)
        .build();

    var createParamsBuilder = ChatCompletionCreateParams.builder()
        .model("anthropic/claude-haiku-4.5")
        .addUserMessage(prompt);

    var toolRegistry = new ToolRegistry();
    toolRegistry.getAvailableTools().forEach(tool -> createParamsBuilder.addTool(tool.getClass()));

    while (true) {
        ChatCompletion response = client.chat().completions().create(createParamsBuilder.build());

        List<Choice> choices = response.choices();

        if (choices.isEmpty()) {
            throw new RuntimeException("no choices in response");
        }

        Choice choice = choices.getFirst();
        createParamsBuilder.addMessage(choice.message());

        if (choice.message().toolCalls().isPresent()) {
            choice.message().toolCalls().get().forEach(toolCall -> {
                var tool = toolRegistry.getTool(toolCall).orElseThrow(
                    () -> new RuntimeException("Unknown tool: " + toolCall.function().get().function().name()));

                var result = tool.execute(toolCall.function().get());

                createParamsBuilder.addMessage(ChatCompletionToolMessageParam.builder()
                    .toolCallId(toolCall.function().get().id())
                    .contentAsJson(result)
                    .build());
            });
        } else {
            System.out.print(choice.message().content().orElse(""));
            break;
        }
    }
}
