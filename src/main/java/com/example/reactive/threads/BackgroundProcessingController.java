package com.example.reactive.threads;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.time.Duration;

@RestController
public class BackgroundProcessingController {

    private Logger logger = LoggerFactory.getLogger(getClass());

    private int status = 0;
    private Client client;

    public BackgroundProcessingController (Client client){
        this.client = client;
    }


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
                .flatMap(thing -> client.callRemoteService())
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

}
