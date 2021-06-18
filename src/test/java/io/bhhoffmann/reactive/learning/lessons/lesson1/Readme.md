# When to subscribe
If you are writing web applications with Spring WebFlux, you will most likely never subscribe manually yourself,
instead you will return a sequence representing the result of your processing to the framework, and it will subscribe
in a correct way on your behalf. E.g.: If you implement a REST API using @RestController you simply return a Mono/Flux
containing your response.