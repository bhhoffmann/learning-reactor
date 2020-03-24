package com.example.reactive.learning;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

public class UsingEmpty {

    Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * Several reactor operators return Mono.empty, which can be thought of as "nothing". When a Mono.empty is returned
     * the reactor sequence/pipeline will be completed (complete signal sent) and none of the normal operators will be
     * run with the Mono.empty. "Special" operators like .switchIfEmpty and .defaultIfEmpty can be used to "catch" a
     * Mono.empty and trigger some alternative behaviour.
     * <p>
     * A good example Mono.empty, why it exists and how it can be useful is illustrated below with the .filter operator.
     * This operator filters out all elements that does not match the predicate. We only want to apply the operators
     * after the filter for elements that match. Thus, if the filter does not match it will return a Mono.empty,
     * effectively skipping those operators that were meant for matching elements. However, it is important to remember
     * that Mono.empty will complete the entire sequence, so in the cases that the .filter does not get a match we need
     * to define a fallback action for these no match cases. In this example the .switchIfEmpty operator has been done
     * to achieve this, which can be thought as a "if we did not get a match do this instead".
     */
    @Test
    public void doSomethingElseIfConditionNotMet() {

        Balloon red = new Balloon("red", 1);
        Balloon blue = new Balloon("blue", 2);

        Mono.just(red)//Try to run this test with both the red and blue balloon
            .filter(b -> b.getColor().equals(
                "red")) //We filter on the balloons color. If the predicate is not true this will return Mono.empty
            .doOnNext(b -> logger.info(
                "Got a red balloon")) //All operators from here until switchIfEmpty will only be applied for matching elements.
            .map(Balloon::getColor)
            .switchIfEmpty(
                Mono.just("Color did not match.")) //This will start ONLY of we got a Mono.empty from the filter.
            .doOnNext(result -> logger.info("Result: {}",
                result)) //Operators from this point and out will apply to everything (data from filter and switch)
            .block();

    }

    public class Balloon {

        private String color;
        private int size;

        public Balloon() {
        }

        public Balloon(String color, int size) {
            this.color = color;
            this.size = size;
        }

        public String getColor() {
            return color;
        }

        public void setColor(String color) {
            this.color = color;
        }

        public int getSize() {
            return size;
        }

        public void setSize(int size) {
            this.size = size;
        }
    }

}
