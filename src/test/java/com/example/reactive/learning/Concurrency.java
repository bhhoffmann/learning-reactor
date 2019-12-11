package com.example.reactive.learning;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class Concurrency {

    private Logger logger = LoggerFactory.getLogger(getClass());

    private Random random = new Random();

    public Mono<Integer> simulatedNetworkCall(Integer input, int delayMillis) {
        Integer response = input;
        return Mono.just(response)
            .delayElement(Duration.ofMillis(delayMillis))
            .doOnNext(res -> logger.info("Simulated network call returning. Input: {}, response: {}", input, response));
    }

    @Test
    public void parallelNetworkCalls() {

        List<Integer> results = Flux.range(1, 3)
            .flatMap(input -> {
                int delay = random.nextInt(1000);
                logger.debug("Starting network call with input {} and delay {} [ms]", input, delay);
                return simulatedNetworkCall(input, delay);
            })
            .doOnNext(res -> logger.debug("Network call {} completed", res))
            .collectList()
            .block();

        logger.debug("Results: {}", results);

    }

    @Test
    public void serialNetworkCalls() {

        List<Integer> networkCallResponses = new ArrayList<>();
        List<Integer> results = Flux.range(1, 3)
            .concatMap(nr -> {
                int delay = random.nextInt(1000);
                if (!networkCallResponses.isEmpty()) {
                    logger.debug("Previous network call responses that can be used as input: {}", networkCallResponses);
                }
                logger.debug("Starting network call {} with delay {} [ms]", nr, delay);
                return simulatedNetworkCall(nr, delay)
                    .doOnNext(networkCallResponses::add)
                    .doOnNext(res -> logger.debug("Network call {} completed", nr));
            })
            .collectList()
            .block();


        logger.debug("Results: {}, network call responses: {}", results, networkCallResponses);

    }

}
