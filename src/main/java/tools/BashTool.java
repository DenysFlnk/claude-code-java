package tools;

import com.fasterxml.jackson.annotation.JsonClassDescription;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.openai.models.chat.completions.ChatCompletionMessageFunctionToolCall;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Paths;
import org.json.JSONObject;

@JsonClassDescription("Execute a shell command")
public class BashTool implements Tool<String> {

    @JsonPropertyDescription("The command to execute")
    @JsonProperty("command")
    public String command;

    @Override
    public String execute(ChatCompletionMessageFunctionToolCall toolCall) {
        var arguments = new JSONObject(toolCall.function().arguments());
        var toolCommand = arguments.getString("command").split(" ");

        var result = new StringBuilder();

        try {
            System.out.println("Executing command: " + toolCommand);
            var processBuilder = new ProcessBuilder(toolCommand);
            var process = processBuilder.start();
            process.waitFor();

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

            String line;
            while ((line = reader.readLine())!= null) {
                result.append(line).append(System.lineSeparator());
            }

        } catch (IOException | InterruptedException e) {
            System.out.println("Error while executing bash command: " + e.getMessage());
            return "Error while executing bash command: " + e.getMessage();
        }
        System.out.println(result);
        return result.toString();
    }

    static void main() {
        System.out.println(Paths.get("").toAbsolutePath());
    }
}
