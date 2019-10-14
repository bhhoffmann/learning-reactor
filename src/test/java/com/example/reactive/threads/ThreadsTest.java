package com.example.reactive.threads;

import org.apache.juli.logging.Log;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.concurrent.CountDownLatch;

public class ThreadsTest {

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Test
    public void offLoadWorkToParallelScheduler() throws InterruptedException {
        CountDownLatch countDownLatch = new CountDownLatch(3);

        Flux.range(1,3)
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
