package com.epam.training.gen.ai.vector;

import com.azure.ai.openai.OpenAIAsyncClient;
import com.azure.ai.openai.models.EmbeddingItem;
import com.azure.ai.openai.models.Embeddings;
import com.azure.ai.openai.models.EmbeddingsOptions;
import com.epam.training.gen.ai.dto.VectorRequestDto;
import io.qdrant.client.QdrantClient;
import io.qdrant.client.grpc.Collections;
import io.qdrant.client.grpc.Collections.VectorParams;
import io.qdrant.client.grpc.JsonWithInt;
import io.qdrant.client.grpc.Points.PointStruct;
import io.qdrant.client.grpc.Points.ScoredPoint;
import io.qdrant.client.grpc.Points.SearchPoints;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.Collection;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

import static io.qdrant.client.PointIdFactory.id;
import static io.qdrant.client.VectorsFactory.vectors;
import static io.qdrant.client.WithPayloadSelectorFactory.enable;

/**
 * Service class for processing text into embeddings and interacting with Qdrant for vector storage and retrieval.
 * <p>
 * This service converts text into embeddings using Azure OpenAI and saves these vectors in a Qdrant collection.
 * It also provides functionality to search for similar vectors based on input text.
 */

@Slf4j
@Service
@AllArgsConstructor
public class VectorStorageService {
    private static final String COLLECTION_NAME = "demo_collection";
    private final OpenAIAsyncClient openAIAsyncClient;
    private final QdrantClient qdrantClient;

    /**
     * Processes the input text into embeddings, transforms them into vector points,
     * @param vectorRequestDto
     * @throws ExecutionException
     * @throws InterruptedException
     */
    public void processAndSaveText(VectorRequestDto vectorRequestDto) throws ExecutionException, InterruptedException {
        var embeddings = getEmbeddings(vectorRequestDto.getText());

        List<PointStruct> pointStructs = embeddings.stream()
                .map(EmbeddingItem::getEmbedding)
                .map(point -> getPointStruct(point, vectorRequestDto.getText()))
                .toList();

        boolean collectionDontExists = !qdrantClient.collectionExistsAsync(COLLECTION_NAME).get();

        if (collectionDontExists) {
            log.info("Collection [{}] does not exist.", COLLECTION_NAME);
            createCollection();
        }

        saveVector(pointStructs);
    }

    /**
     * Searches the Qdrant collection for vectors similar to the input text.
     * <p>
     * The input text is converted to embeddings, and a search is performed based on the vector similarity.
     *
     * @param text the text to search for similar vectors
     * @return a list of scored points representing similar vectors
     * @throws ExecutionException if the search operation fails
     * @throws InterruptedException if the thread is interrupted during execution
     */
    public List<ScoredPoint> search(String text) throws ExecutionException, InterruptedException {
        boolean collectionDontExists = !qdrantClient.collectionExistsAsync(COLLECTION_NAME).get();

        if (collectionDontExists) {
            log.warn("Collection [{}] does not exist.", COLLECTION_NAME);
            return List.of();
        }

        var embeddings = retrieveEmbeddings(text);
        var qe = new ArrayList<Float>();
        embeddings.block().getData().forEach(embeddingItem ->
                qe.addAll(embeddingItem.getEmbedding())
        );
        return qdrantClient
                .searchAsync(
                        SearchPoints.newBuilder()
                                .setCollectionName(COLLECTION_NAME)
                                .addAllVector(qe)
                                .setWithPayload(enable(true))
                                .setLimit(1)
                                .build())
                .get();
    }

    public List<String> searchEmbeddings(String input) {
        List<EmbeddingItem> embeddings = getEmbeddings(input);
        List<Float> vector = embeddings.stream().map(EmbeddingItem::getEmbedding)
                .flatMap(Collection::stream)
                .toList();

        SearchPoints searchPoints = SearchPoints.newBuilder()
                .setCollectionName(COLLECTION_NAME)
                .addAllVector(vector)
                .setWithPayload(enable(true))
                .setLimit(2)
                .build();

        try {
            List<ScoredPoint> scoredPoints = qdrantClient.searchAsync(searchPoints).get();
            return scoredPoints.stream()
                    .map(point -> point.getPayloadMap().get("input").getStringValue())
                    .toList();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Retrieves the embeddings for the given text using Azure OpenAI.
     *
     * @param text the text to be embedded
     * @return a list of {@link EmbeddingItem} representing the embeddings
     */
    public List<EmbeddingItem> getEmbeddings(String text) {
        var embeddings = retrieveEmbeddings(text);
        return embeddings.block().getData();
    }

    /**
     * Creates a new collection in Qdrant with specified vector parameters.
     *
     * @throws ExecutionException if the collection creation operation fails
     * @throws InterruptedException if the thread is interrupted during execution
     */
    public void createCollection() throws ExecutionException, InterruptedException {
        var result = qdrantClient.createCollectionAsync(COLLECTION_NAME,
                        VectorParams.newBuilder()
                                .setDistance(Collections.Distance.Cosine)
                                .setSize(1536)
                                .build())
                .get();
        log.info("Collection was created: [{}]", result.getResult());
    }

    /**
     * Saves the list of point structures (vectors) to the Qdrant collection.
     *
     * @param pointStructs the list of vectors to be saved
     * @throws InterruptedException if the thread is interrupted during execution
     * @throws ExecutionException if the saving operation fails
     */
    private void saveVector(List<PointStruct> pointStructs) throws InterruptedException, ExecutionException {
        var updateResult = qdrantClient.upsertAsync(COLLECTION_NAME, pointStructs).get();
        log.info(updateResult.getStatus().name());
    }

    /**
     * Constructs a point structure from a list of float values representing a vector.
     *
     * @param point the vector values
     * @return a {@link PointStruct} object containing the vector and associated metadata
     */
    private PointStruct getPointStruct(List<Float> point, String input) {
        return PointStruct.newBuilder()
                .setId(id(UUID.randomUUID()))
                .setVectors(vectors(point))
                .putPayload("input", JsonWithInt.Value.newBuilder().setStringValue(input).build())
                .build();
    }

    /**
     * Retrieves the embeddings for the given text asynchronously from Azure OpenAI.
     *
     * @param text the text to be embedded
     * @return a {@link Mono} of {@link Embeddings} representing the embeddings
     */
    private Mono<Embeddings> retrieveEmbeddings(String text) {
        var qembeddingsOptions = new EmbeddingsOptions(List.of(text));
        return openAIAsyncClient.getEmbeddings("text-embedding-ada-002", qembeddingsOptions);
    }

    private boolean collectionExists(String collectionName) throws ExecutionException, InterruptedException {
        var response = qdrantClient.getCollectionInfoAsync(collectionName).get();
        return response.getStatus().equals("ok");
    }
}
