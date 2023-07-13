package com.openai;

import dev.langchain4j.code.Judge0JavaScriptExecutionTool;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.service.AiServices;

public class ServiceWithDynamicToolsExample {

    interface Assistant {

        String chat(String message);
    }

    public static void main(String[] args) {

        String openAiApiKey = System.getenv("OPENAI_API_KEY"); // https://platform.openai.com/account/api-keys
        ChatLanguageModel chatLanguageModel = OpenAiChatModel.builder()
                .apiKey(openAiApiKey)
                .temperature(0.0)
                .build();

        String rapidApiKey = System.getenv("RAPID_API_KEY"); // https://rapidapi.com/judge0-official/api/judge0-ce
        Judge0JavaScriptExecutionTool judge0Tool = new Judge0JavaScriptExecutionTool(rapidApiKey);

        Assistant assistant = AiServices.builder(Assistant.class)
                .chatLanguageModel(chatLanguageModel)
                .chatMemory(MessageWindowChatMemory.withCapacity(10))
                .tools(judge0Tool)
                .build();

        interact(assistant, "What is the square root of 49506838032859?");
        interact(assistant, "Capitalize every third letter: abcabc");
        interact(assistant, "What is the number of hours between 17:00 on 21 Feb 1988 and 04:00 on 12 Apr 2014?");
    }

    private static void interact(Assistant assistant, String userMessage) {
        System.out.println("[User]: " + userMessage);
        String answer = assistant.chat(userMessage);
        System.out.println("[Assistant]: " + answer);
    }
}
