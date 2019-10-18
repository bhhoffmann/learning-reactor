package com.example.reactive.learning.remote;

import java.time.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
public class SimulatedRemoteService {

    private Logger logger = LoggerFactory.getLogger(getClass());

    @GetMapping("/remote/fast")
    public Mono<String> remote() {
        return Mono.just("Pretend this is from a remote service")
            .doOnNext(it -> logger.info("Remote endpoint called"));
    }

    @GetMapping("/remote/slow/{delaySeconds}")
    public Mono<String> remoteSlow(@PathVariable Integer delaySeconds) {
        return Mono.just("Response from remote server after " + delaySeconds + " seconds.")
            .doOnNext(it -> logger.info("Remote endpoint called. Delaying for {} seconds", delaySeconds))
            .delayElement(Duration.ofSeconds(delaySeconds))
            .doOnNext(it -> logger.info("Remote endpoint returning."));
    }

}
