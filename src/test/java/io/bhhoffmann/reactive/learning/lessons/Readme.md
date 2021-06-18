# Lessons

This package contains simple code examples aimed at helping people understand the basics of Project Reactor. The lessons
are grouped in separate sub-packages that in some cases also contains a Readme-file with additional comments describing
the code. Most of the examples use log statements to demonstrate their point.

## Definitions

- Sequence = A Mono or Flux

## A brief note on Threads

A unit test is executed on the main thread. No other threads will be used unless we make use of operators that creates
them for us (e.g. Mono.delayElements(), .subscribeOn(Schedulers.parallel()), etc.). When the main thread completes the
program will exit, also stopping any other threads that might have been created and are running. To make sure that our
sequences have time to complete before the unit test completes many of the examples thus use a CountDownLatch or
Thread.sleep().