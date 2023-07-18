package com.huggingface.huggingface;

import dev.langchain4j.chain.ConversationalRetrievalChain;
import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.DocumentLoader;
import dev.langchain4j.data.document.DocumentSegment;
import dev.langchain4j.data.document.DocumentSplitter;
import dev.langchain4j.data.document.splitter.SentenceSplitter;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.huggingface.HuggingFaceEmbeddingModel;
import dev.langchain4j.model.input.Prompt;
import dev.langchain4j.model.input.PromptTemplate;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiEmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import dev.langchain4j.store.embedding.PineconeEmbeddingStore;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static dev.langchain4j.data.document.DocumentType.TEXT;
import static dev.langchain4j.model.openai.OpenAiModelName.GPT_3_5_TURBO;
import static dev.langchain4j.model.openai.OpenAiModelName.TEXT_EMBEDDING_ADA_002;
import static java.time.Duration.ofSeconds;
import static java.util.stream.Collectors.joining;

public class ChatWithDocumentsExamples {

    // Please also check com.huggingface.ServiceWithRetrieverExample

    static class IfYouNeedSimplicity {

        public static void main(String[] args) throws UnsupportedEncodingException, URISyntaxException, MalformedURLException {

//            String apiKey = System.getenv("OPENAI_API_KEY"); // https://platform.openai.com/account/api-keys

            ConversationalRetrievalChain chain = ConversationalRetrievalChain.builder()
                    .documentLoader(DocumentLoader.from(toPath("classpath://story-about-happy-carrot.txt")))
                    .embeddingModel(ChatModelFactory.buildEmbeddingModel())
                    .chatLanguageModel(ChatModelFactory.buildHuggingFaceChatModel())
                    .build();

            // By default,
            // - Document will be split into paragraphs
            // - Transient in-memory embedding store will be used
            // - 5 most relevant embeddings will be retrieved on each chain execution
            // - The following prompt template will be used:
            // Answer the following question to the best of your ability: {{question}}
            //
            // Base your answer on the following information:
            // {{information}}

            // You can override above-mentioned behavior in ConversationalRetrievalChain builder

            String answer = chain.execute("Who is Charlie? Answer in 10 words.");

            System.out.println(answer);
        }
    }

    static class HuggingFace_Embeddings_Example {

        public static void main(String[] args) {

            ConversationalRetrievalChain chain = ConversationalRetrievalChain.builder()
                    .documentLoader(DocumentLoader.from(toPath("story-about-happy-carrot.txt")))
                    .embeddingModel(HuggingFaceEmbeddingModel.builder()
                            .accessToken(System.getenv("HF_API_KEY")) // https://huggingface.co/settings/tokens
                            .modelId("sentence-transformers/all-MiniLM-L6-v2")
                            .build())
                    .chatLanguageModel(OpenAiChatModel.withApiKey(System.getenv("OPENAI_API_KEY")))
                    .build();

            String answer = chain.execute("Who is Charlie? Answer in 10 words.");

            System.out.println(answer);
        }
    }

    static class If_You_Need_More_Control {

        public static void main(String[] args) {

            // Load the document that includes the information you'd like to "chat" about with the model.
            // Currently, loading text and PDF files from file system and by URL is supported.

            DocumentLoader documentLoader = DocumentLoader.from(toPath("story-about-happy-carrot.txt"), TEXT);
            Document document = documentLoader.load();


            // Split document into segments (one paragraph per segment)

            DocumentSplitter splitter = new SentenceSplitter();
            List<DocumentSegment> documentSegments = splitter.split(document);


            // Embed segments (convert them into semantic vectors)

            EmbeddingModel embeddingModel = OpenAiEmbeddingModel.builder()
                    .apiKey(System.getenv("OPENAI_API_KEY")) // https://platform.openai.com/account/api-keys
                    .modelName(TEXT_EMBEDDING_ADA_002)
                    .timeout(ofSeconds(15))
                    .build();

            List<Embedding> embeddings = embeddingModel.embedAll(documentSegments).get();


            // Store embeddings into embedding store for further search / retrieval

            PineconeEmbeddingStore pinecone = PineconeEmbeddingStore.builder()
                    .apiKey(System.getenv("PINECONE_API_KEY")) // https://app.pinecone.io/organizations/xxx/projects/yyy:zzz/keys
                    .environment("northamerica-northeast1-gcp")
                    .projectName("19a129b")
                    .index("test-s1-1536") // make sure the dimensions of the Pinecone index match the dimensions of the embedding model (1536 for text-embedding-ada-002)
                    .build();

            pinecone.addAll(embeddings, documentSegments);


            // Define the question you want to ask the model and embed it

            String question = "Who is Charlie? Answer in 10 words.";

            Embedding questionEmbedding = embeddingModel.embed(question).get();


            // Find relevant embeddings in embedding store by semantic similarity

            List<EmbeddingMatch<DocumentSegment>> relevantEmbeddings = pinecone.findRelevant(questionEmbedding, 3);


            // Create a prompt for the model that includes question and relevant embeddings

            PromptTemplate promptTemplate = PromptTemplate.from(
                    "Answer the following question to the best of your ability:\n"
                            + "\n"
                            + "Question:\n"
                            + "{{question}}\n"
                            + "\n"
                            + "Base your answer on the following information:\n"
                            + "{{information}}");

            String information = relevantEmbeddings.stream()
                    .map(match -> match.embedded().text())
                    .collect(joining("\n\n"));

            Map<String, Object> variables = new HashMap<>();
            variables.put("question", question);
            variables.put("information", information);

            Prompt prompt = promptTemplate.apply(variables);


            // Send prompt to the model

            ChatLanguageModel chatModel = OpenAiChatModel.builder()
                    .apiKey(System.getenv("OPENAI_API_KEY")) // https://platform.openai.com/account/api-keys
                    .modelName(GPT_3_5_TURBO)
                    .temperature(1.0)
                    .logResponses(true)
                    .logRequests(true)
                    .build();

            AiMessage aiMessage = chatModel.sendUserMessage(prompt).get();


            // See an answer from the model

            String answer = aiMessage.text();
            System.out.println(answer);
        }
    }

    private static Path toPath(String fileName) {
        try {
            URL fileUrl = ChatWithDocumentsExamples.class.getResource(fileName);
            return Paths.get(fileUrl.toURI());
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }
}
