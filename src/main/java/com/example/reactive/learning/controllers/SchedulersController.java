package com.example.reactive.learning.controllers;

import com.example.reactive.learning.Client;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;


@RestController
public class SchedulersController {

    private Logger logger = LoggerFactory.getLogger(getClass());

    private Client client;

    public SchedulersController(Client client) {
        this.client = client;
    }

    @GetMapping("/schedulers")
    public Mono<String> endpoint(){
        logger.info("Got a request to endpoint /test");
        return doSomeWork()
                .doOnNext(it -> logger.info("doSomeWork() completed"));
    }

    private Mono<String> doSomeWork(){
        logger.info("Starting the method doSomeWork()");
        return Flux.range(1, 2)
                .map(elem -> {
                    logger.info("Got elem: {}", elem);
                    return elem;
                }).collectList()
                .map(list -> {
                    logger.info("Got a list: {}", list);
                    return Integer.toString(list.get(0));
                })
                .then(client.callRemoteService("fast"))
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

}
