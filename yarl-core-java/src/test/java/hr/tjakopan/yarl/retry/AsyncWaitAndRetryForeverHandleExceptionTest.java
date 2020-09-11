package hr.tjakopan.yarl.retry;

import hr.tjakopan.yarl.Context;
import hr.tjakopan.yarl.DelegateResult;
import hr.tjakopan.yarl.test.helpers.AsyncPolicyUtils;
import kotlin.Unit;
import kotlin.jvm.functions.Function0;
import org.junit.Test;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import static hr.tjakopan.yarl.Functions.fromConsumer;
import static hr.tjakopan.yarl.Functions.fromConsumer4Async;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

public class AsyncWaitAndRetryForeverHandleExceptionTest {
  @Test
  public void shouldThrowWhenSleepDurationProviderIsNull() {
    //noinspection ConstantConditions
    assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(() ->
      AsyncRetryPolicy.<Void>builder()
        .handle(ArithmeticException.class)
        .waitAndRetryForever(null, fromConsumer4Async(r -> d -> (i, c) -> {
        })))
      .withMessageContaining("sleepDurationProvider");
  }

  @Test
  public void shouldThrowWhenOnRetryActionIsNull() {
    //noinspection ConstantConditions
    assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(() ->
      AsyncRetryPolicy.<Void>builder()
        .handle(ArithmeticException.class)
        .waitAndRetryForever((i, r, c) -> Duration.ZERO, null))
      .withMessageContaining("onRetry");
  }

  @Test
  public void shouldNotThrowRegardlessOfHowManyTimesTheSpecifiedExceptionIsRaised() {
    final var policy = AsyncRetryPolicy.<Void>builder()
      .handle(ArithmeticException.class)
      .waitAndRetryForever((i, r, c) -> Duration.ZERO);

    AsyncPolicyUtils.raiseExceptions(policy, 3, i -> new ArithmeticException())
      .join();
  }

  @Test
  public void shouldNotThrowRegardlessOfHowManyTimesOneOfTheSpecifiedExceptionIsRaised() {
    final var policy = AsyncRetryPolicy.<Void>builder()
      .handle(ArithmeticException.class)
      .handle(IllegalArgumentException.class)
      .waitAndRetryForever((i, r, c) -> Duration.ZERO);

    AsyncPolicyUtils.raiseExceptions(policy, 3, i -> new IllegalArgumentException())
      .join();
  }

  @Test
  public void shouldThrowWhenExceptionThrownIsNotTheSpecifiedExceptionType() {
    final var policy = AsyncRetryPolicy.<Void>builder()
      .handle(ArithmeticException.class)
      .waitAndRetryForever((i, r, c) -> Duration.ZERO);

    assertThatExceptionOfType(CompletionException.class).isThrownBy(() ->
      AsyncPolicyUtils.raiseExceptions(policy, 1, i -> new NullPointerException())
        .join())
      .withCauseInstanceOf(NullPointerException.class);
  }

  @Test
  public void shouldThrowWhenExceptionThrownIsNotOneOfTheSpecifiedExceptionTypes() {
    final var policy = AsyncRetryPolicy.<Void>builder()
      .handle(ArithmeticException.class)
      .handle(IllegalArgumentException.class)
      .waitAndRetryForever((i, r, c) -> Duration.ZERO);

    assertThatExceptionOfType(CompletionException.class).isThrownBy(() ->
      AsyncPolicyUtils.raiseExceptions(policy, 1, i -> new NullPointerException())
        .join())
      .withCauseInstanceOf(NullPointerException.class);
  }

  @Test
  public void shouldThrowWhenSpecifiedExceptionPredicateIsNotSatisfied() {
    final var policy = AsyncRetryPolicy.<Void>builder()
      .handle(ArithmeticException.class, e -> false)
      .waitAndRetryForever((i, r, c) -> Duration.ZERO);

    assertThatExceptionOfType(CompletionException.class).isThrownBy(() ->
      AsyncPolicyUtils.raiseExceptions(policy, 1, i -> new ArithmeticException())
        .join())
      .withCauseInstanceOf(ArithmeticException.class);
  }

  @Test
  public void shouldThrowWhenNoneOfTheSpecifiedExceptionPredicatesAreSatisfied() {
    final var policy = AsyncRetryPolicy.<Void>builder()
      .handle(ArithmeticException.class, e -> false)
      .handle(IllegalArgumentException.class, e -> false)
      .waitAndRetryForever((i, r, c) -> Duration.ZERO);

    assertThatExceptionOfType(CompletionException.class).isThrownBy(() ->
      AsyncPolicyUtils.raiseExceptions(policy, 1, i -> new IllegalArgumentException())
        .join())
      .withCauseInstanceOf(IllegalArgumentException.class);
  }

  @Test
  public void shouldNotThrowWhenSpecifiedExceptionPredicateIsSatisfied() {
    final var policy = AsyncRetryPolicy.<Void>builder()
      .handle(ArithmeticException.class, e -> true)
      .waitAndRetryForever((i, r, c) -> Duration.ofMillis(1));

    AsyncPolicyUtils.raiseExceptions(policy, 1, i -> new ArithmeticException())
      .join();
  }

  @Test
  public void shouldNotThrowWhenOneOfTheSpecifiedExceptionPredicatesAreSatisfied() {
    final var policy = AsyncRetryPolicy.<Void>builder()
      .handle(ArithmeticException.class, e -> true)
      .handle(IllegalArgumentException.class, e -> true)
      .waitAndRetryForever((i, r, c) -> Duration.ofMillis(1));

    AsyncPolicyUtils.raiseExceptions(policy, 1, i -> new IllegalArgumentException())
      .join();
  }

  @Test
  public void shouldCallOnRetryOnEachRetryWithTheCurrentException() {
    final var expectedExceptions = List.of("Exception #1", "Exception #2", "Exception #3");
    final var retryExceptions = new ArrayList<Throwable>(3);
    final var policy = AsyncRetryPolicy.<Void>builder()
      .handle(ArithmeticException.class)
      .waitAndRetryForever((i, r, c) -> Duration.ZERO,
        fromConsumer4Async(outcome -> d -> (i, c) -> outcome.onFailure(fromConsumer(retryExceptions::add))));

    AsyncPolicyUtils.raiseExceptions(policy, 3, i -> new ArithmeticException("Exception #" + i))
      .join();

    assertThat(retryExceptions.stream()
      .map(Throwable::getMessage)
      .collect(Collectors.toList()))
      .containsExactlyElementsOf(expectedExceptions);
  }

  @Test
  public void shouldCallOnRetryOnEachRetryWithTheCurrentRetryCount() {
    final var expectedRetryCounts = List.of(1, 2, 3);
    final var retryCounts = new ArrayList<Integer>(3);
    final var policy = AsyncRetryPolicy.<Void>builder()
      .handle(ArithmeticException.class)
      .waitAndRetryForever((i, r, c) -> Duration.ZERO,
        fromConsumer4Async(dr -> d -> (retryCount, c) -> retryCounts.add(retryCount)));

    AsyncPolicyUtils.raiseExceptions(policy, 3, i -> new ArithmeticException())
      .join();

    assertThat(retryCounts).containsExactlyElementsOf(expectedRetryCounts);
  }

  @Test
  public void shouldNotCallOnRetryWhenNoRetriesArePerformed() {
    final var onRetryCalled = new AtomicBoolean(false);
    final var policy = AsyncRetryPolicy.<Void>builder()
      .handle(ArithmeticException.class)
      .waitAndRetryForever((i, r, c) -> Duration.ofMillis(1),
        fromConsumer4Async(dr -> d -> (i, c) -> onRetryCalled.set(true)));

    assertThatExceptionOfType(CompletionException.class).isThrownBy(() ->
      AsyncPolicyUtils.raiseExceptions(policy, 1, i -> new IllegalArgumentException())
        .join())
      .withCauseInstanceOf(IllegalArgumentException.class);
    assertThat(onRetryCalled.get()).isFalse();
  }

  @Test
  public void shouldCreateNewContextForEachCallToExecute() {
    final var contextValue = new AtomicReference<String>();
    final var policy = AsyncRetryPolicy.<Void>builder()
      .handle(ArithmeticException.class)
      .waitAndRetryForever((i, r, c) -> Duration.ofMillis(1),
        fromConsumer4Async(r -> d -> (i, ctx) -> contextValue.set(ctx.getContextData().get("key").toString())));

    AsyncPolicyUtils.raiseExceptions(policy,
      Context.builder()
        .contextData(new HashMap<>() {{
          put("key", "original_value");
        }}).build(),
      1,
      i -> new ArithmeticException())
      .join();

    assertThat(contextValue.get()).isEqualTo("original_value");

    AsyncPolicyUtils.raiseExceptions(policy,
      Context.builder()
        .contextData(new HashMap<>() {{
          put("key", "new_value");
        }}).build(),
      1,
      i -> new ArithmeticException())
      .join();

    assertThat(contextValue.get()).isEqualTo("new_value");
  }

  @Test
  public void shouldCalculateRetryDurationsFromCurrentRetryAttemptAndDurationProvider() {
    final var expectedRetryWaits = List.of(Duration.ofMillis(2), Duration.ofMillis(4), Duration.ofMillis(8),
      Duration.ofMillis(16), Duration.ofMillis(32));
    final var actualRetryWaits = new ArrayList<Duration>(5);
    final var policy = AsyncRetryPolicy.<Void>builder()
      .handle(ArithmeticException.class)
      .waitAndRetryForever((retryAttempt, dr, c) -> Duration.ofMillis((long) Math.pow(2.0, retryAttempt)),
        fromConsumer4Async(dr -> duration -> (i, c) -> actualRetryWaits.add(duration)));

    AsyncPolicyUtils.raiseExceptions(policy, 5, i -> new ArithmeticException())
      .join();

    assertThat(actualRetryWaits).containsExactlyElementsOf(expectedRetryWaits);
  }

  @Test
  public void shouldBeAbleToCalculateRetryDurationsBasedOnTheHandledFault() {
    final var expectedRetryWaits = Map.of(new ArithmeticException(), Duration.ofMillis(2),
      new NullPointerException(), Duration.ofMillis(4));
    final var actualRetryWaits = new ArrayList<Duration>(2);
    //noinspection SuspiciousMethodCalls
    final var policy = AsyncRetryPolicy.<Void>builder()
      .handle(RuntimeException.class)
      .waitAndRetryForever((Integer i, DelegateResult<Void> outcome, Context c) ->
          outcome.fold(v -> Duration.ZERO, expectedRetryWaits::get),
        fromConsumer4Async(dr -> duration -> (i, c) -> actualRetryWaits.add(duration)));

    final var iterator = expectedRetryWaits.keySet().iterator();
    policy.executeAsync(() -> {
      if (iterator.hasNext()) throw iterator.next();
      return CompletableFuture.completedFuture(null);
    })
      .join();

    assertThat(actualRetryWaits).containsExactlyElementsOf(expectedRetryWaits.values());
  }

  @Test
  public void shouldBeAbleToPassRetryDurationFromExecutionToSleepDurationProviderViaContext() {
    final var expectedRetryDuration = Duration.ofMillis(1);
    final var actualRetryDuration = new AtomicReference<Duration>();
    final var defaultRetryAfter = Duration.ofMillis(30);
    final var policy = AsyncRetryPolicy.<Void>builder()
      .handle(ArithmeticException.class)
      .waitAndRetryForever((Integer i, DelegateResult<Void> dr, Context context) -> {
          if (context.getContextData().containsKey("RetryAfter")) {
            return (Duration) context.getContextData().get("RetryAfter");
          } else {
            return defaultRetryAfter;
          }
        },
        fromConsumer4Async(dr -> duration -> (i, c) -> actualRetryDuration.set(duration)));
    final var failedOnce = new AtomicBoolean(false);

    policy.executeAsync(
      new HashMap<>() {{
        put("RetryAfter", defaultRetryAfter);
      }},
      (Context context) -> {
        context.getContextData().put("RetryAfter", expectedRetryDuration);
        if (!failedOnce.get()) {
          failedOnce.set(true);
          throw new ArithmeticException();
        }
        return CompletableFuture.completedFuture(null);
      })
      .join();

    assertThat(actualRetryDuration.get()).isEqualTo(expectedRetryDuration);
  }

  @Test
  public void shouldWaitAsynchronouslyForAsyncOnRetryDelegate() {
    final var duration = Duration.ofMillis(200);
    final var executeDelegateInvocations = new AtomicInteger(0);
    final var executeDelegateInvocationsWhenOnRetryExits = new AtomicInteger(0);
    final var policy = AsyncRetryPolicy.<Void>builder()
      .handle(ArithmeticException.class)
      .waitAndRetryForever((i, dr, c) -> Duration.ZERO,
        (r, d, i, c) -> {
          final var executor = CompletableFuture.delayedExecutor(duration.toMillis(), TimeUnit.MILLISECONDS);
          return CompletableFuture
            .runAsync(() -> executeDelegateInvocationsWhenOnRetryExits.set(executeDelegateInvocations.get()), executor)
            .thenApplyAsync(v -> Unit.INSTANCE);
        });

    policy.executeAsync(() -> {
      executeDelegateInvocations.incrementAndGet();
      if (executeDelegateInvocations.get() == 1) {
        throw new ArithmeticException();
      }
      return CompletableFuture.completedFuture(null);
    })
      .join();

    assertThat(executeDelegateInvocationsWhenOnRetryExits.get()).isEqualTo(1);
    assertThat(executeDelegateInvocations.get()).isEqualTo(2);
  }

  @Test
  public void shouldExecuteActionWhenNonFaultingAndNotCancelled() {
    final var policy = AsyncRetryPolicy.<Void>builder()
      .handle(ArithmeticException.class)
      .waitAndRetryForever((i, r, c) -> Duration.ZERO);
    final var attemptsInvoked = new AtomicInteger(0);
    final Function0<CompletableFuture<Void>> action = () -> {
      attemptsInvoked.incrementAndGet();
      return CompletableFuture.completedFuture(null);
    };

    AsyncPolicyUtils.raiseExceptions(policy, 0, action, i -> new ArithmeticException())
      .join();

    assertThat(attemptsInvoked.get()).isEqualTo(1);
  }

  @Test
  public void shouldNotExecuteActionWhenCancelledBeforeExecute() {
    final var policy = AsyncRetryPolicy.<Void>builder()
      .handle(ArithmeticException.class)
      .waitAndRetryForever((i, r, c) -> Duration.ZERO);
    final var attemptsInvoked = new AtomicInteger(0);
    final Executor executor = CompletableFuture.delayedExecutor(200, TimeUnit.MILLISECONDS);
    final CompletableFuture<Void> dummyFuture = CompletableFuture.runAsync(() -> {
    }, executor);
    final Function0<CompletableFuture<Void>> action = () ->
      dummyFuture.thenComposeAsync(v -> {
        attemptsInvoked.incrementAndGet();
        return CompletableFuture.completedFuture(null);
      });

    dummyFuture.cancel(true);

    assertThatExceptionOfType(CancellationException.class).isThrownBy(() ->
      AsyncPolicyUtils.raiseExceptions(policy, 1 + 3, action, i -> new ArithmeticException())
        .join());
    assertThat(attemptsInvoked.get()).isEqualTo(0);
  }

  @Test
  public void shouldReportCancellationDuringOtherwiseNonFaultingActionExecutionAndCancelFurtherRetries() {
    final var policy = AsyncRetryPolicy.<Void>builder()
      .handle(ArithmeticException.class)
      .waitAndRetryForever((i, r, c) -> Duration.ZERO);
    final var attemptsInvoked = new AtomicInteger(0);
    final Function0<CompletableFuture<Void>> action = () -> {
      attemptsInvoked.incrementAndGet();
      return CompletableFuture.completedFuture(null);
    };

    assertThatExceptionOfType(CancellationException.class).isThrownBy(() ->
      AsyncPolicyUtils.raiseExceptionsAndOrCancellation(policy, 0, 1, action,
        i -> new ArithmeticException())
        .join());
    assertThat(attemptsInvoked.get()).isEqualTo(1);
  }

  @Test
  public void shouldReportCancellationDuringFaultingInitialActionExecutionAndCancelFurtherRetries() {
    final var policy = AsyncRetryPolicy.<Void>builder()
      .handle(ArithmeticException.class)
      .waitAndRetryForever((i, r, c) -> Duration.ZERO);
    final var attemptsInvoked = new AtomicInteger(0);
    final Function0<CompletableFuture<Void>> action = () -> {
      attemptsInvoked.incrementAndGet();
      return CompletableFuture.completedFuture(null);
    };

    assertThatExceptionOfType(CancellationException.class).isThrownBy(() ->
      AsyncPolicyUtils.raiseExceptionsAndOrCancellation(policy, 1 + 3, 1,
        action, i -> new ArithmeticException())
        .join());
    assertThat(attemptsInvoked.get()).isEqualTo(1);
  }

  @Test
  public void shouldReportCancellationDuringFaultingRetriedActionExecutionAndCancelFurtherRetries() {
    final var policy = AsyncRetryPolicy.<Void>builder()
      .handle(ArithmeticException.class)
      .waitAndRetryForever((i, r, c) -> Duration.ZERO);
    final var attemptsInvoked = new AtomicInteger(0);
    final Function0<CompletableFuture<Void>> action = () -> {
      attemptsInvoked.incrementAndGet();
      return CompletableFuture.completedFuture(null);
    };

    assertThatExceptionOfType(CancellationException.class).isThrownBy(() ->
      AsyncPolicyUtils.raiseExceptionsAndOrCancellation(policy, 1 + 3, 2,
        action, i -> new ArithmeticException())
        .join());
    assertThat(attemptsInvoked.get()).isEqualTo(2);
  }

  @Test
  public void shouldReportCancellationAfterFaultingActionExecutionAndCancelFurtherRetriesIfOnRetryInvokesCancellation() {
    final var policy = AsyncRetryPolicy.<Void>builder()
      .handle(ArithmeticException.class)
      .waitAndRetryForever((i, r, c) -> Duration.ZERO,
        fromConsumer4Async(r -> d -> (i, c) -> {
          throw new CancellationException();
        }));
    final var attemptsInvoked = new AtomicInteger(0);
    final Function0<CompletableFuture<Void>> action = () -> {
      attemptsInvoked.incrementAndGet();
      return CompletableFuture.completedFuture(null);
    };

    assertThatExceptionOfType(CancellationException.class).isThrownBy(() ->
      AsyncPolicyUtils.raiseExceptions(policy, 1 + 3, action, i -> new ArithmeticException())
        .join());
    assertThat(attemptsInvoked.get()).isEqualTo(1);
  }

  @Test
  public void shouldExecuteFunctionReturningValueWhenNotCancelled() {
    final var policy = AsyncRetryPolicy.<Boolean>builder()
      .handle(ArithmeticException.class)
      .waitAndRetryForever((i, r, c) -> Duration.ZERO);
    final var attemptsInvoked = new AtomicInteger(0);
    final Function0<CompletableFuture<Void>> action = () -> {
      attemptsInvoked.incrementAndGet();
      return CompletableFuture.completedFuture(null);
    };

    final var result = AsyncPolicyUtils.raiseExceptions(policy, 0, action, true,
      i -> new ArithmeticException())
      .join();

    assertThat(result).isTrue();
    assertThat(attemptsInvoked.get()).isEqualTo(1);
  }

  @Test
  public void shouldHonourAndReportCancellationDuringFunctionExecution() {
    final var policy = AsyncRetryPolicy.<Boolean>builder()
      .handle(ArithmeticException.class)
      .waitAndRetryForever((i, r, c) -> Duration.ZERO);
    final var attemptsInvoked = new AtomicInteger(0);
    final Function0<CompletableFuture<Void>> action = () -> {
      attemptsInvoked.incrementAndGet();
      return CompletableFuture.completedFuture(null);
    };
    final AtomicReference<Boolean> result = new AtomicReference<>();

    assertThatExceptionOfType(CancellationException.class).isThrownBy(() ->
      result.set(AsyncPolicyUtils.raiseExceptionsAndOrCancellation(policy, 0, 1,
        action, true, i -> new ArithmeticException())
        .join()));
    assertThat(result.get()).isNull();
    assertThat(attemptsInvoked.get()).isEqualTo(1);
  }
}
