package com.example.reactive.learning;

import java.util.ArrayDeque;
import java.util.Deque;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class FluxCreate {

    private static final Logger logger = LoggerFactory.getLogger(FluxCreate.class);

    private Deque<String> stack = new ArrayDeque<>();

    @Test
    public void createFluxFromDataRetrievedFromPersistentSource() {

        stack.push("A");
        stack.push("B");
        stack.push("C");

        /*
        .expand(data -> {
                    sink.next(data);
                    return getData();
                }
            )
         */

        Flux.create(sink -> getData()
            .doOnNext(sink::next)
        )
            .doOnNext(data -> logger.info("Flux emitted: {}", data))
            .blockLast();
    }

    private Mono<String> getData() {
        if (!stack.isEmpty()) {
            return Mono.just(stack.pop());
        } else {
            return Mono.empty();
        }
    }

}
