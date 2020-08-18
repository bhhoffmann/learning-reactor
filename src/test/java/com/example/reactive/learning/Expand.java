package com.example.reactive.learning;

import com.example.reactive.learning.model.Node;
import java.time.Duration;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.List;
import java.util.Random;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class Expand {

    private static final Logger logger = LoggerFactory.getLogger(Expand.class);

    @Test
    public void expand() {

        List<String> names = Arrays.asList("James", "Sophie", "Carl");

        Node n1 = new Node("1");
        Node n2 = new Node("2");

        Mono.just(n1)
            .expand(node -> {
                logger.info("Got node with id {}", node.getId());
                if ("1".equals(node.getId())) {
                    return Mono.just(n2);
                } else {
                    return Mono.empty();
                }
            }).blockLast();
    }

    @Test
    public void expandTimed() {

        //Dette er en stack
        Deque<Integer> totalBatch = new ArrayDeque<>();
        Random random = new Random();
        random.ints(100, 0, 10).forEach(totalBatch::push);

        logger.info("Total batch size: {}", totalBatch.size());
        List<Integer> results = new ArrayList<>();

        getBatch(5, totalBatch)
            .expand(batch -> {
                logger.info("Processing batch: {}", batch);
                return Flux.fromIterable(batch)
                    .concatMap(nr -> process(nr))
                    .doOnNext(result -> results.add(result))
                    .then(
                        getBatch(5, totalBatch)
                            .zipWith(timer())
                            .map(tuple -> tuple.getT1())
                    );
            })
            .blockLast();

        logger.info("Results: {}", results);

    }

    private Mono<Integer> timer() {
        return Mono.just(1).delayElement(Duration.ofSeconds(1));
    }

    private Mono<Integer> process(Integer nr) {
        logger.info("Processing element: {}", nr);
        return Mono.just(nr * 2);
    }

    private Mono<List<Integer>> getBatch(int batchSize, Deque<Integer> totalBatch) {
        List<Integer> batch = new ArrayList<>();
        for (int i = 0; i < batchSize; i++) {
            if(!totalBatch.isEmpty()) {
                batch.add(totalBatch.pop());
            } else {
                break;
            }
        }

        if (!batch.isEmpty()) {
            return Mono.just(batch);
        } else {
            return Mono.empty();
        }
    }

}
