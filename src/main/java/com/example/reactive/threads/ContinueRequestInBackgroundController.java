package com.example.reactive.threads;


import java.time.Duration;
import java.util.concurrent.TimeoutException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
public class ContinueRequestInBackgroundController {

    private Logger logger = LoggerFactory.getLogger(getClass());

    private Client client;

    public ContinueRequestInBackgroundController(Client client) {
        this.client = client;
    }

    @GetMapping("/timeout/continue/{timeout}")
    public Mono<String> timeoutButContinueRequest(@PathVariable Integer timeout){

        Mono<String> response = client.callRemoteService("slow/5");
        Mono<String> fallback = Mono.just("fallback")
            .doOnNext(it -> logger.info("Starting fallback with timeout: {}", timeout))
            .delayElement(Duration.ofSeconds(timeout))
            .onErrorResume(err -> {
                if (err instanceof TimeoutException){
                    logger.info("TimeoutException");
                }
                return Mono.just("This is a msg from fallback");
            })
            .doOnNext(it -> logger.info("Returning: {}", it));

        return Flux.merge(fallback, response)
            .doOnNext(it -> logger.info("concat got element: {}", it))
            .next();

        /*
        return Mono.just("Start processing")
                .doOnNext(it -> logger.info(it))
                .flatMap(t -> {
                    return client.callRemoteService("slow/5");
                })
                .timeout(Duration.ofSeconds(3))
                .onErrorResume(err -> {
                    if (err instanceof TimeoutException){
                        logger.info("TimeoutException");
                    }
                    return Mono.just("This is a msg from fallback");
                })
                .doOnNext(it -> logger.info("Returning: {}", it));

         */
    }
}
