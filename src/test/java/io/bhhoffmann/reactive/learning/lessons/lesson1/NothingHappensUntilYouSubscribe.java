package io.bhhoffmann.reactive.learning.lessons.lesson1;

import java.time.Duration;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;

class NothingHappensUntilYouSubscribe {

    private static final Logger logger = LoggerFactory.getLogger(NothingHappensUntilYouSubscribe.class);

    @Test
    void ifYouDoNotSubscribeTheSequenceWillNotBeExecuted() throws InterruptedException {
        Flux.range(1, 10)
            .delayElements(Duration.ofMillis(100))
            .doOnNext(element -> logger.debug("Got elem from flux: {}", element));

        logger.info("Nothing will happen because we never subscribe");
        Thread.sleep(1200);

    }

    @Test
    void subscribingStartsExecutionOfTheSequence() throws InterruptedException {
        Flux<Integer> sequence = Flux.range(1, 10)
            .delayElements(Duration.ofMillis(100))
            .doOnNext(element -> logger.debug("Got elem from flux: {}", element));

        logger.info("We have now defined (assembled) our sequence. Let's subscribe to it in order to execute id");
        sequence.subscribe();

        Thread.sleep(1200);
    }

}
