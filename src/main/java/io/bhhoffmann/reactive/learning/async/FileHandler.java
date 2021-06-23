package io.bhhoffmann.reactive.learning.async;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.channels.CompletionHandler;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

public class FileHandler {

    private static final Logger logger = LoggerFactory.getLogger(FileHandler.class);

    public static String readFileBlocking(String classpath) {
        List<String> fileLines = new ArrayList<>();

        Path path = Path.of(classpath);
        try {
            fileLines = Files.readAllLines(path);
        } catch (IOException e) {
            logger.warn("Failed to read file with path: {}. Exception message: {}", path, e.getMessage());
        }
        return String.join("\n", fileLines);
    }

    public static void readFileNonBlockingCallback(
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

    public static Future<Integer> readFileNonBlockingFuture(
        String filepath,
        ByteBuffer buffer
    ) {
        Path path = Path.of(filepath);
        Future<Integer> status = null;
        try (AsynchronousFileChannel asyncChannel = AsynchronousFileChannel.open(path, StandardOpenOption.READ)) {
            status = asyncChannel.read(buffer, 0);
        } catch (IOException e) {
            logger.warn("Failed to read file with path: {}. Exception message: {}", path, e.getMessage());
        }
        return status;
    }

    public static CompletableFuture<ByteBuffer> readFileNonBlockingCompletableFuture(String filepath) {
        CompletableFuture<ByteBuffer> completableFuture = new CompletableFuture<>();
        ByteBuffer buffer = ByteBuffer.allocate(1048576);
        readFileNonBlockingCallback(
            filepath,
            buffer,
            //The Java API (java.nio.channels.AsynchronousFileChannel.read()) uses a separate thread
            //to monitor the I/O operation. When the operation completes it will execute the callback provided
            //below, calling completed() if the operation was successful or failed() if it failed.
            new CompletionHandler<>() {
                @Override
                public void completed(Integer result, ByteBuffer attachment) {
                    logger.info("CompletionHandler complete(): Setting completableFuture.complete()");
                    completableFuture.complete(buffer);
                }

                @Override
                public void failed(Throwable e, ByteBuffer attachment) {
                    logger.info("CompletionHandler failed(): Setting completableFuture.completeExceptionally()");
                    completableFuture.completeExceptionally(e);
                }
            }
        );
        return completableFuture;
    }

    public static Mono<String> readFileNonBlockingConvertCompletableFutureToMono(String filepath) {
        //Mono.fromFuture() lets us convert a CompletableFuture to a Mono.

        //Mono.fromFuture() does not take a lambda, i.e. its content is run during assembly.
        //We wrap it in Mono.defer() so that it is not started until execution phase.
        return Mono.defer(() -> Mono.fromFuture(readFileNonBlockingCompletableFuture(filepath)))
            .map(byteBuffer -> {
                byteBuffer.flip();
                return new String(byteBuffer.array()).trim();
            })
            //The callback in readFileNonBlockingCompletableFuture() that populates the CompletableFuture when
            //the I/O operation completes is handled by the thread spawned by the Java API to monitor I/O events,
            //but the CompletableFuture itself, which we convert to a Mono in this method, is handled by the
            //calling thread. If we don't want to block the calling thread while it waits for the
            //CompletableFuture we should run it on a different thread -> .subscribeOn()
            .subscribeOn(Schedulers.boundedElastic());
    }

    public static Mono<String> readFileNonBlockingConvertCallbackToMono(String filepath) {
        //Mono.create() lets us convert a callback to a Mono.
        //The MonoSink (sink) is used to send signals to downstream subscribers (i.e. next chained operator like .map().)
        //Since the sink is used inside the callback it is the thread running the callback, i.e. the thread spawned
        //by the Java API that monitors the I/O operation, that uses the sink to emit the data. Since Project Reactor
        //does not switch threads unless told so all following operators in the sequence will be handled by this thread.

        Mono<ByteBuffer> sequence = Mono.create(sink -> {
            ByteBuffer buffer = ByteBuffer.allocate(1048576);
            readFileNonBlockingCallback(
                filepath,
                buffer,
                new CompletionHandler<>() {
                    @Override
                    public void completed(Integer result, ByteBuffer attachment) {
                        logger.info("CompletionHandler complete(): Setting the Mono's sink.success()");
                        sink.success(buffer);
                    }

                    @Override
                    public void failed(Throwable e, ByteBuffer attachment) {
                        logger.info("CompletionHandler failed(): Setting the Mono's sink.error()");
                        sink.error(e);
                    }
                }
            );
        });

        return sequence
            .map(byteBuffer -> {
                byteBuffer.flip();
                return new String(byteBuffer.array()).trim();
            });

    }


}
