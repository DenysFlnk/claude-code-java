package tools;

import com.fasterxml.jackson.annotation.JsonClassDescription;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.openai.models.chat.completions.ChatCompletionMessageFunctionToolCall;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import org.json.JSONObject;

@JsonClassDescription("Read and return the contents of a file")
public class ReadFileTool implements Tool<String> {

    @JsonPropertyDescription("The path to the file to read")
    @JsonProperty("file_path")
    public String filePath;

    public String execute(ChatCompletionMessageFunctionToolCall toolCall) {
        var arguments = toolCall.function().arguments();
        var argument = new JSONObject(arguments);
        var path = argument.getString("file_path");

        var result = new StringBuilder();

        try (BufferedReader br = new BufferedReader(new FileReader(path))) {
            String line;

            while ((line = br.readLine()) != null) {
                result.append(line).append("\n");
            }
        } catch (IOException e) {
            return "Failed to read a file. Reason: " + e.getMessage();
        }

        return result.toString();
    }
}
