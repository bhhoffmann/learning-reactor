package io.bhhoffmann.reactive.learning.controllers;


import io.bhhoffmann.reactive.learning.Client;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;

@RestController
public class ContinueRequestInBackgroundController {

    private Logger logger = LoggerFactory.getLogger(getClass());

    private Client client;

    public ContinueRequestInBackgroundController(Client client) {
        this.client = client;
    }

    @GetMapping("/timeout/continue/{timeout}")
    public Mono<String> timeoutButContinueRequest(@PathVariable Integer timeout) {

        Mono<String> response = client.callRemoteService("slow/5");


        Mono<String> fallback = Mono.just("fallback")
                .doOnNext(it -> logger.info("Starting fallback with timeout: {}", timeout))
                .delayElement(Duration.ofSeconds(timeout))
                .doOnNext(it -> logger.info("Fallback emitted"));

        Flux<String> sharedFlux = Flux.merge(response, fallback).share();

        Flux.from(sharedFlux)
                .last()
                .subscribe(out -> logger.debug("backgroundFlux received: {}", out));

        Flux<String> result = Flux.from(sharedFlux);

        return result.next().doFinally(e -> logger.debug("Returning to user"));

    }
}
