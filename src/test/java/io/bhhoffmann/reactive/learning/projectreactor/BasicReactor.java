package io.bhhoffmann.reactive.learning.projectreactor;

import io.bhhoffmann.reactive.learning.model.vehicle.Car;
import io.bhhoffmann.reactive.learning.model.vehicle.Chassis;
import io.bhhoffmann.reactive.learning.model.vehicle.Engine;
import io.bhhoffmann.reactive.learning.model.vehicle.Wheel;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

class BasicReactor {

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Test
    void nothingHappensUntilYouSubscribe() throws InterruptedException {

        Flux.range(1, 10)
            .delayElements(Duration.ofMillis(100))
            .doOnNext(it -> logger.debug("Got elem from flux: {}", it))
            .then(Mono.just("ny sekvens"));

        Thread.sleep(3000);

    }

    @Test
    void nothingHappensUntilYouSubscribe2() throws InterruptedException {

        Flux.range(1, 10)
            .delayElements(Duration.ofMillis(100))
            .doOnNext(it -> logger.debug("Got elem from flux: {}", it))
            .doOnComplete(() -> {
                logger.debug("Flux complete");
            })
            .then(Mono.just("Ny sekvens"))
            .subscribe(elem -> logger.info("Subscribe received: {}", elem));

        Thread.sleep(3000);

    }

    @Test
    void assemblyVsExecution() throws InterruptedException {

        logger.debug("This is in the assembly phase, and will be logged first.");

        Flux.range(1, 6)
            .delayElements(Duration.ofMillis(10))
            .subscribe(elem -> logger.info("Got elem from flux: {}", elem));

        logger.debug("This is also in the assembly phase, and will be logged second.");
        logger.debug("This is the last log statment, since the application exits before the pipeline is executed.");

        Thread.sleep(3000);
    }

    @Test
    void assemblyVsExecution2() throws InterruptedException {
        CountDownLatch countDownLatch = new CountDownLatch(3);
        logger.debug("This is in the assembly phase, and will be logged first.");

        Flux.range(1, 3)
            .delayElements(Duration.ofMillis(10))
            .doOnNext(it -> {
                logger.debug("Got an element from the flux. Counting down the latch.");
                countDownLatch.countDown();
            })
            .subscribe(elem -> logger.info("Got elem from flux: {}", elem));

        logger.debug("This is also in the assembly phase, and will be logged second.");
        logger.debug("Since we have a countDownLatch that awaits countdown the program will not exit" +
            ", and we will start executing the pipeline.");

        countDownLatch.await();

    }

    @Test
    void imperativeExample() {

        //The imperative way
        Car myCar = new Car();

        Engine engine = new Engine(1500, 100);
        myCar.setEngine(engine);

        Chassis chassis = manufactureChassis(1).block();
        myCar.setChassis(chassis);

        List<Wheel> wheels = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            Wheel aWheel = new Wheel("Continental", 50, 20);
            wheels.add(aWheel);
        }
        myCar.setWheels(wheels);

        logger.debug("My car: {}", myCar);

    }

    @Test
    void buildingAPipeline() throws InterruptedException {

        Mono.just(new Engine(1500, 100))
            .map(engine -> {
                Car myCar = new Car();
                myCar.setEngine(engine);

                List<Wheel> wheels = new ArrayList<>();
                for (int i = 0; i < 4; i++) {
                    Wheel aWheel = new Wheel("Continental", 50, 20);
                    wheels.add(aWheel);
                }
                myCar.setWheels(wheels);

                return myCar;
            })
            .flatMap(car ->
                manufactureChassis(1)
                    .map(chassis -> {
                        car.setChassis(chassis);
                        return car;
                    })
            )
            .subscribe(car -> logger.debug("My car: {}", car));

        Thread.sleep(3000);

    }

    @Test
    void moreAdvancedPipeline() throws InterruptedException {

        Mono.just(new Engine(1500, 100))
            .flatMap(engine -> {
                Car myCar = new Car();
                myCar.setEngine(engine);

                return Flux.range(1, 4)
                    .flatMap(number -> manufactureWheel(1))
                    .collectList()
                    .map(wheels -> {
                        myCar.setWheels(wheels);
                        return myCar;
                    });

            })
            .flatMap(car ->
                manufactureChassis(1)
                    .map(chassis -> {
                        car.setChassis(chassis);
                        return car;
                    })
            )
            .subscribe(car -> logger.debug("My car: {}", car));

        Thread.sleep(3000);
    }

    private Mono<Engine> manufactureEngine(Integer engineType) {
        if (engineType == 1) {
            return Mono.just(new Engine(1500, 100));
        } else {
            return Mono.just(new Engine(2000, 150));
        }

    }

    private Mono<Chassis> manufactureChassis(Integer chassisType) {
        if (chassisType == 1) {
            return Mono.just(new Chassis(1234, "SUV"));
        } else {
            return Mono.just(new Chassis(5678, "Sport"));
        }
    }

    private Mono<Wheel> manufactureWheel(Integer wheelType) {
        if (wheelType == 1) {
            return Mono.just(new Wheel("Continental", 50, 20));
        } else {
            return Mono.just(new Wheel("Continental", 70, 30));
        }
    }


    @Test
    void subscribing() throws InterruptedException {

        Mono.just("Print this text")
            .map(text -> {
                Flux.range(1, 3)
                    .delayElements(Duration.ofMillis(500))
                    .doOnComplete(() -> {
                        logger.debug("Flux completed");
                    })
                    .subscribe(elem -> logger.debug("From flux: {}", elem));

                return text;
            })
            .subscribe(elem -> logger.debug("From mono: {}", elem));

        Thread.sleep(5000);
    }

}
