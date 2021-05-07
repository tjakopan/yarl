package hr.tjakopan.yarl.retry;

import hr.tjakopan.yarl.Context;
import hr.tjakopan.yarl.test.helpers.AsyncPolicyUtils;
import kotlin.Unit;
import kotlin.jvm.functions.Function0;
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

public class AsyncRetryForeverTest {
  @Test
  public void shouldNotThrowRegardlessOfHowManyTimesTheSpecifiedExceptionIsRaised() {
    final var policy = AsyncRetryPolicy.<Void>builder()
      .handle(ArithmeticException.class)
      .retryForever();

    AsyncPolicyUtils.raiseExceptions(policy, 3, i -> new ArithmeticException())
      .join();
  }

  @Test
  public void shouldNotThrowRegardlessOfHowManyTimesOneOfTheSpecifiedExceptionIsRaised() {
    final var policy = AsyncRetryPolicy.<Void>builder()
      .handle(ArithmeticException.class)
      .handle(IllegalArgumentException.class)
      .retryForever();

    AsyncPolicyUtils.raiseExceptions(policy, 3, i -> new IllegalArgumentException())
      .join();
  }

  @Test
  public void shouldThrowWhenExceptionThrownIsNotTheSpecifiedExceptionType() {
    final var policy = AsyncRetryPolicy.<Void>builder()
      .handle(ArithmeticException.class)
      .retryForever();

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
      .retryForever();

    assertThatExceptionOfType(CompletionException.class).isThrownBy(() ->
      AsyncPolicyUtils.raiseExceptions(policy, 1, i -> new NullPointerException())
        .join())
      .withCauseInstanceOf(NullPointerException.class);
  }

  @Test
  public void shouldThrowWhenSpecifiedExceptionPredicateIsNotSatisfied() {
    final var policy = AsyncRetryPolicy.<Void>builder()
      .handle(ArithmeticException.class, e -> false)
      .retryForever();

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
      .retryForever();

    assertThatExceptionOfType(CompletionException.class).isThrownBy(() ->
      AsyncPolicyUtils.raiseExceptions(policy, 1, i -> new IllegalArgumentException())
        .join())
      .withCauseInstanceOf(IllegalArgumentException.class);
  }

  @Test
  public void shouldNotThrowWhenSpecifiedExceptionPredicateIsSatisfied() {
    final var policy = AsyncRetryPolicy.<Void>builder()
      .handle(ArithmeticException.class, e -> true)
      .retryForever();

    AsyncPolicyUtils.raiseExceptions(policy, 1, i -> new ArithmeticException())
      .join();
  }

  @Test
  public void shouldNotThrowWhenOneOfTheSpecifiedExceptionPredicatesAreSatisfied() {
    final var policy = AsyncRetryPolicy.<Void>builder()
      .handle(ArithmeticException.class, e -> true)
      .handle(IllegalArgumentException.class, e -> true)
      .retryForever();

    AsyncPolicyUtils.raiseExceptions(policy, 1, i -> new IllegalArgumentException())
      .join();
  }

  @Test
  public void shouldCallOnRetryOnEachRetryWithTheCurrentException() {
    final var expectedExceptions = List.of("Exception #1", "Exception #2", "Exception #3");
    final var retryExceptions = new ArrayList<Throwable>(3);
    final var policy = AsyncRetryPolicy.<Void>builder()
      .handle(ArithmeticException.class)
      .retryForever(fromConsumer3Async(outcome -> (i, c) -> outcome.onFailure(fromConsumer(retryExceptions::add))));

    AsyncPolicyUtils.raiseExceptions(policy, 3, i -> new ArithmeticException("Exception #" + i))
      .join();

    assertThat(retryExceptions.stream()
      .map(Throwable::getMessage))
      .containsExactlyElementsOf(expectedExceptions);
  }

  @Test
  public void shouldCallOnRetryOnEachRetryWithThePassedContext() {
    final var context = new AtomicReference<Context>();
    final var policy = AsyncRetryPolicy.<Void>builder()
      .handle(ArithmeticException.class)
      .retryForever(fromConsumer3Async(d -> (i, ctx) -> context.set(ctx)));

    AsyncPolicyUtils.raiseExceptions(
      policy,
      Context.of(new HashMap<>() {{
        put("key1", "value1");
        put("key2", "value2");
      }}),
      1,
      i -> new ArithmeticException())
      .join();

    assertThat(context.get()).isNotNull();
    assertThat(context.get()).containsKeys("key1", "key2")
      .containsValues("value1", "value2");
  }

  @Test
  public void shouldCallOnRetryOnEachRetryWithTheCurrentRetryCount() {
    final var expectedRetryCounts = List.of(1, 2, 3);
    final var retryCounts = new ArrayList<Integer>(3);
    final var policy = AsyncRetryPolicy.<Void>builder()
      .handle(ArithmeticException.class)
      .retryForever(fromConsumer3Async(d -> (retryCount, c) -> retryCounts.add(retryCount)));

    AsyncPolicyUtils.raiseExceptions(policy, 3, i -> new ArithmeticException())
      .join();

    assertThat(retryCounts).containsExactlyElementsOf(expectedRetryCounts);
  }

  @Test
  public void contextShouldBeEmptyIfExecuteNotCalledWithAnyData() {
    final var capturedContext = new AtomicReference<Context>();
    final var policy = AsyncRetryPolicy.<Void>builder()
      .handle(ArithmeticException.class)
      .retryForever(fromConsumer3Async(r -> (i, ctx) -> capturedContext.set(ctx)));

    AsyncPolicyUtils.raiseExceptions(policy, 1, i -> new ArithmeticException())
      .join();

    assertThat(capturedContext.get()).isEmpty();
  }

  @Test
  public void shouldCreateNewContextForEachCallToExecute() {
    final var contextValue = new AtomicReference<String>();
    //noinspection ConstantConditions
    final var policy = AsyncRetryPolicy.<Void>builder()
      .handle(ArithmeticException.class)
      .retryForever(fromConsumer3Async(r -> (i, ctx) -> contextValue.set(ctx.get("key").toString())));

    AsyncPolicyUtils.raiseExceptions(policy,
      Context.of(new HashMap<>() {{
        put("key", "original_value");
      }}),
      1,
      i -> new ArithmeticException())
      .join();

    assertThat(contextValue.get()).isEqualTo("original_value");

    AsyncPolicyUtils.raiseExceptions(policy,
      Context.of(new HashMap<>() {{
        put("key", "new_value");
      }}),
      1,
      i -> new ArithmeticException())
      .join();

    assertThat(contextValue.get()).isEqualTo("new_value");
  }

  @Test
  public void shouldNotCallOnRetryWhenNoRetriesArePerformed() {
    final var retryCalled = new AtomicBoolean(false);
    final var policy = AsyncRetryPolicy.<Void>builder()
      .handle(ArithmeticException.class)
      .retryForever(fromConsumer3Async(r -> (i, c) -> retryCalled.set(true)));

    assertThatThrownBy(() -> AsyncPolicyUtils.raiseExceptions(policy, 1,
      i -> new IllegalArgumentException())
      .join())
      .isInstanceOf(CompletionException.class)
      .hasCauseInstanceOf(IllegalArgumentException.class);
    assertThat(retryCalled.get()).isFalse();
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
      .retryForever();
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
      .retryForever();
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
      .retryForever();
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
      .retryForever();
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
      .retryForever();
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
      .retryForever(fromConsumer3Async(r -> (i, c) -> {
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
      .retryForever();
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
      .retryForever();
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
