package io.bhhoffmann.reactive.learning.lessons.lesson4;

import java.time.Duration;
import java.util.Random;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

class Concurrency {

    private static final Logger logger = LoggerFactory.getLogger(Concurrency.class);

    @Test
    void byDefaultEverythingSimplyRunsOnTheMainThread() {
        //You can see in the log statement which thread that runs the log statement -> e.g. [main] for the main thread.
        logger.info("Starting executing the program. This runs on the main thread");

        logger.info("--- START ASSEMBLY ---");
        Flux<Integer> sequence = Flux.range(1, 10)
            .map(nr -> nr * 2)
            .doOnNext(nr -> logger.info("This runs on the main thread. Got element: {}", nr));

        logger.info("--- START EXECUTION ---");
        sequence.subscribe();
        logger.info("Since we only use the main thread this line won't be run before the sequence has completed "
            + "its execution.");
    }

    @Test
    void implicitlyUsingAThreadPoolBecauseOfDelayElements() throws InterruptedException {
        logger.info("Starting executing the program. This runs on the main thread");

        logger.info("--- START ASSEMBLY ---");
        Flux<Integer> sequence = Flux.range(1, 10)
            // Since we use this operator the execution is shifted to a thread pool
            .delayElements(Duration.ofMillis(100))
            .map(nr -> nr * 2)
            .doOnNext(nr -> logger.info("This does not run on the main thread. Got element: {}", nr));

        logger.info("--- START EXECUTION ---");
        sequence.subscribe();
        logger.info("Since execution happens on a different thread .subscribe() returns immediately and can "
            + "execute this line. It will most likely be run before the sequence is executed.");

        // We need to sleep the main thread so that the threads executing the sequence have time to finish.
        // You can try to comment out the sleep, and see that the program will exit before the sequence executes.
        Thread.sleep(2000);
    }

    @Test
    void explicitlyUsingAThreadPool() throws InterruptedException {
        logger.info("Starting executing the program. This runs on the main thread");

        logger.info("--- START ASSEMBLY ---");
        Flux<Integer> sequence = Flux.range(1, 10)
            // Will cause the entire sequence to be executed on the boundedElastic thread pool
            // (default pool provided by Project Reactor).
            // It is also possible to create your own custom pools.
            .subscribeOn(Schedulers.boundedElastic())
            .map(nr -> nr * 2)
            .doOnNext(nr -> logger.info("This does not run on the main thread. Got element: {}", nr));

        logger.info("--- START EXECUTION ---");
        sequence.subscribe();
        logger.info("Since execution happens on a different thread .subscribe() returns immediately and can "
            + "execute this line. It will most likely be run before the sequence is executed.");

        //We need to sleep the main thread so that the threads executing the sequence have time to finish.
        //You can try to comment out the sleep, and see that the program will exit before the sequence executes.
        Thread.sleep(2000);
    }

    @Test
    void concurrencyInFlatMap() throws InterruptedException {
        logger.info("Starting executing the program.");

        Random random = new Random();

        logger.info("--- START ASSEMBLY ---");
        Flux<Integer> sequence = Flux.range(1, 10)
            .flatMap(nr ->
                // The sequence inside this flatMap simulates an operation that takes an unknown amount of time,
                // like making a network request. Since execution of the sequence inside the .flatMap() is run
                // on a thread pool (because of .delayElements()), the main thread running the sequence with the
                // .flatMap() is free to start executing the next element emitted by Flux.range().
                // .flatMap() does not keep the order of the elements, it will simply emit elements in the order
                // that the inner sequences complete. There are other operators that keeps ordering.
                // Try changing it to concatMap() and see the difference
                Mono.fromCallable(() -> nr)
                    .delayElement(Duration.ofMillis(random.nextInt(1000) + 500)) //random delay, 500 - 1000 ms
            )
            .doOnNext(nr -> logger.info("Got element: {}", nr));

        logger.info("--- START EXECUTION ---");
        sequence.subscribe();

        //We need to sleep the main thread so that the threads executing the sequence have time to finish.
        //You can try to comment out the sleep, and see that the program will exit before the sequence executes.
        logger.info("Sleeping main thread to wait for execution to complete on other threads");
        Thread.sleep(10000);
    }

    @Test
    void concurrencyInFlatMapCanBeControlledExplicitly() throws InterruptedException {
        logger.info("--- START ASSEMBLY ---");
        Flux<Integer> sequence = Flux.range(1, 10)
            .flatMap(nr ->
                    // We provide .flatMap() with the optional second argument 'concurrency'. This controls how
                    // many inner sequences the .flatMap() will run concurrently. Here we set it to 3, and give all
                    // inner sequences a delay of 500 ms. As a result we should see 3 and 3 elements processed and
                    // emitted more or less at the same time.
                    // PS: Default value of the 'concurrency' argument is 254
                    Mono.fromCallable(() -> nr)
                        .delayElement(Duration.ofMillis(500)),
                3
            )
            .doOnNext(nr -> logger.info("Got element: {}", nr));

        logger.info("--- START EXECUTION ---");
        sequence.subscribe();

        //We need to sleep the main thread so that the threads executing the sequence have time to finish.
        //You can try to comment out the sleep, and see that the program will exit before the sequence executes.
        logger.info("Sleeping main thread to wait for execution to complete on other threads");
        Thread.sleep(5000);
    }

    @Test
    void blockWillBlockTheThread() throws InterruptedException {
        //NEVER BLOCK IN PRODUCTION
        logger.info("Starting executing the program. This runs on the main thread");

        logger.info("--- START ASSEMBLY ---");
        Flux<Integer> sequence = Flux.range(1, 10)
            .delayElements(
                Duration.ofMillis(100)) //Since we use this operator the execution is shifted to a thread pool
            .map(nr -> nr * 2)
            .doOnNext(nr -> logger.info("This does not run on the main thread. Got element: {}", nr));

        logger.info("--- START EXECUTION, BLOCK ---");
        //For a Flux we can use blockLast() to block until we get the last element
        Integer lastElement = sequence.blockLast();
        logger.info("Execution of the sequence happens on a thread pool, but since we use .blockLast() "
            + "we are blocking the main thread until the sequence is completed.");

        //The Thread.sleep() is not required here, because we block the main thread waiting for the result of
        //the sequence.
    }

}
