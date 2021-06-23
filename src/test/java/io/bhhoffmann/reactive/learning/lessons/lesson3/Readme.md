# 3 - Signals

Signals are used to control the flow of data in a sequence. These signals are what enables some of the more advanced
reactive capabilities of Project Reactor (remember, it is called reactive programming) like backpressure, but we won't
go into details about that here.

The different types of signals are:

- subscribe: Sent up the sequence when something subscribes to it.
- request: Used by a subscriber to tell the publisher that it is ready for data.
- next: Used by the publisher to send data to the subscriber.
- complete: Used by the publisher to tell the subscriber that the sequence is complete.
- error: Used to signal an error (e.g. when an exception is thrown)

A Mono/Flux is both a publisher and a subscriber. For each operator we use we create a new Mono/Flux that subscribes to
the previous one.

The operator .log() can be used to see the flow of signals at any point in a sequence. Running the example
signalsAreUsedToControlDataFlow() containing a simple sequence we can see from the logs how the signals are used:

- A subscribe signal is sent when we run .subscribe().
- The default subscriber .subscribe() creates sends a request(unbounded) signal, saying that it wants all the data that
  the publisher can give it.
- The publisher (Flux.range()) sends a next signals containing the data (integers) which is propagated down the
  sequence.
- When the publisher has published all its data it sends the complete signal, which ends the sequence.

## Operators are executed based on signals

Since signals control the execution of a sequence, they also control the execution of the various operators. Most of the
time we don't have to think about this because most of the operators we use work on the data in the sequence and are
executed by next signals. There are however some situations where we do have to think about it:

- Handling errors signals: When an exception is thrown in a sequence an error signal is sent. If we want to handle this
  exception (like a catch-block), we need to use operators that are executed on error signals
  (onErrorResume(), onErrorMap(), etc.)
- Handling complete signals: There are operators that are only run when a complete signal is received, like .then()
  for a Mono and .thenMany() for a Flux.
- Handling empty sequences: There are also operators that execute only when the above sequence is empty (produces no
  data). This can happen if we for instance use a .filter() to remove elements from the sequence that does not fit the
  filter predicate. As with the error signal there are separate operators that are executed on complete signals (
  switchIfEmpty(), defaultIfEmpty(), etc.)

There are also several operators that let us perform side effects for the different signals, the doOnX() operators
(doOnNext(), doOnError(), doOnComplete(), etc.)
