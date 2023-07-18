package com.huggingface.huggingface;

import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.model.chat.ChatLanguageModel;

public class HelloWorldExample {

    public static void main(String[] args) {

        // Import your OpenAI/HuggingFace API key
//        String apiKey = System.getenv("OPENAI_API_KEY");

        // Create an instance of a model
//        OpenAiChatModel model = OpenAiChatModel.withApiKey(apiKey);
        ChatLanguageModel model = ChatModelFactory.buildHuggingFaceChatModel();

        // Start interacting!
        AiMessage answer = model.sendUserMessage("Hello world!").get();

        System.out.println(answer.text()); // Hello! How can I assist you today?
    }
}
