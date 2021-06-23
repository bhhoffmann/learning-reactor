# Lessons

This package contains simple code examples aimed at helping people understand the basics of Project Reactor. The lessons
are grouped in separate sub-packages that in some cases also contains a Readme-file with additional comments describing
the code and the concepts it tries to demonstrate. Most of the examples use log statements to make their point.

## Definitions

- Sequence = A Mono or Flux
- Operator = A method on Mono or Flux

## Similarities between Project Reactor and other things in the Java world

The Mono and Flux objects that form the base of Project Reactor can in many ways be compared to Java's
CompletableFuture, just with many more methods (operators), enabling you to write almost all your code by chaining these
methods (which is also what gives rise to the functional style of programming).

With all its additional methods Project Reactor also resembles the Java Stream API, with .filter(), .collect(), .map(),
etc.

## Recommended reading

From the Spring blog - Notes on Reactive Programming Part II: Writing Some Code:
https://spring.io/blog/2016/06/13/notes-on-reactive-programming-part-ii-writing-some-code#extractors-the-subscribers-from-the-dark-side

YouTube video with a good explanation of the most common pitfalls Sergei Egorov - Don’t be Homer Simpson with your
Reactor!:
https://www.youtube.com/watch?v=eE5-dhP44dw

## A brief note on Threads

In these lessons we use junit tests to run code examples individually. These are executed as a separate process, like
running a Java program with a main function. This means that everything runs on the main thread created for the process,
unless you explicitly write code that spawns other threads and executes code on them. When the main thread completes the
program will exit, also stopping any other threads that might have been created and are running. To make sure that our
sequences have time to complete before the unit test completes many of the examples use a CountDownLatch or
Thread.sleep().

Project Reactor has several operators that will switch the execution to different threads, both explicit ones
(e.g. Mono.subscribeOn(Schedulers.parallel())) and implicit ones (e.g. Mono.delayElements())