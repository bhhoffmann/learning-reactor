package com.example.reactive.threads;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;

@RestController
public class Controller {

    private Logger logger = LoggerFactory.getLogger(getClass());

    private WebClient webClient;

    public Controller(WebClient.Builder builder){
        webClient = builder.build();
    }

    private int status = 0;

    @GetMapping("/processing")
    public Mono<String> process(){
        logger.info("Status before starting processing: {}", status);

        startProcessingInBackground().subscribe();

        return Mono.just("Processing started at server. Starting status: " + status);
    }

    private Mono<String> startProcessingInBackground() {
        return Mono.just("Processing complete")
                .doOnNext(it -> logger.info("startProcessingInBackground() started."))
                .delayElement(Duration.ofSeconds(5))
                .flatMap(thing -> callRemoteService())
                .map(thing -> {
                    status++;
                    logger.info("Completed processing. Status: {}", status);
                    return thing;
                });
    }

    @GetMapping("/processing/read")
    public Mono<String> read(){
        logger.info("Fetching status of processing");

        return Mono.just("Processing status: " + status);
    }

    @GetMapping("/remote")
    public Mono<String> remote(){
        return Mono.just("Pretend this is from a remote service")
                .doOnNext(it -> logger.info("Remote endpoint called"));
    }

    @GetMapping("/test")
    public Mono<String> endpoint(){
        logger.info("Got a request to endpoint /test");
        return doSomeWork()
                .doOnNext(it -> logger.info("doSomeWork() completed"));
    }

    private Mono<String> doSomeWork(){
        logger.info("Starting the methd doSomeWork()");
        return Flux.range(1, 2)
                .map(elem -> {
                    logger.info("Got elem: {}", elem);
                    return elem;
                }).collectList()
                .map(list -> {
                    logger.info("Got a list: {}", list);
                    return Integer.toString(list.get(0));
                })
                .then(callRemoteService())
                .map(response -> {
                    logger.info("Response from remote call: {}", response);
                    return response;
                })
                .map(response -> "Transformed the response to this text.")
                .then(Mono.just("Then just this mono"))
                .doOnNext(it -> logger.info("Did a final then to see if this is done on different thread."))
                .publishOn(Schedulers.parallel())
                .map(thing -> {
                    logger.info("This has been manually publishOn-ed Schedulers.parallel");
                    return "{response: Final asnwer}";
                });
    }

    private Mono<String> callRemoteService(){
        return webClient.get()
                .uri("http://localhost:8080/remote")
                .retrieve()
                .bodyToMono(String.class)
                .doOnNext(it -> logger.info("WebClient got a response"));
    }

}
