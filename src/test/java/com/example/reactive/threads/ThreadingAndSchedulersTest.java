package com.example.reactive.threads;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.concurrent.CountDownLatch;

public class ThreadingAndSchedulersTest {

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Test
    public void assemblyVsExecution() {

        logger.debug("This is in the assembly phase, and will be logged first.");

        Flux.range(1, 3)
                .delayElements(Duration.ofMillis(10))
                .subscribe(elem -> logger.info("Got elem from flux: {}", elem));

        logger.debug("This is also in the assembly phase, and will be logged second.");
        logger.debug("This is the last log statment, since the application exits before the pipeline is executed.");

    }

    @Test
    public void assemblyVsExecution2() throws InterruptedException {
        CountDownLatch countDownLatch = new CountDownLatch(3);
        logger.debug("This is in the assembly phase, and will be logged first.");

        Flux.range(1, 3)
                .delayElements(Duration.ofMillis(10))
                .doOnNext(it -> {
                    logger.debug("Got an element from the flux. Counting down the latch.");
                    countDownLatch.countDown();
                })
                .subscribe(elem -> logger.info("Got elem from flux: {}", elem));

        logger.debug("This is also in the assembly phase, and will be logged second.");
        logger.debug("Since we have a countDownLatch that awaits countdown the program will not exit" +
                ", and we will start executing the pipeline.");

        countDownLatch.await();

    }

    @Test
    public void offLoadWorkToParallelScheduler() throws InterruptedException {
        CountDownLatch countDownLatch = new CountDownLatch(3);

        Flux.range(1, 3)
                .map(number -> {
                    logger.info("Got number: {}", number);

                    logger.info("Offloading work to different thread pool");
                    doBackGroundWork(countDownLatch)
                            .doOnNext(next -> logger.info("Background work complete"))
                            .subscribe();

                    return number;
                })
                .subscribe(elem -> logger.info("Got elem from flux: {}", elem));


        countDownLatch.await();
    }

    private Mono<String> doBackGroundWork(CountDownLatch latch) {
        return Mono.just("Background work completed")
                .doOnNext(next -> logger.info("Starting background work."))
                .delayElement(Duration.ofSeconds(3))
                .map(elem -> {
                    logger.info("Counting down latch");
                    latch.countDown();
                    return elem;
                });
    }
}
