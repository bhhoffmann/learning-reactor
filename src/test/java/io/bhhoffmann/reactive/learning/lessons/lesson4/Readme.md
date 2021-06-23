# 4 - Concurrency

Project Reactor is concurrency-agnostic, which means that it does not enforce a concurrency model. You as a developer
are free to use the concurrency model you want (single thread, multiple threads, thread pools, etc.). This gives you
complete control, but also requires you to take threading into consideration where relevant.

For controlling threading explicitly Project Reactor provides Schedulers to define and create thread pools along with
methods that easily lets you switch between them (e.g. Mono.subscribeOn()). Schedulers also comes with several common
types of thread pools (Schedulers.parallel(), Schedulers.boundedElastic(), etc.) which covers most use cases, so you
rarely have the need to define custom ones.

NB! Project Reactor also has some operators that controls thread usage implicitly. One notable example is
Mono/Flux.delayElement() which uses a separate thread pool to simulate delays.

## Default threading behaviour

Project Reactor will always try to use as few threads as possible, because that gives you the greatest chance of the
best possible performance. In the absence of any imperative to switch threads, even if the JVM is optimized to handle
threads very efficiently, it is always faster to do computation on a single thread. Reactor has handed you the keys to
control all the asynchronous processing, and it assumes you know what you are doing.

The simplest way to think about it is that unless you (or your framework) explicitly instructs Project Reactor to use
multiple threads it will use only a single thread. For code examples in these lessons this means that everything by
default will run on the main thread unless we specify otherwise.

## Differences between .subscribe() and .block()

We already know that we need to subscribe to a sequence in order to start its execution. The method .subscribe() is a
void method that returns immediately, so if you subscribe using the main thread you would perhaps expect it to
immediately run the next line of code. This, however, depends on which threads you have defined the execution to happen.
As we just discussed, Project Reactor won't use multiple threads unless you specify it, and the actual execution of a
sequence must be run on some thread. As a result, if you use just a single thread, this means that this thread also
needs to execute the sequence. In these situations the thread will go and execute the sequence when you call
.subscribe(), and will only run code after the .subscribe() call when it has completed the execution of a sequence.

On the other hand, if you had specified that the sequence where to execute on a different thread (.subscribeOn(),
.delayElements(), etc.) that thread would be responsible for the execution. In this case the main thread is free to
continue on to the code after the .subscribe() call immediately.

This is different from .block() (which you should never use in production) which will block and wait for the result and
extract it into a variable (like wait()-ing a Future) no matter which thread you run it on. When using only a single
thread (main thread) .subscribe() and .block() can seem very similar, but they are quite different.

## Operators with concurrency

Each separate sequence can be executed on different threads. This means that operators that takes another sequence as an
argument, like .flatMap(), are concurrent in nature.

Example: If you have a Flux of many elements that flows through a .flatMap() each element that reaches the .flatMap()
will cause the sequence inside the .flatMap() to be assembled and executed. If this inner sequence runs on different
threads, or is asynchronous (e.g. makes a network call), the thread running the sequence with the .flatMap() will be
free to start processing the next element. As a consequence, if the inner sequence uses some time, we might have
concurrent executions of multiple inner sequences.