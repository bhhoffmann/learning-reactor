package com.example.reactive.threads;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
public class SimulatedRemoteService {

    private Logger logger = LoggerFactory.getLogger(getClass());

    @GetMapping("/remote")
    public Mono<String> remote(){
        return Mono.just("Pretend this is from a remote service")
                .doOnNext(it -> logger.info("Remote endpoint called"));
    }

}
