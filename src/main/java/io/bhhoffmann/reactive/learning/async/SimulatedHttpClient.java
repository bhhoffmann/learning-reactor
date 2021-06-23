package io.bhhoffmann.reactive.learning.async;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

public class SimulatedHttpClient {

    private static final Logger logger = LoggerFactory.getLogger(SimulatedHttpClient.class);

    private static final ExecutorService asyncOperationsThreadPool = Executors.newFixedThreadPool(4);

    private SimulatedHttpClient() {
        throw new IllegalStateException("Static class");
    }

    public static String simulateLongRunningNetworkRequest() {
        // Simulate long running network request
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return "Data";
    }

    public static void networkRequestUsingCallback(Consumer<String> callback) {
        asyncOperationsThreadPool.submit(() -> {
            logger.info("Starting simulated network request on async thread pool");

            String response = simulateLongRunningNetworkRequest();

            logger.info("Simulated response received. Invoking callback with result");
            callback.accept(response);
        });
    }

    public static CompletableFuture<String> networkRequestUsingCompletableFuture() {
        CompletableFuture<String> completableFuture = new CompletableFuture<>();
        asyncOperationsThreadPool.submit(() -> {
            logger.info("Starting simulated network request on async thread pool");

            String response = simulateLongRunningNetworkRequest();

            logger.info("Simulated response received. Invoking callback with result");
            completableFuture.complete(response);
        });
        return completableFuture;
    }

    public static Mono<String> networkRequestUsingMono() {
        return Mono.create(sink ->
            asyncOperationsThreadPool.submit(() -> {
                logger.info("Starting simulated network request on async thread pool");

                String response = simulateLongRunningNetworkRequest();

                logger.info("Simulated response received. Emitting the response from the Mono for further processing.");
                sink.success(response);
            })
        );
    }

}
