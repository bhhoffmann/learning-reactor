# 4 - Asynchronous code with Project Reactor

Project Reactor is suitable for writing asynchronous, concurrent and parallel code. The main reason for this is the
functional programming style it provides, which lets you define what should be done and not how and when. The strength
of this declarative approach is that you can ignore the difficulties of asynchronous, concurrent and parallel code when
writing your logic, and only think about it when you decide which thread(s) to execute things on. This means that the
code will look almost identical, whether it is synchronous, asynchronous, concurrent or parallel.

IMPORTANT: Project Reactor lets you write code in a concurrency-agnostic way, but as discussed in the previous lesson it
is up to you (and/or a framework) to use the correct concurrency model for your use case. For instance; Just using Monos
and Fluxes dosen't automatically mean that the code becomes asynchronous or concurrent.

There are some good examples of the advantages of Project Reactor where normal asynchronous Java code is compared with
using Reactor in the reference
documentation: https://projectreactor.io/docs/core/release/reference/#_asynchronicity_to_the_rescue

## Asynchronous APIs and implicit thread switching

You only want to run asynchronous code in situations where it helps performance. One such common situation is when doing
I/O like reading a file from disk or making a network request. I/O operations are performed by other hardware components
than the CPU, which means that the thread you used to start the I/O operation will just be waiting for it to complete,
doing nothing. A common approach is thus to start I/O operations asynchronously, and then have a thread pool that
monitors these I/O operations and processes the result when they are complete. In this way you can continue using your
main thread for other work, and limit the waiting for I/O operations to the specific I/O threads.

There are many APIs for doing I/O that are asynchronous. For example; The Java library provides
java.nio.channels.AsynchronousFileChannel to do operations with files asynchronously, and Spring WebFlux has an HTTP
client, the org.springframework.web.reactive.function.client.WebClient, that lets you do HTTP asynchronously. These APIs
commonly spawns and manages their own thread pools for monitoring the I/O operation they perform, so you don't have to
manage this manually. All you have to do is use the API and process the result in the way the API allows you (provide a
callback, get a CompletableFuture that eventually will contain the result, or get a Mono/Flux that eventually will
contain the result).
