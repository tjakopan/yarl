package hr.tjakopan.yarl.retry;

import hr.tjakopan.yarl.Context;
import hr.tjakopan.yarl.test.helpers.PolicyUtils;
import org.junit.Test;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import static hr.tjakopan.yarl.Functions.fromConsumer;
import static hr.tjakopan.yarl.Functions.fromConsumer4;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

public class WaitAndRetryForeverHandleExceptionTest {
  @Test
  public void shouldThrowWhenSleepDurationsIsNull() {
    //noinspection ConstantConditions
    assertThatExceptionOfType(NullPointerException.class).isThrownBy(() ->
      RetryPolicy.<Void>builder()
        .handle(ArithmeticException.class)
        .waitAndRetryForever(null,
          fromConsumer4(r -> d -> (i, c) -> {
          })))
      .withMessageContaining("sleepDurationProvider");
  }

  @Test
  public void shouldThrowWhenOnRetryActionIsNull() {
    //noinspection ConstantConditions
    assertThatExceptionOfType(NullPointerException.class).isThrownBy(() ->
      RetryPolicy.<Void>builder()
        .handle(ArithmeticException.class)
        .waitAndRetryForever((i, r, c) -> Duration.ZERO, null))
      .withMessageContaining("onRetry");
  }

  @Test
  public void shouldNotThrowRegardlessOfHowManyTimesTheSpecifiedExceptionIsRaised() {
    final var policy = RetryPolicy.<Void>builder()
      .handle(ArithmeticException.class)
      .waitAndRetryForever((i, r, c) -> Duration.ZERO);

    PolicyUtils.raiseExceptions(policy, 3, i -> new ArithmeticException());
  }

  @Test
  public void shouldNotThrowRegardlessOfHowManyTimesOneOfTheSpecifiedExceptionIsRaised() {
    final var policy = RetryPolicy.<Void>builder()
      .handle(ArithmeticException.class)
      .handle(IllegalArgumentException.class)
      .waitAndRetryForever((i, r, c) -> Duration.ZERO);

    PolicyUtils.raiseExceptions(policy, 3, i -> new IllegalArgumentException());
  }

  @Test
  public void shouldThrowWhenExceptionThrownIsNotTheSpecifiedExceptionType() {
    final var policy = RetryPolicy.<Void>builder()
      .handle(ArithmeticException.class)
      .waitAndRetryForever((i, r, c) -> Duration.ZERO);

    assertThatExceptionOfType(NullPointerException.class).isThrownBy(() ->
      PolicyUtils.raiseExceptions(policy, 1, i -> new NullPointerException()));
  }

  @Test
  public void shouldThrowWhenExceptionThrownIsNotOneOfTheSpecifiedExceptionTypes() {
    final var policy = RetryPolicy.<Void>builder()
      .handle(ArithmeticException.class)
      .handle(IllegalArgumentException.class)
      .waitAndRetryForever((i, r, c) -> Duration.ZERO);

    assertThatExceptionOfType(NullPointerException.class).isThrownBy(() ->
      PolicyUtils.raiseExceptions(policy, 1, i -> new NullPointerException()));
  }

  @Test
  public void shouldThrowWhenSpecifiedExceptionPredicateIsNotSatisfied() {
    final var policy = RetryPolicy.<Void>builder()
      .handle(ArithmeticException.class, e -> false)
      .waitAndRetryForever((i, r, c) -> Duration.ZERO);

    assertThatExceptionOfType(ArithmeticException.class).isThrownBy(() ->
      PolicyUtils.raiseExceptions(policy, 1, i -> new ArithmeticException()));
  }

  @Test
  public void shouldThrowWhenNoneOfTheSpecifiedExceptionPredicatesAreSatisfied() {
    final var policy = RetryPolicy.<Void>builder()
      .handle(ArithmeticException.class, e -> false)
      .handle(IllegalArgumentException.class, e -> false)
      .waitAndRetryForever((i, r, c) -> Duration.ZERO);

    assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(() ->
      PolicyUtils.raiseExceptions(policy, 1, i -> new IllegalArgumentException()));
  }

  @Test
  public void shouldNotThrowWhenSpecifiedExceptionPredicateIsSatisfied() {
    final var policy = RetryPolicy.<Void>builder()
      .handle(ArithmeticException.class, e -> true)
      .waitAndRetryForever((i, r, c) -> Duration.ofMillis(1));

    PolicyUtils.raiseExceptions(policy, 1, i -> new ArithmeticException());
  }

  @Test
  public void shouldNotThrowWhenOneOfTheSpecifiedExceptionPredicatesAreSatisfied() {
    final var policy = RetryPolicy.<Void>builder()
      .handle(ArithmeticException.class, e -> true)
      .handle(IllegalArgumentException.class, e -> true)
      .waitAndRetryForever((i, r, c) -> Duration.ofMillis(1));

    PolicyUtils.raiseExceptions(policy, 1, i -> new IllegalArgumentException());
  }

  @Test
  public void shouldCallOnRetryOnEachRetryWithTheCurrentException() {
    final var expectedExceptions = List.of("Exception #1", "Exception #2", "Exception #3");
    final var retryExceptions = new ArrayList<Throwable>(3);
    final var policy = RetryPolicy.<Void>builder()
      .handle(ArithmeticException.class)
      .waitAndRetryForever((i, r, c) -> Duration.ZERO,
        fromConsumer4(outcome -> d -> (i, c) -> outcome.onFailure(fromConsumer(retryExceptions::add))));

    PolicyUtils.raiseExceptions(policy, 3, i -> new ArithmeticException("Exception #" + i));

    assertThat(retryExceptions.stream()
      .map(Throwable::getMessage)
      .collect(Collectors.toList()))
      .containsExactlyElementsOf(expectedExceptions);
  }

  @Test
  public void shouldCallOnRetryOnEachRetryWithTheCurrentRetryCount() {
    final var expectedRetryCounts = List.of(1, 2, 3);
    final var retryCounts = new ArrayList<Integer>(3);
    final var policy = RetryPolicy.<Void>builder()
      .handle(ArithmeticException.class)
      .waitAndRetryForever((i, r, c) -> Duration.ZERO,
        fromConsumer4(r -> d -> (retryCount, c) -> retryCounts.add(retryCount)));

    PolicyUtils.raiseExceptions(policy, 3, i -> new ArithmeticException());

    assertThat(retryCounts).containsExactlyElementsOf(expectedRetryCounts);
  }

  @Test
  public void shouldNotCallOnRetryWhenNoRetriesArePerformed() {
    final var onRetryCalled = new AtomicBoolean(false);
    final var policy = RetryPolicy.<Void>builder()
      .handle(ArithmeticException.class)
      .waitAndRetryForever((i, r, c) -> Duration.ofMillis(1),
        fromConsumer4(r -> d -> (i, c) -> onRetryCalled.set(true)));

    assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(() ->
      PolicyUtils.raiseExceptions(policy, 1, i -> new IllegalArgumentException()));
    assertThat(onRetryCalled.get()).isFalse();
  }

  @Test
  public void shouldCreateNewContextForEachCallToExecute() {
    final var contextValue = new AtomicReference<String>();
    //noinspection ConstantConditions
    final var policy = RetryPolicy.<Void>builder()
      .handle(ArithmeticException.class)
      .waitAndRetryForever((i, r, c) -> Duration.ofMillis(1),
        fromConsumer4(r -> d -> (i, context) -> contextValue.set(context.get("key").toString())));

    PolicyUtils.raiseExceptions(policy,
      Context.of(new HashMap<>() {{
        put("key", "original_value");
      }}),
      1,
      i -> new ArithmeticException());

    assertThat(contextValue.get()).isEqualTo("original_value");

    PolicyUtils.raiseExceptions(policy,
      Context.of(new HashMap<>() {{
        put("key", "new_value");
      }}),
      1,
      i -> new ArithmeticException());

    assertThat(contextValue.get()).isEqualTo("new_value");
  }

  @Test
  public void shouldCalculateRetryDurationsFromCurrentRetryAttemptAndDurationProvider() {
    final var expectedRetryWaits = List.of(Duration.ofMillis(2), Duration.ofMillis(4), Duration.ofMillis(8),
      Duration.ofMillis(16), Duration.ofMillis(32));
    final var actualRetryWaits = new ArrayList<Duration>(5);
    final var policy = RetryPolicy.<Void>builder()
      .handle(ArithmeticException.class)
      .waitAndRetryForever((retryAttempt, r, c) -> Duration.ofMillis((long) Math.pow(2.0, retryAttempt)),
        fromConsumer4(r -> duration -> (i, c) -> actualRetryWaits.add(duration)));

    PolicyUtils.raiseExceptions(policy, 5, i -> new ArithmeticException());

    assertThat(actualRetryWaits).containsExactlyElementsOf(expectedRetryWaits);
  }

  @Test
  public void shouldBeAbleToCalculateRetryDurationsBasedOnTheHandledFault() {
    final var expectedRetryWaits = new HashMap<RuntimeException, Duration>() {{
      put(new ArithmeticException(), Duration.ofMillis(2));
      put(new IllegalArgumentException(), Duration.ofMillis(4));
    }};
    final var actualRetryWaits = new ArrayList<Duration>(2);
    //noinspection SuspiciousMethodCalls
    final var policy = RetryPolicy.<Void>builder()
      .handle(RuntimeException.class)
      .waitAndRetryForever((i, outcome, c) -> outcome.fold(v -> Duration.ZERO, expectedRetryWaits::get),
        fromConsumer4(r -> duration -> (i, c) -> actualRetryWaits.add(duration)));

    final var iterator = expectedRetryWaits.keySet().iterator();
    policy.execute(() -> {
      if (iterator.hasNext()) {
        throw iterator.next();
      }
      return null;
    });

    assertThat(actualRetryWaits).containsExactlyElementsOf(expectedRetryWaits.values());
  }

  @Test
  public void shouldBeAbleToPassRetryDurationFromExecutionToSleepDurationProviderViaContext() {
    final var expectedRetryDuration = Duration.ofMillis(1);
    final var actualRetryDuration = new AtomicReference<Duration>();
    final var defaultRetryAfter = Duration.ofMillis(30);
    final var policy = RetryPolicy.<Void>builder()
      .handle(ArithmeticException.class)
      .waitAndRetryForever((i, r, context) -> context.containsKey("RetryAfter")
          ? (Duration) context.get("RetryAfter")
          : defaultRetryAfter,
        fromConsumer4(r -> duration -> (i, c) -> actualRetryDuration.set(duration)));

    final var failedOnce = new AtomicBoolean(false);
    policy.execute(new HashMap<>() {{
                     put("RetryAfter", defaultRetryAfter);
                   }},
      (Context context) -> {
        context.put("RetryAfter", expectedRetryDuration);
        if (!failedOnce.get()) {
          failedOnce.set(true);
          throw new ArithmeticException();
        }
        return null;
      });

    assertThat(actualRetryDuration.get()).isEqualTo(expectedRetryDuration);
  }
}
