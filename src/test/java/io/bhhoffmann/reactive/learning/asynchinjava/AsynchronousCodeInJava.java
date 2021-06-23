package io.bhhoffmann.reactive.learning.asynchinjava;

import io.bhhoffmann.reactive.learning.async.FileHandler;
import java.nio.ByteBuffer;
import java.nio.channels.CompletionHandler;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

class AsynchronousCodeInJava {

    private static final Logger logger = LoggerFactory.getLogger(AsynchronousCodeInJava.class);

    private static final Integer ITERATIONS = 100_000;

    private static int simulateProcessingSomething() {
        int sum = 0;
        for (int i = 0; i < ITERATIONS; i++) {
            sum += i;
        }
        return sum;
    }

    @Test
    void synchronousBlockingIO() {
        logger.info("Doing something...");
        logger.info("Start blocking IO operation");
        String fileContent = FileHandler.readFileBlocking("src/main/resources/the-road-not-taken.txt");
        logger.info("IO operation complete");
        logger.info("File content:\n{}", fileContent);
        logger.info("Doing something else...");
    }

    @Test
    void asynchronousNonBlockingIOWithCallback() throws InterruptedException {
        logger.info("Doing something...");
        logger.info("Start non-blocking IO operation");
        ByteBuffer buffer = ByteBuffer.allocate(1048576);
        FileHandler.readFileNonBlockingCallback(
            "src/main/resources/the-road-not-taken.txt",
            buffer,
            new CompletionHandler<>() {
                @Override
                public void completed(Integer result, ByteBuffer attachment) {
                    logger.info("IO operation complete");
                    attachment.flip();
                    String fileContent = new String(attachment.array()).trim();
                    logger.info("File content:\n{}", fileContent);
                    attachment.clear();
                }

                @Override
                public void failed(Throwable e, ByteBuffer attachment) {
                    logger.warn("Failed to read file. Exception message: {}", e.getMessage());
                }
            }
        );
        logger.info("Doing something else...");

        //Sleep to wait for async code to finish
        Thread.sleep(1000);
    }

    @Test
    void asynchronousNonBlockingIOWithFuture() throws InterruptedException {
        logger.info("Doing something...");
        logger.info("Start non-blocking IO operation");
        ByteBuffer buffer = ByteBuffer.allocate(1048576);
        Future<Integer> ioStatus = FileHandler.readFileNonBlockingFuture(
            "src/main/resources/the-road-not-taken.txt",
            buffer
        );
        logger.info("Doing something else...");

        //Loop on the status to wait until the async IO is ready
        while (!ioStatus.isDone()) {
            logger.info("File read not complete");
        }
        logger.info("IO operation complete");
        buffer.flip();
        String fileContent = new String(buffer.array()).trim();
        logger.info("File content:\n{}", fileContent);
    }

    @Test
    void asynchronousNonBlockingIOWithCompletableFuture() throws InterruptedException, ExecutionException {
        logger.info("Doing something...");

        logger.info("Start non-blocking IO operation");
        CompletableFuture<ByteBuffer> completableFuture = FileHandler
            .readFileNonBlockingCompletableFuture("src/main/resources/the-road-not-taken.txt");

        logger.info("Doing something else...");

        ByteBuffer buffer = completableFuture.get();
        logger.info("IO operation complete");
        buffer.flip();
        String fileContent = new String(buffer.array()).trim();
        logger.info("File content:\n{}", fileContent);
    }

    @Test
    void asynchronousNonBlockingIOWithCompletableFutureConvertedToMono() throws InterruptedException {
        logger.info("Doing something...");

        logger.info("Assembling mono that contains IO operation");
        Mono<String> sequence = FileHandler
            .readFileNonBlockingConvertCompletableFutureToMono("src/main/resources/the-road-not-taken.txt")
            .doOnNext(fileContent -> {
                logger.info("IO operation complete");
                logger.info("File content:\n{}", fileContent);
            });

        logger.info("Start execution of Mono");
        sequence.subscribe();

        logger.info("Doing something else...");

        Thread.sleep(1000);
    }

    @Test
    void asynchronousNonBlockingIOWithCallbackConvertedToMono() throws InterruptedException {
        logger.info("Doing something...");

        logger.info("Assembling mono that contains IO operation");
        Mono<String> sequence = FileHandler
            .readFileNonBlockingConvertCallbackToMono("src/main/resources/the-road-not-taken.txt")
            .doOnNext(fileContent -> {
                logger.info("IO operation complete");
                logger.info("File content:\n{}", fileContent);
            });

        logger.info("Start execution of Mono");
        sequence.subscribe();

        logger.info("Doing something else...");

        Thread.sleep(1000);
    }

    @Test
    void exampleBlockingCode() {
        long executionStart = System.nanoTime();

        //Simulate doing some processing
        long startTime = System.nanoTime();
        int sum = 0;
        for (int i = 0; i < ITERATIONS; i++) {
            sum += i;
        }
        long firstProcessingTime = System.nanoTime() - startTime;

        //Do some IO
        startTime = System.nanoTime();
        String fileContent = FileHandler.readFileBlocking("src/main/resources/lorem-ipsum.txt");
        long ioTime = System.nanoTime() - startTime;

        //Simulate doing some processing again
        startTime = System.nanoTime();
        int secondResult = simulateProcessingSomething();
        long secondProcessingTime = System.nanoTime() - startTime;

        long totalExecutionTime = System.nanoTime() - executionStart;

        logger.info("\n First processing time {} microseconds "
                + "\n IO time {} microseconds "
                + "\n Second processing time {} microseconds"
                + "\n Total execution time {} microseconds",
            firstProcessingTime / 1000,
            ioTime / 1000,
            secondProcessingTime / 1000,
            totalExecutionTime / 1000);
    }

    @Test
    void exampleNonBlockingCode() throws ExecutionException, InterruptedException {
        long executionStart = System.nanoTime();

        //Simulate doing some processing
        long startTime = System.nanoTime();
        int sum = 0;
        for (int i = 0; i < ITERATIONS; i++) {
            sum += i;
        }
        long firstProcessingTime = System.nanoTime() - startTime;

        //Do some IO
        ByteBuffer buffer = ByteBuffer.allocate(1048576);
        long ioStartTime = System.nanoTime();
        Future<Integer> ioStatus = FileHandler.readFileNonBlockingFuture("src/main/resources/lorem-ipsum.txt", buffer);
        long ioTimeToStart = System.nanoTime() - ioStartTime;

        //Do something else while the IO is done in the background
        startTime = System.nanoTime();
        int secondResult = simulateProcessingSomething();
        long secondProcessingTime = System.nanoTime() - startTime;

        //Loop on the status to wait until the async IO is ready
        while (!ioStatus.isDone()) {
            logger.info("File read not complete");
        }
        //buffer.flip();
        //String fileContent = new String(buffer.array()).trim();
        long ioTimeToComplete = System.nanoTime() - ioStartTime;

        long totalExecutionTime = System.nanoTime() - executionStart;

        //logger.info("Data read from file: {}", fileContent);

        logger.info("\n First processing time {} microseconds "
                + "\n IO time to start {} microseconds "
                + "\n Second processing time {} microseconds "
                + "\n IO time to complete {} microseconds "
                + "\n Total execution time {} microseconds",
            firstProcessingTime / 1000,
            ioTimeToStart / 1000,
            secondProcessingTime / 1000,
            ioTimeToComplete / 1000,
            totalExecutionTime / 1000);
    }

}
