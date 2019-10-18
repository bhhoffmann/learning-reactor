package com.example.reactive.learning;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class Experimentive {

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Test
    public void collect() throws InterruptedException {

        Callable<String> blockingStuff = () -> {
            return "completed some blocking stuff.";
        };

        Flux.just("red", "white", "blue")
                .flatMap(value ->
                        Mono.fromCallable(blockingStuff)
                        .subscribeOn(Schedulers.elastic()))
                .collect(Result::new, (x, y) -> {
                    logger.info("x: {}, y: {}", x, y);
                })
                .doOnNext(thing -> logger.info("doOnNext: {}", thing))
                .subscribe();

        Thread.sleep(3000);
    }

    @Test
    public void test() throws InterruptedException {
        Mono.delay(Duration.ofMillis(3000))
                .map(d -> "Spring 4")
                .or(Mono.delay(Duration.ofMillis(2000)).map(d -> "Spring 5"))
                .then(Mono.just( " world"))
                .elapsed()
                .subscribe(t -> logger.debug("t {}", t));

        Thread.sleep(3000);
    }

    @Test
    public void fluxSharing() throws InterruptedException {
        Flux<Long> startFlux = Flux.interval(Duration.ofMillis(1000)).share();

        for (int i = 0; i < 2; i++) {
            final int subscriptionNumber = i;
            Flux outputFlux = Flux.from(startFlux);
            outputFlux.subscribe(out -> System.out.println("Flux " + subscriptionNumber + " " + out));
        }

        new CountDownLatch(1).await(10, TimeUnit.SECONDS);
    }

    public class Result {

        private List<String> stufz;

        public Result(){

        }

        public void add(String a, String b){
            stufz.add(a);
        }
    }
}
