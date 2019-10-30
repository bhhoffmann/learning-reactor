package com.example.reactive.learning;

import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Random;
import java.util.concurrent.*;

@Ignore
public class ReactorTest {
    private static Logger logger = LoggerFactory.getLogger(ReactorTest.class);
    Random rand = new Random();
    int counter = 0;

    @Test
    public void calendar(){
        Calendar lastDate = Calendar.getInstance();
        lastDate.add(Calendar.DATE, -14);
        logger.debug("{}", lastDate.getTimeInMillis());
    }

    @Test
    public void mapToString(){

    }

    @Test
    public void flatMap(){
        Mono.just("data")
                .flatMap(data -> {
                    if(true){
                        return pretendToDoSomething().then(Mono.just(data));
                    } else {
                        return Mono.just(data);
                    }
                })
                .block();

        Mono.empty()
                .flatMap(empty -> {
                    return pretendToDoSomething();
                }).block();
    }

    private Mono<Boolean> pretendToDoSomething(){
        return Mono.just(Boolean.TRUE)
                .doOnNext(thing -> logger.debug("Did something. Thing: {}", thing));
    }

    @Test
    public void fluxCreate(){
        var numberList = Arrays.asList(1, 2, 3, 4, 5);
        Flux.create(emitter -> {
            numberList.forEach(number -> {
                emitter.next(number);
            });
        }).subscribe(n -> System.out.println(n));
    }

    @Test
    public void fluxFrom(){
        CountDownLatch latch = new CountDownLatch(1);

        Flux.from(requestThatReturnsMono())
                .map(n -> n*2)
                .doOnTerminate(latch::countDown)
                .subscribe(System.out::println);
    }

    @Test
    public void loopEquivalent() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);

        processNumber(1)
                .expand(number -> {
                    if(number <= 5){
                        return processNumber(number);
                    }else {
                        return Mono.empty();
                    }
                })
                .doOnTerminate(latch::countDown)
                .subscribe(n -> logger.debug("Subscriber got {}", n));

        /*
        processNumber(1)
                .flatMap(processResult -> {
                    logger.debug("flatMap: {}", processResult);
                    return processNumber(processResult);
                })
                .repeatWhenEmpty(Repeat.times(5))
                .doOnTerminate(latch::countDown)
                .subscribe(n -> logger.debug("Subscriber got {}", n));

         */

        latch.await();
    }

    @Test
    public void filterAndFlatMapMany() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);

        requestThatReturnsMono()
                .map(number -> {
                    logger.debug("Number: {}", number);
                    return number;
                })
                .filter(number -> number <= 3)
                .map(number -> {
                    logger.debug("Filtered number: {}", number);
                    return number;
                })
                .flatMapMany(number -> requestThatReturnsMono())
                .doOnTerminate(latch::countDown)
                .subscribe(n -> logger.debug("Subscriber got {}", n));

        latch.await();

    }

    private Mono<Integer> requestThatReturnsMono(){
        counter++;
        logger.debug("Request returning {}", counter);
        return Mono.just(counter);
    }

    /**
     * CONDITIONAL LOGIC (loop break equivalent): Process elements emitted from a flux until a condition is satisfied.
     *
     */
    @Test
    public void fluxBreakEquivalent(){
        boolean complete = false;
        var numberList = Arrays.asList(1, 2, 3, 4, 5);
        Flux.fromIterable(numberList)
                .flatMap(this::processNumber)
                .takeUntil(result -> result == 6)
                .collectList()
                .map(numbers -> {
                    var lastNumber = numbers.get(numbers.size() - 1);
                    System.out.println("List of collected elements: " + numbers);
                    System.out.println("Last number: " + lastNumber);
                    return lastNumber;
                })
                .subscribe(elem -> System.out.println("The subscriber received: " + elem));
    }

    private Mono<Integer> processNumber(Integer number){
        logger.debug("Processing number {}", number);
        return Mono.just(number*2);
    }

    @Test
    public void recursiveCall(){
        Mono.just(randomInt())
                .map(number -> {
                    logger.debug("Emitted: {}", number);
                    if(number == 1){
                        logger.debug("Call method recursively");
                        recursiveCall();
                        logger.debug("Exited recursive call, number: {}", number);
                    }else {
                        logger.debug("Finish");
                    }
                    return number;
                }).subscribe(System.out::println);
    }

    private int randomInt(){
        return rand.nextInt(2);
    }



    @Test
    public void fluxConcat(){
        int min = 1;
        int max = 5;

        Flux<Integer> evenNumbers = Flux
                .range(min, max)
                .filter(x -> x % 2 == 0); // i.e. 2, 4

        Flux<Integer> oddNumbers = Flux
                .range(min, max)
                .filter(x -> x % 2 > 0);  // ie. 1, 3, 5

        Flux<Integer> fluxOfIntegers = Flux.concat(
                evenNumbers,
                oddNumbers);

        fluxOfIntegers.subscribe(elem -> System.out.println(elem));
    }

    @Test
    public void processOnMainThread(){
        Flux.fromIterable(Arrays.asList(1, 2, 3))
                .map(n -> {
                    logger.debug("Doing something with {} on thread: {}", n, Thread.currentThread().getName());
                    return n;
                })
                .subscribe(elem -> logger.debug("{}", elem));
    }

    @Test
    public void processOnDifferentThread(){
        Flux.fromIterable(Arrays.asList(1, 2, 3))
                .map(n -> {
                    logger.debug("Doing something with {} on thread: {}", n, Thread.currentThread().getName());
                    return n;
                })
                .subscribeOn(Schedulers.parallel())
                .collectList()
                .doOnSuccess(list -> logger.debug("Result {} on thread: {}", list, Thread.currentThread().getName()))
                .block();
    }

    @Test
    public void example() throws InterruptedException {
        Scheduler s = Schedulers.newParallel("parallel-scheduler", 4);

        final Flux<String> flux = Flux
                .range(1, 2)
                .map(i -> 10 + i)
                .publishOn(s)
                .map(i -> "value " + i);

        Thread t = new Thread(() -> flux.subscribe(System.out::println));
        t.start();
        t.join();
    }

    @Test
    public void concurrency() throws InterruptedException {
        Mono<List<Integer>> fluxOfIntegers = Flux.fromIterable(Arrays.asList(1, 2, 3))
                .flatMap(number -> {
                    logger.debug("Got request {} on thread {}", number, Thread.currentThread().getName());
                    return processWithRandomDelay(number);
                })
                .collectList();

        Thread t = new Thread(() -> fluxOfIntegers.subscribe(list -> logger.debug("Result {} on thread: {}", list, Thread.currentThread().getName())));
        t.start();
        t.join();

    }


    private Mono<Integer> processWithRandomDelay(Integer number){
        return Mono.just(simulatedNetworkCall(number))
                .publishOn(Schedulers.single());
    }

    private Integer simulatedNetworkCall(Integer number){
        logger.debug("Making network call on thread {}", Thread.currentThread().getName());
        int duration = rand.nextInt(5)*100;
        //logger.debug("Simulating network call that takes {} milliseconds.", duration);

        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

        scheduler.schedule(() -> number, duration, TimeUnit.MILLISECONDS);

        return number;
    }

    @Test
    public void flatMapWithoutChangingScheduler() {
        Flux.range(1, 3)
                .map(n -> identityWithThreadLogging(n, "map1"))
                .flatMap(n -> Mono.just(n)
                        .map(nn -> identityWithThreadLogging(nn, "mono"))
                )
                .subscribe(n -> {
                    this.identityWithThreadLogging(n, "subscribe");
                    System.out.println(n);
                });
    }

    @Test
    public void flatMapWithChangingScheduler() {
        Flux.range(1, 3)
                .map(n -> identityWithThreadLogging(n, "map1"))
                .flatMap(n -> Mono.just(n)
                        .map(nn -> identityWithThreadLogging(nn, "mono"))
                        .subscribeOn(Schedulers.elastic())
                )
                .subscribe(n -> {
                    this.identityWithThreadLogging(n, "subscribe");
                    System.out.println(n);
                });
    }


    private <T> T identityWithThreadLogging(T el, String operation) {
        System.out.println(operation + " -- " + el + " -- " +
                Thread.currentThread().getName());
        return el;
    }
}
