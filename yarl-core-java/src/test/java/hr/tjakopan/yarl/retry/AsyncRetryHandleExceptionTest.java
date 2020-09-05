package hr.tjakopan.yarl.retry;

import hr.tjakopan.yarl.Context;
import hr.tjakopan.yarl.test.helpers.AsyncPolicyUtils;
import hr.tjakopan.yarl.test.helpers.TestResult;
import kotlin.Unit;
import kotlin.jvm.functions.Function0;
import org.assertj.core.api.ThrowableAssert;
import org.junit.Test;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static hr.tjakopan.yarl.Functions.fromConsumer;
import static hr.tjakopan.yarl.Functions.fromConsumer3Async;
import static org.assertj.core.api.Assertions.*;

public class AsyncRetryHandleExceptionTest {
  @Test
  public void shouldThrowWhenRetryCountIsLessThanZero() {
    final ThrowableAssert.ThrowingCallable shouldThrow = () -> AsyncRetryPolicy.<TestResult>builder()
      .handle(ArithmeticException.class)
      .retry(-1);

    assertThatThrownBy(shouldThrow)
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessageContaining("Retry count");
  }

  @Test
  public void shouldThrowWhenOnRetryIsNull() {
    final ThrowableAssert.ThrowingCallable shouldThrow = () -> {
      //noinspection ConstantConditions
      AsyncRetryPolicy.<TestResult>builder()
        .handle(ArithmeticException.class)
        .retry(null);
    };

    assertThatThrownBy(shouldThrow)
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessageContaining("onRetry");
  }

  @Test
  public void shouldNotThrowWhenSpecifiedExceptionThrownSameNumberOfTimesAsRetryCount() {
    final var policy = AsyncRetryPolicy.<Void>builder()
      .handle(ArithmeticException.class)
      .retry(3);

    AsyncPolicyUtils.raiseExceptions(policy, 3, i -> new ArithmeticException())
      .join();
  }

  @Test
  public void shouldNotThrowWhenOneOfTheSpecifiedExceptionsThrownSameNumberOfTriesAsRetryCount() {
    final var policy = AsyncRetryPolicy.<Void>builder()
      .handle(ArithmeticException.class)
      .handle(IllegalArgumentException.class)
      .retry(3);

    AsyncPolicyUtils.raiseExceptions(policy, 3, i -> new IllegalArgumentException())
      .join();
  }

  @Test
  public void shouldNotThrowWhenSpecifiedExceptionThrownLessNumberOfTimesThanRetryCount() {
    final var policy = AsyncRetryPolicy.<Void>builder()
      .handle(ArithmeticException.class)
      .retry(3);

    AsyncPolicyUtils.raiseExceptions(policy, 1, i -> new ArithmeticException())
      .join();
  }

  @Test
  public void shouldNotThrowWhenOneOfTheSpecifiedExceptionsThrownLessNumberOfTimesThanRetryCount() {
    final var policy = AsyncRetryPolicy.<Void>builder()
      .handle(ArithmeticException.class)
      .handle(IllegalArgumentException.class)
      .retry(3);

    AsyncPolicyUtils.raiseExceptions(policy, 1, i -> new IllegalArgumentException())
      .join();
  }

  @Test
  public void shouldThrowWhenSpecifiedExceptionThrownMoreTimesThanRetryCount() {
    final var policy = AsyncRetryPolicy.<Void>builder()
      .handle(ArithmeticException.class)
      .retry(3);

    assertThatThrownBy(() ->
      AsyncPolicyUtils.raiseExceptions(policy, 3 + 1, i -> new ArithmeticException())
        .join())
      .isInstanceOf(CompletionException.class)
      .hasCauseInstanceOf(ArithmeticException.class);
  }

  @Test
  public void shouldThrowWhenOneOfTheSpecifiedExceptionsThrownMoreTimesThanRetryCount() {
    final var policy = AsyncRetryPolicy.<Void>builder()
      .handle(ArithmeticException.class)
      .handle(IllegalArgumentException.class)
      .retry(3);

    assertThatThrownBy(() ->
      AsyncPolicyUtils.raiseExceptions(policy, 3 + 1, i -> new IllegalArgumentException())
        .join())
      .isInstanceOf(CompletionException.class)
      .hasCauseInstanceOf(IllegalArgumentException.class);
  }

  @Test
  public void shouldThrowWhenExceptionThrownIsNotTheSpecifiedExceptionType() {
    final var policy = AsyncRetryPolicy.<Void>builder()
      .handle(ArithmeticException.class)
      .retry(3);

    assertThatThrownBy(() ->
      AsyncPolicyUtils.raiseExceptions(policy, 1, i -> new NullPointerException())
        .join())
      .isInstanceOf(CompletionException.class)
      .hasCauseInstanceOf(NullPointerException.class);
  }

  @Test
  public void shouldThrowWhenExceptionThrownIsNotOneOfTheSpecifiedExceptionTypes() {
    final var policy = AsyncRetryPolicy.<Void>builder()
      .handle(ArithmeticException.class)
      .handle(IllegalArgumentException.class)
      .retry(3);

    assertThatThrownBy(() ->
      AsyncPolicyUtils.raiseExceptions(policy, 1, i -> new NullPointerException())
        .join())
      .isInstanceOf(CompletionException.class)
      .hasCauseInstanceOf(NullPointerException.class);
  }

  @Test
  public void shouldThrowWhenSpecifiedExceptionPredicateIsNotSatisfied() {
    final var policy = AsyncRetryPolicy.<Void>builder()
      .handle(ArithmeticException.class, e -> false)
      .retry();

    assertThatThrownBy(() ->
      AsyncPolicyUtils.raiseExceptions(policy, 1, i -> new ArithmeticException())
        .join())
      .isInstanceOf(CompletionException.class)
      .hasCauseInstanceOf(ArithmeticException.class);
  }

  @Test
  public void shouldThrowWhenNoneOfTheSpecifiedExceptionPredicatesAreSatisfied() {
    final var policy = AsyncRetryPolicy.<Void>builder()
      .handle(ArithmeticException.class, e -> false)
      .handle(IllegalArgumentException.class, e -> false)
      .retry();

    assertThatThrownBy(() ->
      AsyncPolicyUtils.raiseExceptions(policy, 1, i -> new IllegalArgumentException())
        .join())
      .isInstanceOf(CompletionException.class)
      .hasCauseInstanceOf(IllegalArgumentException.class);
  }

  @Test
  public void shouldNotThrowWhenSpecifiedExceptionPredicateIsSatisfied() {
    final var policy = AsyncRetryPolicy.<Void>builder()
      .handle(ArithmeticException.class, e -> true)
      .retry();

    AsyncPolicyUtils.raiseExceptions(policy, 1, i -> new ArithmeticException())
      .join();
  }

  @Test
  public void shouldNotThrowWhenOneOfTheSpecifiedExceptionPredicatesIsSatisfied() {
    final var policy = AsyncRetryPolicy.<Void>builder()
      .handle(ArithmeticException.class, e -> true)
      .handle(IllegalArgumentException.class, e -> true)
      .retry();

    AsyncPolicyUtils.raiseExceptions(policy, 1, i -> new IllegalArgumentException())
      .join();
  }

  @Test
  public void shouldCallOnRetryOnEachRetryWithTheCurrentRetryCount() {
    final var expectedRetryCounts = List.of(1, 2, 3);
    final var retryCounts = new ArrayList<Integer>(3);
    final var policy = AsyncRetryPolicy.<Void>builder()
      .handle(ArithmeticException.class)
      .retry(3, fromConsumer3Async(r -> (retryCount, ctx) -> retryCounts.add(retryCount)));

    AsyncPolicyUtils.raiseExceptions(policy, 3, i -> new ArithmeticException())
      .join();

    assertThat(retryCounts).containsExactlyElementsOf(expectedRetryCounts);
  }

  @Test
  public void shouldCallOnRetryOnEachRetryWithTheCurrentException() {
    final var expectedExceptions = List.of("Exception #1", "Exception #2", "Exception #3");
    final var retryExceptions = new ArrayList<Throwable>();
    final var policy = AsyncRetryPolicy.<Void>builder()
      .handle(ArithmeticException.class)
      .retry(3, fromConsumer3Async(outcome -> (i, c) ->
        outcome.onFailure(fromConsumer(retryExceptions::add))));

    AsyncPolicyUtils.raiseExceptions(policy, 3,
      i -> new ArithmeticException("Exception #" + i))
      .join();

    assertThat(retryExceptions.stream()
      .map(Throwable::getMessage))
      .containsExactlyElementsOf(expectedExceptions);
  }

  @Test
  public void shouldCallOnRetryWithAHandledCauseException() {
    final var exceptionPassedToOnRetry = new AtomicReference<Throwable>();
    final var policy = AsyncRetryPolicy.<Void>builder()
      .handleCause(ArithmeticException.class)
      .retry(3, fromConsumer3Async(outcome -> (i, c) ->
        outcome.onFailure(fromConsumer(exceptionPassedToOnRetry::set))));
    final var causeException = new ArithmeticException();
    final var exception = new RuntimeException(causeException);

    AsyncPolicyUtils.raiseExceptions(policy, 1, i -> exception)
      .join();

    assertThat(exceptionPassedToOnRetry.get()).isSameAs(causeException);
  }

  @Test
  public void shouldNotCallOnRetryWhenNoRetriesArePerformed() {
    final var retryCalled = new AtomicBoolean(false);
    final var policy = AsyncRetryPolicy.<Void>builder()
      .handle(ArithmeticException.class)
      .retry(fromConsumer3Async(r -> (i, c) -> retryCalled.set(true)));

    assertThatThrownBy(() -> AsyncPolicyUtils.raiseExceptions(policy, 1,
      i -> new IllegalArgumentException())
      .join())
      .isInstanceOf(CompletionException.class)
      .hasCauseInstanceOf(IllegalArgumentException.class);
    assertThat(retryCalled.get()).isFalse();
  }

  @Test
  public void shouldCreateNewStateForEachCallToPolicy() {
    final var policy = AsyncRetryPolicy.<Void>builder()
      .handle(ArithmeticException.class)
      .retry();

    AsyncPolicyUtils.raiseExceptions(policy, 1, i -> new ArithmeticException());
    AsyncPolicyUtils.raiseExceptions(policy, 1, i -> new ArithmeticException());
  }

  @Test
  public void shouldCallOnRetryWithThePassedContext() {
    final var context = new AtomicReference<Context>();
    final var policy = AsyncRetryPolicy.<Void>builder()
      .handle(ArithmeticException.class)
      .retry(fromConsumer3Async(r -> (i, ctx) -> context.set(ctx)));

    AsyncPolicyUtils.raiseExceptions(
      policy,
      Context.builder()
        .contextData(new HashMap<>() {{
          put("key1", "value1");
          put("key2", "value2");
        }})
        .build(),
      1,
      i -> new ArithmeticException()
    )
      .join();

    assertThat(context.get()).isNotNull();
    assertThat(context.get().getContextData()).containsKeys("key1", "key2")
      .containsValues("value1", "value2");
  }

  @Test
  public void shouldCallOnRetryWithThePassedContextOnExecuteAndCapture() {
    final var context = new AtomicReference<Context>();
    final var policy = AsyncRetryPolicy.<Void>builder()
      .handle(ArithmeticException.class)
      .retry(fromConsumer3Async(r -> (i, ctx) -> context.set(ctx)));

    AsyncPolicyUtils.raiseExceptionsOnExecuteAndCapture(
      policy,
      Context.builder()
        .contextData(new HashMap<>() {{
          put("key1", "value1");
          put("key2", "value2");
        }})
        .build(),
      1,
      i -> new ArithmeticException()
    )
      .join();

    assertThat(context.get()).isNotNull();
    assertThat(context.get().getContextData()).containsKeys("key1", "key2")
      .containsValues("value1", "value2");
  }

  @Test
  public void contextShouldBeEmptyIfExecuteNotCalledWithContext() {
    final var capturedContext = new AtomicReference<Context>();
    final var policy = AsyncRetryPolicy.<Void>builder()
      .handle(ArithmeticException.class)
      .retry(fromConsumer3Async(r -> (i, ctx) -> capturedContext.set(ctx)));

    AsyncPolicyUtils.raiseExceptions(policy, 1, i -> new ArithmeticException())
      .join();

    assertThat(capturedContext.get()).isNotNull();
    assertThat(capturedContext.get().getPolicyWrapKey()).isNull();
    assertThat(capturedContext.get().getPolicyKey()).isNotNull();
    assertThat(capturedContext.get().getOperationKey()).isNull();
    assertThat(capturedContext.get().getContextData()).isEmpty();
  }

  @Test
  public void shouldCreateNewContextForEachCallToExecute() {
    final var contextValue = new AtomicReference<String>();
    final var policy = AsyncRetryPolicy.<Void>builder()
      .handle(ArithmeticException.class)
      .retry(fromConsumer3Async(r -> (i, ctx) -> contextValue.set(ctx.getContextData().get("key").toString())));

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
  public void shouldCreateNewContextForEachCallToExecuteAndCapture() {
    final var contextValue = new AtomicReference<String>();
    final var policy = AsyncRetryPolicy.<Void>builder()
      .handle(ArithmeticException.class)
      .retry(fromConsumer3Async(r -> (i, ctx) -> contextValue.set(ctx.getContextData().get("key").toString())));

    AsyncPolicyUtils.raiseExceptionsOnExecuteAndCapture(policy,
      Context.builder()
        .contextData(new HashMap<>() {{
          put("key", "original_value");
        }}).build(),
      1,
      i -> new ArithmeticException())
      .join();

    assertThat(contextValue.get()).isEqualTo("original_value");

    AsyncPolicyUtils.raiseExceptionsOnExecuteAndCapture(policy,
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
  public void shouldNotCallOnRetryWhenRetryCountIsZero() {
    final var retryInvoked = new AtomicBoolean(false);
    final var policy = AsyncRetryPolicy.<Void>builder()
      .handle(ArithmeticException.class)
      .retry(0, fromConsumer3Async(r -> (i, c) -> retryInvoked.set(true)));

    assertThatThrownBy(() ->
      AsyncPolicyUtils.raiseExceptions(policy, 1, i -> new ArithmeticException())
        .join())
      .isInstanceOf(CompletionException.class)
      .hasCauseInstanceOf(ArithmeticException.class);
    assertThat(retryInvoked.get()).isFalse();
  }

  @Test
  public void shouldWaitAsynchronouslyForAsyncOnRetryDelegate() {
    final var duration = Duration.ofMillis(200);
    final var executeDelegateInvocations = new AtomicInteger(0);
    final var executeDelegateInvocationsWhenOnRetryExits = new AtomicInteger(0);
    final var policy = AsyncRetryPolicy.<Void>builder()
      .handle(ArithmeticException.class)
      .retry((r, i, c) -> {
        final var executor = CompletableFuture.delayedExecutor(duration.toMillis(), TimeUnit.MILLISECONDS);
        return CompletableFuture
          .runAsync(() -> executeDelegateInvocationsWhenOnRetryExits.set(executeDelegateInvocations.get()), executor)
          .thenApplyAsync(v -> Unit.INSTANCE);
      });

    assertThatThrownBy(() ->
      policy.executeAsync(() -> {
        executeDelegateInvocations.incrementAndGet();
        throw new ArithmeticException();
      })
        .join())
      .isInstanceOf(CompletionException.class)
      .hasCauseInstanceOf(ArithmeticException.class);
    assertThat(executeDelegateInvocationsWhenOnRetryExits.get()).isEqualTo(1);
    assertThat(executeDelegateInvocations.get()).isEqualTo(2);
  }

  @Test
  public void shouldExecuteActionWhenNonFaultingAndNotCancelled() {
    final var policy = AsyncRetryPolicy.<Void>builder()
      .handle(ArithmeticException.class)
      .retry(3);
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
  public void shouldExecuteAllTriesWhenFaultingAndNotCancelled() {
    final var policy = AsyncRetryPolicy.<Void>builder()
      .handle(ArithmeticException.class)
      .retry(3);
    final var attemptsInvoked = new AtomicInteger(0);
    final Function0<CompletableFuture<Void>> action = () -> {
      attemptsInvoked.incrementAndGet();
      return CompletableFuture.completedFuture(null);
    };

    assertThatThrownBy(() ->
      AsyncPolicyUtils.raiseExceptions(policy, 1 + 3, action, i -> new ArithmeticException())
        .join())
      .isInstanceOf(CompletionException.class)
      .hasCauseInstanceOf(ArithmeticException.class);
    assertThat(attemptsInvoked.get()).isEqualTo(1 + 3);
  }

  @Test
  public void shouldNotExecuteActionWhenCancelledBeforeExecute() {
    final var policy = AsyncRetryPolicy.<Void>builder()
      .handle(ArithmeticException.class)
      .retry(3);
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
      .retry(3);
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
      .retry(3);
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
      .retry(3);
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
  public void shouldReportCancellationDuringFaultingLastRetryExecution() {
    final var policy = AsyncRetryPolicy.<Void>builder()
      .handle(ArithmeticException.class)
      .retry(3);
    final var attemptsInvoked = new AtomicInteger(0);
    final Function0<CompletableFuture<Void>> action = () -> {
      attemptsInvoked.incrementAndGet();
      return CompletableFuture.completedFuture(null);
    };

    assertThatExceptionOfType(CancellationException.class).isThrownBy(() ->
      AsyncPolicyUtils.raiseExceptionsAndOrCancellation(policy, 1 + 3, 1 + 3,
        action, i -> new ArithmeticException())
        .join());
    assertThat(attemptsInvoked.get()).isEqualTo(1 + 3);
  }

  @Test
  public void shouldReportCancellationAfterFaultingActionExecutionAndCancelFurtherRetriesIfOnRetryInvokesCancellation() {
    final var policy = AsyncRetryPolicy.<Void>builder()
      .handle(ArithmeticException.class)
      .retry(3, fromConsumer3Async(r -> (i, c) -> {
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
      .retry(3);
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
      .retry(3);
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
