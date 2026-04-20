import com.openai.client.OpenAIClient;
import com.openai.client.okhttp.OpenAIOkHttpClient;
import com.openai.models.chat.completions.ChatCompletion;
import com.openai.models.chat.completions.ChatCompletion.Choice;
import com.openai.models.chat.completions.ChatCompletionCreateParams;
import org.json.JSONObject;
import tools.ReadFileTool;

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

    ChatCompletion response = client.chat().completions().create(
        ChatCompletionCreateParams.builder()
            .model("anthropic/claude-haiku-4.5")
            .addUserMessage(prompt)
            .addTool(ReadFileTool.class)
            .build()
    );

    List<Choice> choices = response.choices();

    if (choices.isEmpty()) {
        throw new RuntimeException("no choices in response");
    }

    Choice choice = choices.getFirst();

    if (choice.message().toolCalls().isPresent()) {
        var toolCalls = choice.message().toolCalls().get();

        var toolCall = toolCalls.getFirst();

        if (toolCall.isFunction()
            && toolCall.function().get().function().name().equals(ReadFileTool.class.getSimpleName())) {
            var arguments = toolCall.function().get().function().arguments();
            var argument = new JSONObject(arguments);
            var filePath = argument.getString("file_path");

            try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
                String line;

                while ((line = br.readLine()) != null) {
                    System.out.println(line);
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    } else {
        System.out.print(choice.message().content().orElse(""));
    }
}
