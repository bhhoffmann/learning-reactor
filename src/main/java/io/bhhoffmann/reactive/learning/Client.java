package io.bhhoffmann.reactive.learning;

import java.time.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;

@Service
public class Client {

    private Logger logger = LoggerFactory.getLogger(getClass());

    private WebClient webClient;

    private HttpClient httpClient = HttpClient.create().wiretap(true);

    public Client(WebClient.Builder builder){
        webClient = builder
            .clientConnector(new ReactorClientHttpConnector(httpClient))
            .build();
    }

    public Mono<String> callRemoteService(String endpoint){
        return webClient.get()
                .uri("http://localhost:8083/remote/" + endpoint)
                .retrieve()
                .bodyToMono(String.class)
                .timeout(Duration.ofSeconds(10))
                .doOnNext(it -> logger.info("WebClient got a response"));
    }

}
