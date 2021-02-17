package com.example.reactive.learning;

import java.time.Duration;
import java.util.Random;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class ConcurrencyWithFlux {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final Random random = new Random();

    public Mono<Integer> simulatedNetworkCall(Integer input, int delayMillis) {
        return Mono.just(input)
            .doOnNext(nr -> logger.info("{} - Start, delay {}", nr, delayMillis))
            .delayElement(Duration.ofMillis(delayMillis))
            .doOnNext(res -> logger.info("{} - Complete, delay {} [ms]", input, delayMillis));
    }

    @Test
    public void flatMapIsConcurrentByDefault() {
        Flux.range(1, 10)
            .flatMap(nr -> simulatedNetworkCall(nr, random.nextInt(1000)))
            .doOnNext(res -> logger.info("{} - Returned", res))
            .blockLast();
    }

    @Test
    public void flatMapConcurrencyCanBeControlled() {
        Flux.range(1, 10)
            .flatMap(nr -> simulatedNetworkCall(nr, random.nextInt(1000)), 2)
            .doOnNext(res -> logger.info("{} - Returned", res))
            .blockLast();
    }

    @Test
    public void flatMapSequentialIsConcurrentButOrderIsPreservedForFollowingOperators() {
        Flux.range(1, 10)
            .flatMapSequential(nr -> simulatedNetworkCall(nr, random.nextInt(1000)))
            .doOnNext(res -> logger.info("{} - Returned", res))
            .blockLast();
    }

    @Test
    public void concatMapWillRunInSequenceWithOrderPreserved() {
        Flux.range(1, 10)
            .concatMap(nr -> simulatedNetworkCall(nr, random.nextInt(1000)))
            .doOnNext(res -> logger.info("{} - Returned", res))
            .blockLast();
    }

}
