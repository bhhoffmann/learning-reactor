# Assembly vs execution phase

In the first lesson we saw that nothing happened until we subscribed. This is because Project Reactor works in phases:

1. The assembly phase: We define our sequence, which describes the flow of data and how it should be manipulated.
2. The execution phase: Once we have described what we want to do we can start executing it.

The unit test in this lesson demonstrates with log statements when assembly happens and when execution happens.

1. When a method is called it is run line by line (as all Java code)
2. Any line with the classes Mono/Flux (a sequence) is also run. Since we compose our sequences by chaining operators
   (methods on Mono/Flux) the entire sequence can be seen as a single line of code (we just put it across multiple lines
   to make it readable)
3. When a line of code that defines a sequence is run, the sequence is ASSEMBLED
    - If the sequence contains operators that combines it with other sequences these will also be assembled Example:
      Mono.zipWith(), Mono.then(), Mono.merge() etc.
    - NB! Mono.just() creates a Mono at assembly time (can be important if you have a method call in it). If you want to
      create a Mono based on a return value of a normal method at execution time you can use Mono.fromCallable()
4. Assembly and execution of the sequence in this test:
    - There are two .then() operators that combine sequences (starts a new when the one above completes)
      so both hello() and world() will be run during assembly.
    - Then execution starts when the sequence is subscribed to.
    - The .fromCallable() is executed
    - The sequence returned from hello() in the first .then() is executed
    - The first .flatMap() is executed. Since it contains another sequence (foo()) this sequence is now
      assembeled and then immediately executed (sequences in flatMaps are automatically subscribed to)
    - The same happens with the second .flatMap(). Since it contains a sequence that itself consists of two separate
      sequences combined with .zipWith() (in nested()) all of these are assembled and then executed.
    - The .map() operator is executed. It uses a normal method to do some work.
    - The sequence in the second .then() is now executed (world())
    - Finally the .doOnNext() is executed and the execution of the main sequence is complete.