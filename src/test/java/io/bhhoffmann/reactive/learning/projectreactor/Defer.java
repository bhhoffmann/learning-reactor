package io.bhhoffmann.reactive.learning.projectreactor;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

class Defer {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Test
    void sequenceInSwitchIfEmptyIsAssembledBeforeWeKnowWhetherItWillBeUsed() {

        logger.info("Assembling main sequence");
        Mono.just("Main")
            .doOnNext(elem -> logger.info("Sequence running: {}", elem))
            .flatMap(ignore -> Mono.empty())
            .switchIfEmpty(secondarySequence())
            .doOnNext(elem -> logger.info("Result: {}", elem))
            .block();

    }

    @Test
    void useDeferToWaitWithAssemblyOfSecondSequenceUntilWeKnowThatItIsNeeded() {
        logger.info("Assembling main sequence");
        Mono.just("Main")
            .doOnNext(elem -> logger.info("Sequence running: {}", elem))
            .flatMap(ignore -> Mono.empty())
            .switchIfEmpty(Mono.defer(this::secondarySequence))
            .doOnNext(elem -> logger.info("Result: {}", elem))
            .block();
    }

    private Mono<String> secondarySequence() {
        logger.info("Assembling secondary sequence");
        return Mono.just("Secondary")
            .doOnNext(elem -> logger.info("Sequence running: {}", elem));
    }

}
