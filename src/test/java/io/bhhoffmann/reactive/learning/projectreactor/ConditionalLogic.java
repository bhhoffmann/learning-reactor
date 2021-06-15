package io.bhhoffmann.reactive.learning.projectreactor;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

class ConditionalLogic {

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Test
    void useDataIfPresent() {

        boolean someCondition = true;

        Mono<String> dataNeededInSomeSituations;
        if (someCondition) {
            dataNeededInSomeSituations = Mono.just("");
        }

        Mono<String> noData = Mono.just("noData");
        Mono<String> data = Mono.just("Data");

        Mono.zip(data, noData)
            .doOnNext(it -> logger.info("zip emitted an element: {}", it))
            .map(tuple -> {
                logger.info("T1: {}", tuple.getT1());
                logger.info("T2: {}", tuple.getT2());
                return tuple;
            }).block();


    }

}
