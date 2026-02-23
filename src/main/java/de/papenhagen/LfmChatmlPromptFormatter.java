package de.papenhagen;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.tool.definition.ToolDefinition;

public class LfmChatmlPromptFormatter {

    private static final Logger LOG = LoggerFactory.getLogger(LfmChatmlPromptFormatter.class);

    private static final String START_OF_TEXT = "<|startoftext|>";
    private static final String IM_START = "<|im_start|>";
    private static final String IM_END = "<|im_end|>";
    private static final String ASSISTANT = "assistant";

    private List<ToolDefinition> toolDefinitions = List.of();

    public String format(final List<Message> messages) {
        final StringBuilder prompt = new StringBuilder();
        prompt.append(START_OF_TEXT);

        for (final Message message : messages) {
            prompt.append(formatMessage(message));
        }

        prompt.append(IM_START).append(ASSISTANT).append("\n");
        LOG.debug("Formatted LFM prompt: {}", prompt);
        return prompt.toString();
    }

    private String formatMessage(final Message message) {
        final String role = getRole(message);
        String content = message.getText();

        if (message instanceof SystemMessage && !this.toolDefinitions.isEmpty()) {
            content = addToolsToSystemPrompt(content);
        }

        return IM_START + role + "\n" + content + IM_END + "\n";
    }

    private String addToolsToSystemPrompt(final String originalContent) {
        final StringBuilder systemContent = new StringBuilder();

        if (originalContent != null && !originalContent.isEmpty()) {
            systemContent.append(originalContent).append("\n\n");
        }

        systemContent.append("Output function calls as JSON.\n");
        systemContent.append("List of tools: ").append(toolDefinitionsToJson());

        return systemContent.toString();
    }

    private String toolDefinitionsToJson() {
        final StringBuilder json = new StringBuilder("[");
        final List<String> toolJsonList = new ArrayList<>();

        for (final ToolDefinition toolDef : this.toolDefinitions) {
            final StringBuilder toolJson = new StringBuilder();
            toolJson.append("{");
            toolJson.append("\"name\":\"").append(toolDef.name()).append("\",");
            toolJson.append("\"description\":\"").append(escapeJson(toolDef.description())).append("\",");
            toolJson.append("\"parameters\":");
            if (toolDef.inputSchema() != null) {
                toolJson.append(toolDef.inputSchema());
            } else {
                toolJson.append("{\"type\":\"object\",\"properties\":{}}");
            }
            toolJson.append("}");
            toolJsonList.add(toolJson.toString());
        }

        json.append(String.join(",", toolJsonList));
        json.append("]");
        return json.toString();
    }

    private String escapeJson(final String text) {
        if (text == null) {
            return "";
        }
        return text.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    public void setToolDefinitions(final List<ToolDefinition> pToolDefinitions) {
        this.toolDefinitions = pToolDefinitions != null ? pToolDefinitions : List.of();
        LOG.debug("Set tool definitions: {}", this.toolDefinitions.size());
    }

    private String getRole(final Message message) {
        if (message instanceof SystemMessage) {
            return "system";
        } else if (message instanceof UserMessage) {
            return "user";
        } else if (message instanceof org.springframework.ai.chat.messages.AssistantMessage) {
            return ASSISTANT;
        } else if (message instanceof org.springframework.ai.chat.messages.ToolResponseMessage) {
            return "tool";
        }
        return "user";
    }
}
