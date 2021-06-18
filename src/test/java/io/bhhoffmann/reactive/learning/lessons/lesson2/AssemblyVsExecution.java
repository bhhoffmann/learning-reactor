package io.bhhoffmann.reactive.learning.lessons.lesson2;

import java.util.concurrent.CountDownLatch;
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
        CountDownLatch countDownLatch = new CountDownLatch(1);

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
            .then(world())
            .doOnNext(e -> countDownLatch.countDown());

        logger.info("--- START EXECUTION ---");
        sequence.subscribe();

        logger.info("Waiting for latch");
        countDownLatch.await();
        logger.info("Complete");
    }

}
