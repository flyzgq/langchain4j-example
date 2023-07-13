import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.model.StreamingResultHandler;
import dev.langchain4j.model.chat.StreamingChatLanguageModel;
import dev.langchain4j.model.language.StreamingLanguageModel;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import dev.langchain4j.model.openai.OpenAiStreamingLanguageModel;

import java.util.List;

import static dev.langchain4j.data.message.SystemMessage.systemMessage;
import static dev.langchain4j.data.message.UserMessage.userMessage;
import static java.util.Arrays.asList;

public class StreamingExamples {

    static class StreamableChatLanguageModel_Example {

        public static void main(String[] args) {

            String apiKey = System.getenv("OPENAI_API_KEY"); // https://platform.openai.com/account/api-keys

            StreamingChatLanguageModel model = OpenAiStreamingChatModel.withApiKey(apiKey);

            List<ChatMessage> messages = asList(
                    systemMessage("You are a very sarcastic assistant"),
                    userMessage("Tell me a joke")
            );

            model.sendMessages(messages, new StreamingResultHandler() {

                @Override
                public void onPartialResult(String partialResult) {
                    System.out.println("Partial result: '" + partialResult + "'");
                }

                @Override
                public void onComplete() {
                    System.out.println("Streaming completed");
                }

                @Override
                public void onError(Throwable error) {
                    error.printStackTrace();
                }
            });
        }
    }

    static class StreamableLanguageModel_Example {

        public static void main(String[] args) {

            String apiKey = System.getenv("OPENAI_API_KEY"); // https://platform.openai.com/account/api-keys

            StreamingLanguageModel model = OpenAiStreamingLanguageModel.withApiKey(apiKey);

            model.process("Tell me a joke", new StreamingResultHandler() {

                @Override
                public void onPartialResult(String partialResult) {
                    System.out.println("Partial result: '" + partialResult + "'");
                }

                @Override
                public void onComplete() {
                    System.out.println("Streaming completed");
                }

                @Override
                public void onError(Throwable error) {
                    error.printStackTrace();
                }
            });
        }
    }
}
