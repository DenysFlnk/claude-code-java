package tools;

import com.fasterxml.jackson.annotation.JsonClassDescription;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.openai.models.chat.completions.ChatCompletionMessageFunctionToolCall;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import org.json.JSONObject;

@JsonClassDescription("Write content to a file")
public class WriteFileTool implements Tool<String> {

    @JsonPropertyDescription("The path of the file to write to")
    @JsonProperty("file_path")
    public String filePath;

    @JsonPropertyDescription("The content to write to the file")
    @JsonProperty("content")
    public String content;

    @Override
    public String execute(ChatCompletionMessageFunctionToolCall toolCall) {
        var arguments = new JSONObject(toolCall.function().arguments());
        var path = arguments.getString("file_path");
        var fileContent = arguments.getString("content");

        try {
            Files.writeString(Paths.get(path), fileContent);
        } catch (IOException e) {
            return "Failed to create a file. Reason: " + e.getMessage();
        }

        return "File created.";
    }
}
