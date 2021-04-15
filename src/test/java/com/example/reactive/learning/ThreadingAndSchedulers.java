package com.example.reactive.learning;

import java.time.Duration;
import java.util.concurrent.CountDownLatch;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

public class ThreadingAndSchedulers {

    private Logger logger = LoggerFactory.getLogger(getClass());


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

    @Test
    public void fromCallableRunsOnSpecifiedScheduler() {
        logger.info("Starting test on main thread");

        Mono
            .fromCallable(() -> {
                logger.info("Kick off two tasks");
                task("1");
                task("2");
                return "tasks complete";
            })
            .subscribeOn(Schedulers.elastic())
            .doOnNext(elem -> logger.info("The entire mono should run on different thread. Elem: {}", elem))
            .block();

        logger.info("Completing test on main thread");
    }

    private String task(String id) {
        logger.info("Running id");
        return "id";
    }

}
