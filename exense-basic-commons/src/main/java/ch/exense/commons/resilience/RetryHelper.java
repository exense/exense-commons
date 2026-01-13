package ch.exense.commons.resilience;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.*;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

public class RetryHelper {

    private static final Logger logger = LoggerFactory.getLogger(RetryHelper.class);

    /**
     * Common network and IO exceptions that are typically retryable.
     * These represent transient failures that may succeed on retry.
     */
    public static final List<Class<? extends Exception>> COMMON_NETWORK_EXCEPTIONS = List.of(
            SocketTimeoutException.class,     // Read/connect timeout
            SocketException.class,            // Generic socket errors (connection reset, broken pipe, etc.)
            ConnectException.class,           // Connection refused or failed
            NoRouteToHostException.class,     // Network unreachable
            UnknownHostException.class,       // DNS resolution failure
            PortUnreachableException.class,   // ICMP port unreachable
            ProtocolException.class,          // HTTP protocol error
            HttpRetryException.class          // HTTP request should be retried
    );

    /**
     * Functional interface for operations that can throw checked exceptions
     *
     * @param <T> the return type of the operation
     */
    @FunctionalInterface
    public interface CheckedSupplier<T> {
        T get() throws Exception;
    }

    /**
     * Executes an operation with retry logic
     *
     * @param operation the operation to execute
     * @param maxRetries maximum number of retry attempts (not including the initial attempt)
     * @param retryDelayMs delay in milliseconds between retry attempts
     * @param retryableExceptions list of exceptions classes for which a retry is allowed
     * @param operationDescription description of the operation for logging purposes
     * @param <T> the return type of the operation
     * @return the result of the operation
     * @throws Exception if the operation fails after all retries, or if the exception is not retryable
     */
    public static <T> T executeWithRetryOnExceptions(
            CheckedSupplier<T> operation,
            int maxRetries,
            long retryDelayMs,
            List<Class<? extends Exception>> retryableExceptions,
            String operationDescription) throws Exception {
        Objects.requireNonNull(operation);
        Objects.requireNonNull(retryableExceptions);
        Objects.requireNonNull(operationDescription);
        if (maxRetries <= 0) {
            throw new IllegalArgumentException("maxRetries must be greater than 0");
        }
        Exception lastException = null;

        for (int attempt = 0; attempt <= maxRetries; attempt++) {
            try {
                return operation.get();
            } catch (Exception e) {
                lastException = e;

                // Check if we should retry
                if (isRetryableException(e, retryableExceptions)) {
                    if (attempt < maxRetries) {
                        logger.warn("{} failed (attempt {}/{}). Retrying in {}ms...",
                                operationDescription, attempt + 1, maxRetries + 1, retryDelayMs, e);
                        if (retryDelayMs > 0) {
                            try {
                                Thread.sleep(retryDelayMs);
                            } catch (InterruptedException ie) {
                                Thread.currentThread().interrupt();
                                throw new RuntimeException("Interrupted while waiting to retry " + operationDescription, ie);
                            }
                        }
                    } else {
                        logger.warn("{} failed (attempt {}/{}). Max retries reached, propagating the error...",
                                operationDescription, attempt + 1, maxRetries + 1, e);
                        throw e;
                    }
                } else {
                    logger.warn("{} failed (attempt {}/{}). Retries not allowed for this error, propagating the error..",
                            operationDescription, attempt + 1, maxRetries + 1, e);
                    throw e;
                }
            }
        }
        // This should never be reached, but required for compilation
        throw lastException;
    }

    private static boolean isRetryableException(Exception e, List<Class<? extends Exception>> retryableExceptions) {
        // Check the exception itself and its cause
        return Stream.of(e, e.getCause())
                .filter(Objects::nonNull)
                .anyMatch(throwable -> retryableExceptions.stream()
                        .anyMatch(exceptionClass -> exceptionClass.isInstance(throwable)));
    }
}
