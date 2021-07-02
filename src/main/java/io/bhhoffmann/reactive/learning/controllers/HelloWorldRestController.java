package io.bhhoffmann.reactive.learning.controllers;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
public class HelloWorldRestController {

    private static final Logger logger = LoggerFactory.getLogger(HelloWorldRestController.class);

    @GetMapping(
        path = "/helloWorld",
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    public Mono<String> hello() {
        return Mono.just("{\"greeting\": \"Hello, World!\"}");
    }

    @GetMapping(
        path = "/helloJackson",
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    public Mono<ResponseBody> helloJackson() {
        return Mono.just(new ResponseBody("This object is serialized by Spring using Jackson"));
    }

    @JsonInclude(Include.NON_EMPTY)
    public static class ResponseBody {

        private final String data;

        public ResponseBody(String data) {
            this.data = data;
        }

        public String getData() {
            return data;
        }
    }

}
