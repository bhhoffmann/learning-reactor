package com.example.reactive.learning;

import java.time.Duration;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

public class ConcurrencyWithMono {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Test
    public void combineTwoMono() {

        Mono<String> fooMono = foo(500);
        Mono<String> barMono = bar(200);

        fooMono
            .zipWith(barMono)
            .doOnNext(t -> logger.info("Got combination"))
            .block();

    }

    @Test
    public void combineTwoMonoButWaitUntilTheFirstIsComplete() {

        Mono<String> fooMono = foo(500);
        Mono<String> barMono = bar(200);

        fooMono
            .zipWith(barMono)
            .doOnNext(t -> logger.info("Got combination"))
            .block();

        fooMono
            .zipWhen(aFoo -> barMono)
            .doOnNext(t -> logger.info("Got combination"))
            .block();

    }

    @Test
    public void zipWithEmptyWillCompleteImmediately(){

        Mono<String> fooMono = foo(200);
        Mono<String> empty = Mono.just("will be empty")
                .delayElement(Duration.ofMillis(400))
                .then(Mono.empty());

        fooMono.zipWith(empty)
            .doOnNext(t -> logger.info("Got combination: {}", t))
            .block();

    }

    private Mono<String> foo(int ms) {
        return Mono.just("Foo")
            .doOnNext(elem -> logger.info("foo() sequence started"))
            .delayElement(Duration.ofMillis(ms))
            .doOnNext(elem -> logger.info("foo() sequence complete"));
    }

    private Mono<String> bar(int ms) {
        return Mono.just("Bar")
            .doOnNext(elem -> logger.info("bar() sequence started"))
            .delayElement(Duration.ofMillis(ms))
            .doOnNext(elem -> logger.info("bar() sequence complete"));
    }

}
