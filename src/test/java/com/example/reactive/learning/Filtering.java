package com.example.reactive.learning;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;

public class Filtering {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Test
    public void anyLetsThroughAllElementsIfTrue() {
        Flux.range(1,10)
            .any(nr -> nr == 5)
            .doOnNext(res -> logger.info("Sequence contains 5: {}", res));
    }

}
