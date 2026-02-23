# Spring AI ChatML

A ChatML-like prompt formatter implementation for [Spring AI](https://spring.io/projects/spring-ai), designed for use with [Ollama](https://ollama.com) and other LLM backends.

## Overview

This module provides a custom ChatML-style prompt formatting implementation for Spring AI chat models. It converts chat messages (SystemMessage, UserMessage, AssistantMessage, ToolResponseMessage) into a format compatible with LFM (Language Model Framework) models via Ollama.

For more details on the ChatML format, see the [Liquid AI Documentation](https://docs.liquid.ai/lfm/key-concepts/chat-template).

## Features

- **ChatML-like format**: Uses `<|startoftext|>`, `<|im_start|>`, `<|im_end|>` special tokens
- **Multi-role support**: System, User, Assistant, and Tool messages
- **Tool calling**: Includes tool definitions in system prompts and parses tool calls from responses
- **Configurable stop sequences**: Stops at `<|im_end|>` and `<|tool_call_end|>`

## File Structure

```
spring-ai-chatml/
├── src/main/java/de/papenhagen/
│   └── LfmChatmlPromptFormatter.java    # Main ChatML formatter implementation
├── src/main/resources/
│   └── application.properties
└── README.md
```

## Key Implementation Details

### ChatML Format

The formatter uses the following pattern:
```
<|startoftext|><|im_start|>system
You are a helpful assistant.<|im_end|>
<|im_start|>user
Hello!<|im_end|>
<|im_start|>assistant
Hi there!<|im_end|>
```

### Message Types

- **SystemMessage**: Formatted as `<|im_start|>system\n{content}<|im_end|>`
- **UserMessage**: Formatted as `<|im_start|>user\n{content}<|im_end|>`
- **AssistantMessage**: Formatted as `<|im_start|>assistant\n{content}<|im_end|>`
- **ToolResponseMessage**: Formatted as `<|im_start|>tool\n{content}<|im_end|>`

### Tool Calling

When tool definitions are provided, they are added to the system prompt in JSON format:
```
Output function calls as JSON.
List of tools: [{"name":"toolName","description":"...","parameters":{...}}]
```

Tool calls in responses are parsed using the pattern:
```
<|tool_call_start|>{tool_name}(arg1=value1, arg2=value2)<|tool_call_end|>
```

## Usage

### Basic Usage

```java
LfmChatmlPromptFormatter formatter = new LfmChatmlPromptFormatter();

List<Message> messages = List.of(
    new SystemMessage("You are a helpful assistant."),
    new UserMessage("What is the weather?")
);

String formattedPrompt = formatter.format(messages);
```

### With Tool Definitions

```java
LfmChatmlPromptFormatter formatter = new LfmChatmlPromptFormatter();
formatter.setToolDefinitions(List.of(toolDefinition1, toolDefinition2));

String formattedPrompt = formatter.format(messages);
```

## Integration with LfmChatModel

This formatter is designed to work with `LfmChatModel`, a custom ChatModel implementation:

```java
LfmChatModel chatModel = new LfmChatModel(
    baseUrl,
    model,
    temperature,
    numCtx,
    numPredict,
    topP,
    topK,
    toolCallingManager
);
```

## Configuration

The formatter uses these constants:
- `<|startoftext|>` - Start of document token
- `<|im_start|>` - Start of message token
- `<|im_end|>` - End of message token
- `<|tool_call_start|>` - Start of tool call token
- `<|tool_call_end|>` - End of tool call token

## Dependencies

- [Spring AI](https://spring.io/projects/spring-ai) (`org.springframework.ai:spring-ai-core`)
- SLF4J for logging
- Java 25

## Build

```bash
./mvnw compile
```

## License

MIT
