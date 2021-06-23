# 1 - When to subscribe

If you are writing web applications with Spring WebFlux, you will most likely never subscribe manually yourself, instead
you will return a sequence representing the result of your processing to the framework, and it will subscribe in a
correct way on your behalf. E.g.: If you implement a REST API using @RestController you simply return a Mono/Flux
containing your response.

## Dangerous subscribers

There are a few subscribers that can be used to extract the value out of the sequence, called extractors. These will all
block the thread and take you out of the reactive world. When you work with Project Reactor you rarely want to block
your threads, so these should never be used unless you know exactly what you are doing.

These are: Mono.block(), Mono.toFuture(), Mono.toStream(), Flux.blockFirst(), Flux.blockLast() (and perhaps more)

Reference: https://spring.io/blog/2016/06/13/notes-on-reactive-programming-part-ii-writing-some-code#extractors-the-subscribers-from-the-dark-side
"A good rule of thumb is "never call an extractor". There are some exceptions (otherwise the methods would not exist).
One notable exception is in tests because it’s useful to be able to block to allow results to accumulate."