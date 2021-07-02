package io.bhhoffmann.reactive.learning.logging;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import org.slf4j.MDC;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Signal;
import reactor.core.publisher.SignalType;
import reactor.util.context.Context;
import reactor.util.context.ContextView;

public final class LogUtil {

    public static final String CONTEXT_MAP = "context-map";
    public static final String AUDIT_MARKER = "AUDIT";

    private LogUtil() {
        throw new IllegalStateException("Static utility class");
    }

    public static Mono<Void> logFirst(Runnable log) {
        return Mono.deferContextual(ctx -> Mono.fromRunnable(() -> logWithContext(ctx, log)));
    }

    public static void logWithContext(ContextView ctx, Runnable log) {
        Optional<Map<String, String>> maybeContextMap = ctx.getOrEmpty(CONTEXT_MAP);

        if (maybeContextMap.isEmpty()) {
            log.run();
        } else {
            logWithContext(maybeContextMap.get(), log);
        }
    }

    public static void logWithContext(Map<String, String> mapToContext, Runnable log) {
        Objects.requireNonNull(mapToContext);
        Objects.requireNonNull(log);

        MDC.setContextMap(mapToContext);
        try {
            log.run();
        } finally {
            MDC.clear();
        }
    }

    public static <T> Consumer<Signal<T>> logOnNext(Consumer<T> log) {
        return signal -> {
            if (signal.getType() == SignalType.ON_NEXT) {
                logConsumer(log).accept(signal);
            }
        };
    }

    public static <T> Consumer<Signal<T>> logOnComplete(Consumer<T> log) {
        return signal -> {
            if (signal.getType() == SignalType.ON_COMPLETE) {
                logConsumer(log).accept(signal);
            }
        };
    }

    public static <T> Consumer<Signal<T>> logConsumer(Consumer<T> log) {
        return signal -> {
            Optional<Map<String, String>> maybeContextMap = signal.getContextView().getOrEmpty(CONTEXT_MAP);

            if (maybeContextMap.isEmpty()) {
                log.accept(signal.get());
            } else {
                MDC.setContextMap(maybeContextMap.get());
                try {
                    log.accept(signal.get());
                } finally {
                    MDC.clear();
                }
            }
        };
    }

    public static <T> Consumer<Signal<T>> logOnError(Consumer<Throwable> log) {
        return signal -> {
            if (signal.isOnError()) {
                Optional<Map<String, String>> maybeContextMap
                    = signal.getContextView().getOrEmpty(CONTEXT_MAP);

                if (maybeContextMap.isEmpty()) {
                    log.accept(signal.getThrowable());
                } else {
                    MDC.setContextMap(maybeContextMap.get());
                    try {
                        log.accept(signal.getThrowable());
                    } finally {
                        MDC.clear();
                    }
                }
            }
        };
    }

    public static Function<Context, Context> put(String key, String value) {
        return ctx -> {
            Optional<Map<String, String>> maybeContextMap =
                ctx.getOrEmpty(CONTEXT_MAP);

            if (maybeContextMap.isPresent()) {
                maybeContextMap.get().put(key, value);
                return ctx;
            } else {
                Map<String, String> ctxMap = new HashMap<>();
                ctxMap.put(key, value);

                return ctx.put(CONTEXT_MAP, ctxMap);
            }
        };
    }

    public static Function<Context, Context> put(Map<String, String> map) {
        return ctx -> {
            Optional<Map<String, String>> maybeContextMap =
                ctx.getOrEmpty(CONTEXT_MAP);

            if (maybeContextMap.isPresent()) {
                maybeContextMap.get().putAll(map);
                return ctx;
            } else {
                Map<String, String> ctxMap = new HashMap<>(map);
                return ctx.put(CONTEXT_MAP, ctxMap);
            }
        };
    }

    public static String get(ContextView ctx, String key) {
        Optional<Map<String, String>> maybeContextMap = ctx.getOrEmpty(CONTEXT_MAP);
        return maybeContextMap.map(stringStringMap -> stringStringMap.get(key)).orElse(null);
    }

    public static Optional<Map<String, String>> getContextMap(ContextView contextView) {
        return contextView.getOrEmpty(CONTEXT_MAP);
    }

}
