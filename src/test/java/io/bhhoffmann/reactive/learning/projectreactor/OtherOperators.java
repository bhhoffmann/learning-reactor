package io.bhhoffmann.reactive.learning.projectreactor;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;

class OtherOperators {

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Test
    void window() {
        Flux.just("a", "b", "c", "d", "e", "f", "g", "h", "i")
            .window(3)
            .doOnNext(elem -> logger.debug("Element from window(3): {}", elem))
            .concatMap(fluxOf3Letters -> {
                return fluxOf3Letters.map(elem -> elem.toUpperCase());
            })
            .doOnNext(elem -> logger.debug("Final element: {}", elem))
            .blockLast();
    }

}
