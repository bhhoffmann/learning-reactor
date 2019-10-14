package com.example.reactive.threads;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
public class Client {

    private Logger logger = LoggerFactory.getLogger(getClass());

    private WebClient webClient;

    public Client(WebClient.Builder builder){
        webClient = builder.build();
    }

    public Mono<String> callRemoteService(){
        return webClient.get()
                .uri("http://localhost:8080/remote")
                .retrieve()
                .bodyToMono(String.class)
                .doOnNext(it -> logger.info("WebClient got a response"));
    }

}
