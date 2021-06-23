package io.bhhoffmann.reactive.learning.lessons.lesson5;

import io.bhhoffmann.reactive.learning.async.SimulatedHttpClient;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

class Asynchronous {

    private static final Logger logger = LoggerFactory.getLogger(Asynchronous.class);

    private static String someOperationWeWantToDoAsynchronously() {
        logger.info("Starting asynchronous operation");
        // Simulate long running async operation
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        logger.info("Asynchronous operation complete");
        return "Async operation complete";
    }

    @Test
    void normalSynchronousCode() {
        // do something
        String result = someOperationWeWantToDoAsynchronously();
        result = result.toUpperCase();
        logger.info("Result of async operation: {}", result);
        // do something else
    }

    @Test
    void projectReactorDoesNotMagicallyMakeThingAsynchronous() {
        // do something
        logger.info("--- START ASSEMBLY ---");
        // Even though we wrap it in a Mono this code will still be synchronous, since we don't
        // specify the use of any other threads.
        Mono<String> sequence = Mono.fromCallable(() -> someOperationWeWantToDoAsynchronously())
            .map(res -> res.toUpperCase())
            .doOnNext(res -> logger.info("Result of async operation: {}", res));

        logger.info("--- START EXECUTION ---");
        sequence.subscribe();

        logger.info("This line won't execute until the sequence is complete");

        // do something else
    }

    @Test
    void runExecutionOnDifferentThreadToMakeItAsynchronous() throws InterruptedException {
        // do something
        logger.info("--- START ASSEMBLY ---");
        // Notice that the only difference from the synchronous version is the .subscibeOn() that controls
        // on which thread the code is executed. The rest of the code, and most importantly the logic, is unchanged!
        Mono<String> sequence = Mono.fromCallable(() -> someOperationWeWantToDoAsynchronously())
            .map(res -> res.toUpperCase())
            .doOnNext(res -> logger.info("Result of async operation: {}", res))
            .subscribeOn(Schedulers.boundedElastic());

        logger.info("--- START EXECUTION ---");
        sequence.subscribe();

        logger.info("Main thread doing something else");

        // do something else

        //Sleep to let the async execution complete
        Thread.sleep(3000);
    }

    @Test
    void simulatedAsyncHttpRequestWithCallbackToHandleResult() throws InterruptedException {
        // Do something
        logger.info("Do a simulated HTTP request");
        SimulatedHttpClient.networkRequestUsingCallback(response -> {
            logger.info("Result of network request: {}", response);
            // Do something with the result here
            // If you need to do more async operations this quickly becomes a callback hell.
        });

        logger.info("Optionally continue doing other things with this thread.");
        // Sleep main thread to let async operations complete
        Thread.sleep(3000);
    }

    @Test
    void simulatedAsyncHttpRequestWithMonoToHandleResult() throws InterruptedException {
        // Do something

        logger.info("Do a simulated HTTP request");
        logger.info("--- START ASSEMBLY ---");
        Mono<String> sequence = SimulatedHttpClient.networkRequestUsingMono()
            .doOnNext(response -> logger.info("Result of network request: {}", response));
        // Here you can just continue to chain operators in order to process the result.
        // No callback hell, and it looks identical to as if this was synchronous code.

        logger.info("--- START EXECUTION ---");
        sequence.subscribe();

        logger.info("Optionally continue doing other things with this thread.");
        // Sleep main thread to let async operations complete
        Thread.sleep(3000);
    }

}
