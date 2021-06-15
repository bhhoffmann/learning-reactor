package io.bhhoffmann.reactive.learning.projectreactor;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

class ErrorHandling {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Test
    void stopFluxIfOneElementFails() {
        Flux.range(1, 10)
            .map(nr -> {
                if (nr == 5) {
                    throw new RuntimeException("Something failed");
                } else {
                    return nr;
                }
            })
            .onErrorResume(err ->
                Mono.just("Caught an error")
                    .doOnNext(msg -> logger.info("{}", msg))
                    .then(Mono.empty())
            )
            .doOnNext(thing -> logger.info("Result: {}", thing))
            .blockLast();
    }

    @Test
    void doNotStopFluxIfOneElementFails() {
        Flux.range(1, 10)
            .concatMap(nr -> errorHandledAsEmpty(nr))
            .doOnNext(thing -> logger.info("Result: {}", thing))
            .blockLast();
    }

    Mono<Integer> errorHandledAsEmpty(Integer nr) {
        return Mono.just(nr)
            .map(i -> {
                if (nr == 5) {
                    throw new RuntimeException("Something failed");
                } else {
                    return nr;
                }
            })
            .doOnError(err -> logger.info("Error: {}", err.getClass()))
            .onErrorResume(err -> Mono.empty());
    }

}
