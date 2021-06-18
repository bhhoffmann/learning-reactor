package io.bhhoffmann.reactive.learning.lessons.lesson3;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

class Signals {

    private static final Logger logger = LoggerFactory.getLogger(Signals.class);

    @Test
    void signalsAreUsedToControlDataFlow() {
        Flux<Integer> sequence = Flux.range(1, 10)
            .log() //This will log all signals that passes through this point
            .doOnNext(element -> logger.debug("Got elem from flux: {}", element));

        sequence.subscribe();
    }

    @Test
    void errorSignalsCanBeHandledByErrorOperators() {
        Flux<Integer> sequence = Flux.range(1, 10)
            .doOnNext(nr -> {
                if (nr == 5) {
                    throw new RuntimeException("ERROR!");
                }
            })
            .log()
            .onErrorResume(err -> {
                logger.warn("Got an error! Error message: {}", err.getMessage());
                return Mono.just(1000);
            })
            .doOnNext(element -> logger.debug("Got elem from flux: {}", element));

        sequence.subscribe();
    }

    @Test
    void completeSignalsCanBeHandledByCompleteOperators() {
        Flux<Integer> sequence = Flux.range(1, 10)
            .log() //We do nothing with the elements from the first range, only log the signals
            .thenMany(Flux.range(11, 20))
            .doOnNext(element -> logger.debug("Got elem from flux: {}", element)); //We print the second range

        sequence.subscribe();
    }

    @Test
    void emptySequencesCanBeHandledByOperators() {
        logger.info("Empty flux");
        Flux.range(1, 10)
            .filter(nr -> nr > 10) //The sequence after filter will be empty
            .log()
            .defaultIfEmpty(1000)
            .doOnNext(element -> logger.debug("Got elem from flux: {}", element))
            .subscribe();

        logger.info("Empty Mono");
        Mono.just("A")
            .filter(letter -> "B".equals(letter))
            .log()
            .defaultIfEmpty("C")
            .doOnNext(element -> logger.info("Got elem from mono: {}", element))
            .subscribe();
    }

    @Test
    void weCanCreateEmptySequencesManually() {
        logger.info("Empty Flux");
        Flux.range(1, 10)
            .flatMap(letter -> Flux.empty()) //We need a flatMap because the lambda's return value is a Mono
            .log()
            .defaultIfEmpty(1000)
            .doOnNext(element -> logger.info("Got elem from flux: {}", element))
            .subscribe();

        logger.info("Empty Mono");
        Mono.just("A")
            .flatMap(letter -> Mono.empty()) //We need a flatMap because the lambda's return value is a Mono
            .log()
            .defaultIfEmpty("C")
            .doOnNext(element -> logger.info("Got elem from mono: {}", element))
            .subscribe();
    }

    @Test
    void sideEffectsBasedOnSignals() {
        Mono.just("Hello")
            .doOnNext(e -> logger.info("Logging the element like this is a side effect. Element: {}", e))
            .subscribe();

        Mono.empty()
            .doOnEach(signal -> logger.info("doOnEach lets us catch all signals"))
            .subscribe();

        Mono.just("Let's create some errors")
            .map(e -> {
                throw new RuntimeException("ERROR");
            })
            .doOnError(err -> logger.info("We can choose to just log errors like this. Error message: {}",
                err.getMessage()))
            .subscribe();
    }

}
