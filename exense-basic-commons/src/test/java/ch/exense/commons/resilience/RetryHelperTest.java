package ch.exense.commons.resilience;

import org.junit.Test;

import java.net.ConnectException;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.*;

public class RetryHelperTest {

    @Test
    public void testSuccessfulOperationOnFirstAttempt() throws Exception {
        // Given
        String expectedResult = "success";
        AtomicInteger attemptCount = new AtomicInteger(0);

        // When
        String result = RetryHelper.executeWithRetryOnExceptions(
                () -> {
                    attemptCount.incrementAndGet();
                    return expectedResult;
                },
                3,
                100,
                RetryHelper.COMMON_NETWORK_EXCEPTIONS,
                "Test operation"
        );

        // Then
        assertEquals(expectedResult, result);
        assertEquals(1, attemptCount.get());
    }

    @Test
    public void testSuccessAfterRetries() throws Exception {
        // Given
        AtomicInteger attemptCount = new AtomicInteger(0);
        String expectedResult = "success";

        // When
        String result = RetryHelper.executeWithRetryOnExceptions(
                () -> {
                    int attempt = attemptCount.incrementAndGet();
                    if (attempt < 3) {
                        throw new SocketTimeoutException("Simulated timeout");
                    }
                    return expectedResult;
                },
                3,
                50,
                RetryHelper.COMMON_NETWORK_EXCEPTIONS,
                "Test operation with retries"
        );

        // Then
        assertEquals(expectedResult, result);
        assertEquals("Should succeed on third attempt", 3, attemptCount.get());
    }

    @Test
    public void testSocketTimeoutException() {
        // Given
        AtomicInteger attemptCount = new AtomicInteger(0);

        // When/Then
        try {
            RetryHelper.executeWithRetryOnExceptions(
                    () -> {
                        attemptCount.incrementAndGet();
                        throw new SocketTimeoutException("Connection timeout");
                    },
                    3,
                    50,
                    RetryHelper.COMMON_NETWORK_EXCEPTIONS,
                    "Test timeout"
            );
            fail("Should have thrown SocketTimeoutException");
        } catch (SocketTimeoutException e) {
            assertEquals("Connection timeout", e.getMessage());
            assertEquals("Should attempt 1 initial + 3 retries", 4, attemptCount.get());
        } catch (Exception e) {
            fail("Unexpected exception type: " + e.getClass().getName());
        }
    }

    @Test
    public void testSocketException() {
        // Given
        AtomicInteger attemptCount = new AtomicInteger(0);

        // When/Then
        try {
            RetryHelper.executeWithRetryOnExceptions(
                    () -> {
                        attemptCount.incrementAndGet();
                        throw new SocketException("Connection reset");
                    },
                    2,
                    50,
                    RetryHelper.COMMON_NETWORK_EXCEPTIONS,
                    "Test socket exception"
            );
            fail("Should have thrown SocketException");
        } catch (SocketException e) {
            assertEquals("Connection reset", e.getMessage());
            assertEquals("Should attempt 1 initial + 2 retries", 3, attemptCount.get());
        } catch (Exception e) {
            fail("Unexpected exception type: " + e.getClass().getName());
        }
    }

    @Test
    public void testConnectException() {
        // Given
        AtomicInteger attemptCount = new AtomicInteger(0);

        // When/Then
        try {
            RetryHelper.executeWithRetryOnExceptions(
                    () -> {
                        attemptCount.incrementAndGet();
                        throw new ConnectException("Connection refused");
                    },
                    3,
                    50,
                    RetryHelper.COMMON_NETWORK_EXCEPTIONS,
                    "Test connect exception"
            );
            fail("Should have thrown ConnectException");
        } catch (ConnectException e) {
            assertEquals("Connection refused", e.getMessage());
            assertEquals(4, attemptCount.get());
        } catch (Exception e) {
            fail("Unexpected exception type: " + e.getClass().getName());
        }
    }

    @Test
    public void testUnknownHostException() {
        // Given
        AtomicInteger attemptCount = new AtomicInteger(0);

        // When/Then
        try {
            RetryHelper.executeWithRetryOnExceptions(
                    () -> {
                        attemptCount.incrementAndGet();
                        throw new UnknownHostException("unknown.host.example.com");
                    },
                    2,
                    50,
                    RetryHelper.COMMON_NETWORK_EXCEPTIONS,
                    "Test unknown host"
            );
            fail("Should have thrown UnknownHostException");
        } catch (UnknownHostException e) {
            assertEquals("unknown.host.example.com", e.getMessage());
            assertEquals(3, attemptCount.get());
        } catch (Exception e) {
            fail("Unexpected exception type: " + e.getClass().getName());
        }
    }

    @Test
    public void testNonRetryableException() {
        // Given
        AtomicInteger attemptCount = new AtomicInteger(0);

        // When/Then
        try {
            RetryHelper.executeWithRetryOnExceptions(
                    () -> {
                        attemptCount.incrementAndGet();
                        throw new IllegalArgumentException("Invalid argument");
                    },
                    3,
                    50,
                    RetryHelper.COMMON_NETWORK_EXCEPTIONS,
                    "Test non-retryable exception"
            );
            fail("Should have thrown IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertEquals("Invalid argument", e.getMessage());
            assertEquals("Should not retry for non-retryable exception", 1, attemptCount.get());
        } catch (Exception e) {
            fail("Unexpected exception type: " + e.getClass().getName());
        }
    }

    @Test
    public void testCustomRetryableExceptions() {
        // Given
        AtomicInteger attemptCount = new AtomicInteger(0);

        // When/Then
        try {
            RetryHelper.executeWithRetryOnExceptions(
                    () -> {
                        attemptCount.incrementAndGet();
                        throw new CustomException("Custom error");
                    },
                    2,
                    50,
                    Collections.singletonList(CustomException.class),
                    "Test custom exception"
            );
            fail("Should have thrown CustomException");
        } catch (CustomException e) {
            assertEquals("Custom error", e.getMessage());
            assertEquals("Should retry for custom exception", 3, attemptCount.get());
        } catch (Exception e) {
            fail("Unexpected exception type: " + e.getClass().getName());
        }
    }

    @Test
    public void testExceptionWithRetryableCause() {
        // Given
        AtomicInteger attemptCount = new AtomicInteger(0);
        SocketTimeoutException cause = new SocketTimeoutException("Timeout");

        // When/Then
        try {
            RetryHelper.executeWithRetryOnExceptions(
                    () -> {
                        attemptCount.incrementAndGet();
                        throw new RuntimeException("Wrapper exception", cause);
                    },
                    2,
                    50,
                    RetryHelper.COMMON_NETWORK_EXCEPTIONS,
                    "Test exception with retryable cause"
            );
            fail("Should have thrown RuntimeException");
        } catch (RuntimeException e) {
            assertEquals("Wrapper exception", e.getMessage());
            assertSame(cause, e.getCause());
            assertEquals("Should retry when cause is retryable", 3, attemptCount.get());
        } catch (Exception e) {
            fail("Unexpected exception type: " + e.getClass().getName());
        }
    }

    @Test
    public void testExceptionWithNonRetryableCause() {
        // Given
        AtomicInteger attemptCount = new AtomicInteger(0);
        IllegalStateException cause = new IllegalStateException("Invalid state");

        // When/Then
        try {
            RetryHelper.executeWithRetryOnExceptions(
                    () -> {
                        attemptCount.incrementAndGet();
                        throw new RuntimeException("Wrapper exception", cause);
                    },
                    3,
                    50,
                    RetryHelper.COMMON_NETWORK_EXCEPTIONS,
                    "Test exception with non-retryable cause"
            );
            fail("Should have thrown RuntimeException");
        } catch (RuntimeException e) {
            assertEquals("Wrapper exception", e.getMessage());
            assertEquals("Should not retry when cause is not retryable", 1, attemptCount.get());
        } catch (Exception e) {
            fail("Unexpected exception type: " + e.getClass().getName());
        }
    }

    @Test
    public void testZeroRetries() {
        // Given
        AtomicInteger attemptCount = new AtomicInteger(0);

        // When/Then
        try {
            RetryHelper.executeWithRetryOnExceptions(
                    () -> {
                        attemptCount.incrementAndGet();
                        throw new SocketTimeoutException("Timeout");
                    },
                    0,
                    50,
                    RetryHelper.COMMON_NETWORK_EXCEPTIONS,
                    "Test zero retries"
            );
            fail("Should have thrown IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertEquals("expected message: maxRetries must be greater than 0", "maxRetries must be greater than 0", e.getMessage());
        } catch (Exception e) {
            fail("Unexpected exception type: " + e.getClass().getName());
        }
    }

    @Test(timeout = 2000)
    public void testRetryDelayIsApplied() {
        // Given
        AtomicInteger attemptCount = new AtomicInteger(0);
        long startTime = System.currentTimeMillis();

        // When/Then
        try {
            RetryHelper.executeWithRetryOnExceptions(
                    () -> {
                        attemptCount.incrementAndGet();
                        throw new SocketTimeoutException("Timeout");
                    },
                    2,
                    200,
                    RetryHelper.COMMON_NETWORK_EXCEPTIONS,
                    "Test retry delay"
            );
            fail("Should have thrown SocketTimeoutException");
        } catch (SocketTimeoutException e) {
            long elapsedTime = System.currentTimeMillis() - startTime;
            assertEquals(3, attemptCount.get());
            assertTrue("Should wait at least 400ms (2 retries * 200ms)", elapsedTime >= 400);
        } catch (Exception e) {
            fail("Unexpected exception type: " + e.getClass().getName());
        }
    }

    @Test
    public void testInterruptedDuringRetry() {
        // Given
        AtomicInteger attemptCount = new AtomicInteger(0);
        Thread testThread = Thread.currentThread();

        // Schedule interrupt after first failure
        new Thread(() -> {
            try {
                Thread.sleep(100);
                testThread.interrupt();
            } catch (InterruptedException e) {
                // Ignore
            }
        }).start();

        // When/Then
        try {
            RetryHelper.executeWithRetryOnExceptions(
                    () -> {
                        attemptCount.incrementAndGet();
                        throw new SocketTimeoutException("Timeout");
                    },
                    3,
                    500,
                    RetryHelper.COMMON_NETWORK_EXCEPTIONS,
                    "Test interrupted"
            );
            fail("Should have thrown RuntimeException");
        } catch (RuntimeException e) {
            assertTrue("Exception message should contain 'Interrupted'",
                    e.getMessage().contains("Interrupted"));
            assertTrue("Interrupt flag should be cleared", Thread.interrupted());
            assertTrue("Should stop retrying after interrupt", attemptCount.get() < 4);
        } catch (Exception e) {
            fail("Unexpected exception type: " + e.getClass().getName());
        }
    }

    @Test
    public void testMultipleExceptionTypes() throws Exception {
        // Given
        AtomicInteger attemptCount = new AtomicInteger(0);

        // When
        String result = RetryHelper.executeWithRetryOnExceptions(
                () -> {
                    int attempt = attemptCount.incrementAndGet();
                    switch (attempt) {
                        case 1:
                            throw new SocketTimeoutException("Timeout");
                        case 2:
                            throw new ConnectException("Connection refused");
                        case 3:
                            throw new UnknownHostException("Unknown host");
                        default:
                            return "success";
                    }
                },
                5,
                50,
                RetryHelper.COMMON_NETWORK_EXCEPTIONS,
                "Test multiple exception types"
        );

        // Then
        assertEquals("success", result);
        assertEquals(4, attemptCount.get());
    }

    // Custom exception for testing
    private static class CustomException extends Exception {
        public CustomException(String message) {
            super(message);
        }
    }
}