package io.bhhoffmann.reactive.learning.asynchinjava;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.channels.CompletionHandler;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class AsynchronousCodeInJava {

    private static final Logger logger = LoggerFactory.getLogger(AsynchronousCodeInJava.class);

    private static final Integer ITERATIONS = 100_000;

    private static String readFileBlocking(String classpath) {
        List<String> fileLines = new ArrayList<>();

        Path path = Path.of(classpath);
        try {
            fileLines = Files.readAllLines(path);
        } catch (IOException e) {
            logger.warn("Failed to read file with path: {}. Exception message: {}", path, e.getMessage());
        }
        return String.join("\n", fileLines);
    }

    private static Future<Integer> readFileNonBlocking(
        String filepath,
        ByteBuffer buffer
    ) {
        Path path = Path.of(filepath);
        Future<Integer> status = null;
        try {
            status = AsynchronousFileChannel
                .open(path, StandardOpenOption.READ)
                .read(buffer, 0);
        } catch (IOException e) {
            logger.warn("Failed to read file with path: {}. Exception message: {}", path, e.getMessage());
        }
        return status;
    }

    private static void readFileNonBlocking(
        String filepath,
        ByteBuffer buffer,
        CompletionHandler<Integer, ByteBuffer> callback
    ) {
        Path path = Path.of(filepath);
        try (AsynchronousFileChannel asyncChannel = AsynchronousFileChannel.open(path, StandardOpenOption.READ)) {
            asyncChannel.read(buffer, 0, buffer, callback);
        } catch (IOException e) {
            logger.warn("Failed to read file with path: {}. Exception message: {}", path, e.getMessage());
        }
    }

    private static int simulateProcessingSomething() {
        int sum = 0;
        for (int i = 0; i < ITERATIONS; i++) {
            sum += i;
        }
        return sum;
    }

    @Test
    void synchronousBlocking() {
        logger.info("Doing something...");
        logger.info("Start blocking IO operation");
        String fileContent = readFileBlocking("src/main/resources/the-road-not-taken.txt");
        logger.info("IO operation complete");
        logger.info("File content:\n{}", fileContent);
        logger.info("Doing something else...");
    }

    @Test
    void asynchronousNonBlocking() throws InterruptedException {
        logger.info("Doing something...");
        logger.info("Start non-blocking IO operation");
        ByteBuffer buffer = ByteBuffer.allocate(1048576);
        readFileNonBlocking(
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

        //Sleep to wait for aync code to finish
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
        String fileContent = readFileBlocking("src/main/resources/lorem-ipsum.txt");
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
        Future<Integer> ioStatus = readFileNonBlocking("src/main/resources/lorem-ipsum.txt", buffer);
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

    @Test
    void readFileSynchronousBlocking() {
        String fileContent = readFileBlocking("src/main/resources/the-road-not-taken.txt");
        logger.info("Content read from file: \n{}", fileContent);
    }

}
