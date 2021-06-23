package io.bhhoffmann.reactive.learning.lessons.lesson2;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

class AssemblyVsExecution {

    private static final Logger logger = LoggerFactory.getLogger(AssemblyVsExecution.class);

    private static Mono<String> foo() {
        logger.info("Assemble foo()");
        return Mono.fromCallable(() -> "data")
            .doOnNext(e -> logger.info("Executing foo()"));
    }

    private static Mono<String> hello() {
        logger.info("Assemble hello()");
        return Mono.fromCallable(() -> "Hello")
            .doOnNext(e -> logger.info("Executing hello()"));
    }

    private static Mono<String> world() {
        logger.info("Assemble world()");
        return Mono.fromCallable(() -> "World")
            .doOnNext(e -> logger.info("Executing world()"));
    }

    private static Mono<String> nested() {
        logger.info("Assemble nested()");
        return hello().zipWith(world())
            .map(tuple -> tuple.getT1() + tuple.getT2())
            .doOnNext(e -> logger.info("Executing nested()"));
    }

    private static String reverse(String s) {
        logger.info("reverse() called");
        return new StringBuilder(s).reverse().toString();
    }

    @Test
    void assemblyHappensBeforeExecution() throws InterruptedException {
        logger.info("--- START ASSEMBLY ---");
        String startValue = "Let's go!";
        Mono<String> sequence = Mono
            .fromCallable(() -> {
                logger.info("Executing .fromCallable()");
                return startValue;
            })
            .then(hello())
            .flatMap(e -> foo())
            .flatMap(e -> nested())
            .map(s -> {
                logger.info("Executing .map()");
                return reverse(s);
            })
            .then(world());

        logger.info("--- START EXECUTION ---");
        sequence.subscribe();
    }

    private static int divideTenBy(int i) {
        if(i == 0) {
            throw new RuntimeException("Can't divide a number by 0");
        }
        return 10 / i;
    }

    private static Mono<String> sequenceThatThrowsExceptionDuringAssembly(int i) {
        int res = divideTenBy(i);
        if(res < 0) {
            return Mono.just("negative");
        } else {
            return Mono.just("positive");
        }
    }

    @Test
    void assemblyPhasePitfall() {
        logger.info("--- START ASSEMBLY ---");
        Mono<String> sequence = Mono.fromCallable(() -> "Hello")
            .then(sequenceThatThrowsExceptionDuringAssembly(0))
            //The error handling below will not work, since exception is thrown during assembly of this sequence
            //not during execution. If you want to fix this you either have to handle the error during assembly
            //(e.g. with try-catch) or make sure that the code that can throw an exception is run during
            //execution phase instead (e.g. using flatMap instead of then, or putting divideTenBy inside the sequence
            //with a fromCallable etc.)
            .onErrorResume(err -> {
                logger.info("Caught error. Message: {}", err.getMessage());
                return Mono.just("Fallback");
            });

        logger.info("--- START EXECUTION ---");
        sequence.subscribe();
    }

}
