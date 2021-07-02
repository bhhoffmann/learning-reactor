package io.bhhoffmann.reactive.learning.projectreactor;

import io.bhhoffmann.reactive.learning.model.Node;
import java.time.Duration;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

class Expand {

    private static final Logger logger = LoggerFactory.getLogger(Expand.class);

    @Test
    void imperativeVersion() {
        boolean loop = true;
        String result = "start";
        List<String> listWithResults = new ArrayList<>();
        while (loop) {
            result = doSomething(result);
            listWithResults.add(result);
            logger.info("result={}", result);
            if (result.equals("Blue")) {
                logger.info("Got the result that breaks the loop.");
                loop = false;
            }
        }
        logger.info("List of all results: {}", listWithResults);
        // Do something more with the list of results
    }

    private String doSomething(String input) {
        List<String> data = Arrays.asList("Green", "Yellow", "Blue", "Red");
        int index = data.indexOf(input);
        if (index == -1) {
            index = 0;
        } else if (index < 3) {
            index = index + 1;
        }
        return data.get(index);
    }

    @Test
    void expandVersion() {
        doSomethingMono("start")
            .expand(result -> {
                if (result.equals("Blue")) {
                    logger.info("Got the result that breaks the loop.");
                    return Mono.empty();
                } else {
                    return doSomethingMono(result);
                }
            })
            // Each result is emitted here
            .doOnNext(result -> logger.info("result={}", result))
            .collectList() // You do not have to collect it as a list unless you need all the elements in a list
            .doOnNext(listWithResults -> logger.info("List of all results: {}", listWithResults))
            // Do something more with the list of results
            .subscribe();
    }

    private Mono<String> doSomethingMono(String input) {
        List<String> data = Arrays.asList("Green", "Yellow", "Blue", "Red");
        int index = data.indexOf(input);
        if (index == -1) {
            index = 0;
        } else if (index < 3) {
            index++;
        }
        return Mono.just(data.get(index));
    }

    @Test
    void expand() {

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
    void expandTimed() {

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
            if (!totalBatch.isEmpty()) {
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


    @Test
    void breakByLoopCount() {
        List<Integer> nrs = Arrays.asList(1, 2, 3, 4);
        Map<Integer, Integer> cnt = new HashMap<>();

        Mono.just(1)
            .expand(nr -> {
                cnt.compute(nr, (k, v) -> (v == null) ? 1 : v + 1);
                System.out.println(cnt);
                if (nr < 0) {
                    return Mono.empty();
                } else if (cnt.getOrDefault(nr, 0) > 2) {
                    throw new RuntimeException("INFINITE LOOP");
                } else {
                    return Mono.just(1);
                }
            })
            .doOnNext(nr -> System.out.println("THING: " + nr))
            .blockLast();

    }

}
