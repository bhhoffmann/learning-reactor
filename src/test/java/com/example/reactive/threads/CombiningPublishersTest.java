package com.example.reactive.threads;

import org.junit.After;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.util.Arrays;

public class CombiningPublishersTest {

    private Logger logger = LoggerFactory.getLogger(getClass());

    private Flux<Integer> evenNumbers(){
        return Flux.range(1, 10)
                .delayElements(Duration.ofMillis(10))
                .filter(i -> i % 2 == 0);
    }

    private Flux<Integer> oddNumbers(){
        return Flux.range(1, 10)
                .delayElements(Duration.ofMillis(10))
                .filter(i -> i % 2 != 0);
    }

    private Flux<Integer> numbers(){
        return Flux.range(1, 10)
                .delayElements(Duration.ofMillis(10))
                .filter(i -> i % 2 != 0);
    }

    private Flux<String> letters(){
        return Flux.fromIterable(Arrays.asList("a", "b", "c", "d", "e", "f", "g", "h", "i", "j"))
                .delayElements(Duration.ofMillis(10));
    }

    @After
    public void finish() throws InterruptedException {
        Thread.sleep(3000);
    }

    /**
     * The concat method executes a concatenation of the inputs, forwarding elements emitted by the sources downstream.
     *
     * The concatenation is achieved by sequentially subscribing to the first source then waiting for it
     * to complete before subscribing to the next, and so on until the last source completes. Any error interrupts
     * the sequence immediately and is forwarded downstream.
     */
    @Test
    public void concat(){

    }

    @Test
    public void concatWith(){

    }

    @Test
    public void combineLatest(){

    }

    /**
     * The merge function executes a merging of the data from Publisher sequences contained
     * in an array into an interleaved merged sequence.
     *
     * An interesting thing to note is that, opposed to concat (lazy subscription), the sources are subscribed eagerly.
     */
    @Test
    public void merge(){
        Flux.merge(
                numbers(),
                letters())
                .subscribe(elem -> logger.debug("{}", elem));
    }

    /**
     * The mergeSequential method merges data from Publisher sequences provided in an array into an ordered
     * merged sequence.
     *
     * Unlike concat, sources are subscribed to eagerly.
     */
    @Test
    public void mergeSequential(){

    }

    @Test
    public void mergeDelayError(){

    }

    @Test
    public void mergeWith(){

    }

    @Test
    public void zip(){

    }

    /**
     * The static method zip accumulates multiple sources together, i.e., waits for all the sources to emit
     * one element and combines these elements into an output value (constructed by the provided combinator function).
     *
     * The operator will continue doing so until any of the sources completes:
     */
    @Test
    public void zipWith(){

    }

}
