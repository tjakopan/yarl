package hr.tjakopan.yarl.retry;

import hr.tjakopan.yarl.Context;
import hr.tjakopan.yarl.PolicyResult;
import hr.tjakopan.yarl.test.helpers.AsyncPolicyUtils;
import hr.tjakopan.yarl.test.helpers.TestResult;
import hr.tjakopan.yarl.test.helpers.TestResultClass;
import kotlin.Unit;
import kotlin.jvm.functions.Function1;
import org.assertj.core.api.ThrowableAssert;
import org.junit.Test;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import static hr.tjakopan.yarl.Functions.fromConsumer;
import static hr.tjakopan.yarl.Functions.fromConsumer3Async;
import static org.assertj.core.api.Assertions.*;

public class AsyncRetryPolicyHandleResultTest {
  @Test
  public void shouldThrowWhenRetryCountIsLessThanZero() {
    final ThrowableAssert.ThrowingCallable shouldThrow = () -> {
      AsyncRetryPolicy.<TestResult>builder()
        .handleResult(TestResult.FAULT)
        .retry(-1);
    };

    assertThatThrownBy(shouldThrow)
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessageContaining("Retry count");
  }

  @Test
  public void shouldThrowWhenOnRetryIsNull() {
    final ThrowableAssert.ThrowingCallable shouldThrow = () -> {
      //noinspection ConstantConditions
      AsyncRetryPolicy.<TestResult>builder()
        .handleResult(TestResult.FAULT)
        .retry(null);
    };

    assertThatThrownBy(shouldThrow)
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessageContaining("onRetry");
  }

  @Test
  public void shouldNotReturnHandledResultWhenHandledResultRaisedSameNumberOfTimesAsRetryCount() {
    final var policy = AsyncRetryPolicy.<TestResult>builder()
      .handleResult(TestResult.FAULT)
      .retry(3);

    final var result = AsyncPolicyUtils.raiseResults(policy, TestResult.FAULT, TestResult.FAULT, TestResult.FAULT,
      TestResult.GOOD)
      .join();

    assertThat(result).isEqualTo(TestResult.GOOD);
  }

  @Test
  public void shouldNotReturnHandledResultWhenOneOfTheHandledResultsRaisedSameNumberOfTimesAsRetryCount() {
    final var policy = AsyncRetryPolicy.<TestResult>builder()
      .handleResult(TestResult.FAULT)
      .handleResult(TestResult.FAULT_AGAIN)
      .retry(3);

    final var result = AsyncPolicyUtils.raiseResults(policy, TestResult.FAULT, TestResult.FAULT_AGAIN, TestResult.FAULT,
      TestResult.GOOD)
      .join();

    assertThat(result).isEqualTo(TestResult.GOOD);
  }

  @Test
  public void shouldNotReturnHandledResultWhenHandledResultRaisedLessNumberOfTimesThanRetryCount() {
    final var policy = AsyncRetryPolicy.<TestResult>builder()
      .handleResult(TestResult.FAULT)
      .retry(3);

    final var result = AsyncPolicyUtils.raiseResults(policy, TestResult.FAULT, TestResult.GOOD)
      .join();

    assertThat(result).isEqualTo(TestResult.GOOD);
  }

  @Test
  public void shouldNotReturnHandledResultWhenAllOfTheHandledResultsRaisedLessNumberOfTimesThanRetryCount() {
    final var policy = AsyncRetryPolicy.<TestResult>builder()
      .handleResult(TestResult.FAULT)
      .handleResult(TestResult.FAULT_AGAIN)
      .retry(3);

    final var result = AsyncPolicyUtils.raiseResults(policy, TestResult.FAULT, TestResult.FAULT_AGAIN, TestResult.GOOD)
      .join();

    assertThat(result).isEqualTo(TestResult.GOOD);
  }

  @Test
  public void shouldReturnHandledResultWhenHandledResultRaisedMoreTimesThanRetryCount() {
    final var policy = AsyncRetryPolicy.<TestResult>builder()
      .handleResult(TestResult.FAULT)
      .retry(3);

    final var result = AsyncPolicyUtils.raiseResults(policy, TestResult.FAULT, TestResult.FAULT, TestResult.FAULT,
      TestResult.FAULT, TestResult.GOOD)
      .join();

    assertThat(result).isEqualTo(TestResult.FAULT);
  }

  @Test
  public void shouldReturnHandledResultWhenOneOfTheHandledResultsIsRaisedMoreTimesThanRetryCount() {
    final var policy = AsyncRetryPolicy.<TestResult>builder()
      .handleResult(TestResult.FAULT)
      .handleResult(TestResult.FAULT_AGAIN)
      .retry(3);

    final var result = AsyncPolicyUtils.raiseResults(policy, TestResult.FAULT_AGAIN, TestResult.FAULT_AGAIN,
      TestResult.FAULT_AGAIN, TestResult.FAULT_AGAIN, TestResult.GOOD)
      .join();

    assertThat(result).isEqualTo(TestResult.FAULT_AGAIN);
  }

  @Test
  public void shouldReturnResultWhenResultIsNotTheSpecifiedHandledResult() {
    final var policy = AsyncRetryPolicy.<TestResult>builder()
      .handleResult(TestResult.FAULT)
      .retry();

    final var result = AsyncPolicyUtils.raiseResults(policy, TestResult.FAULT_AGAIN, TestResult.GOOD)
      .join();

    assertThat(result).isEqualTo(TestResult.FAULT_AGAIN);
  }

  @Test
  public void shouldReturnResultWhenResultIsNotOneOfTheSpecifiedHandledResults() {
    final var policy = AsyncRetryPolicy.<TestResult>builder()
      .handleResult(TestResult.FAULT)
      .handleResult(TestResult.FAULT_AGAIN)
      .retry();

    final var result = AsyncPolicyUtils.raiseResults(policy, TestResult.FAULT_YET_AGAIN, TestResult.GOOD)
      .join();

    assertThat(result).isEqualTo(TestResult.FAULT_YET_AGAIN);
  }

  @Test
  public void shouldReturnResultWhenSpecifiedResultPredicateIsNotSatisfied() {
    final var policy = AsyncRetryPolicy.<TestResultClass>builder()
      .handleResult(r -> r.getResultCode() == TestResult.FAULT)
      .retry();

    final var result = AsyncPolicyUtils.raiseResults(policy, new TestResultClass(TestResult.FAULT_AGAIN),
      new TestResultClass(TestResult.GOOD))
      .join();

    assertThat(result.getResultCode()).isEqualTo(TestResult.FAULT_AGAIN);
  }

  @Test
  public void shouldReturnResultWhenNoneOfTheSpecifiedResultPredicatesAreSatisfied() {
    final var policy = AsyncRetryPolicy.<TestResultClass>builder()
      .handleResult(r -> r.getResultCode() == TestResult.FAULT)
      .handleResult(r -> r.getResultCode() == TestResult.FAULT_AGAIN)
      .retry();

    final var result = AsyncPolicyUtils.raiseResults(policy, new TestResultClass(TestResult.FAULT_YET_AGAIN),
      new TestResultClass(TestResult.GOOD))
      .join();

    assertThat(result.getResultCode()).isEqualTo(TestResult.FAULT_YET_AGAIN);
  }

  @Test
  public void shouldNotReturnHandledResultWhenSpecifiedResultPredicateIsSatisfied() {
    final var policy = AsyncRetryPolicy.<TestResultClass>builder()
      .handleResult(r -> r.getResultCode() == TestResult.FAULT)
      .retry();

    final var result = AsyncPolicyUtils.raiseResults(policy, new TestResultClass(TestResult.FAULT),
      new TestResultClass(TestResult.GOOD))
      .join();

    assertThat(result.getResultCode()).isEqualTo(TestResult.GOOD);
  }

  @Test
  public void shouldNotReturnHandledResultWhenOneOfTheSpecifiedResultPredicatesIsSatisfied() {
    final var policy = AsyncRetryPolicy.<TestResultClass>builder()
      .handleResult(r -> r.getResultCode() == TestResult.FAULT)
      .handleResult(r -> r.getResultCode() == TestResult.FAULT_AGAIN)
      .retry();

    final var result = AsyncPolicyUtils.raiseResults(policy, new TestResultClass(TestResult.FAULT_AGAIN),
      new TestResultClass(TestResult.GOOD))
      .join();

    assertThat(result.getResultCode()).isEqualTo(TestResult.GOOD);
  }

  @Test
  public void shouldCallOnRetryOnEachRetryWithTheCurrentRetryCount() {
    final var expectedRetryCounts = List.of(1, 2, 3);
    final var retryCounts = new ArrayList<Integer>(3);
    final var policy = AsyncRetryPolicy.<TestResult>builder()
      .handleResult(TestResult.FAULT)
      .retry(3, fromConsumer3Async(r -> (retryCount, ctx) -> retryCounts.add(retryCount)));

    final var result = AsyncPolicyUtils.raiseResults(policy, TestResult.FAULT, TestResult.FAULT, TestResult.FAULT,
      TestResult.GOOD)
      .join();

    assertThat(result).isEqualTo(TestResult.GOOD);
    assertThat(retryCounts).containsExactlyElementsOf(expectedRetryCounts);
  }

  @Test
  public void shouldCallOnRetryOnEachRetryWithTheCurrentHandledResult() {
    final var expectedFaults = List.of("Fault #1", "Fault #2", "Fault #3");
    final var retryFaults = new ArrayList<String>();
    final var policy = AsyncRetryPolicy.<TestResultClass>builder()
      .handleResult(r -> r.getResultCode() == TestResult.FAULT)
      .retry(3, fromConsumer3Async(outcome -> (i, c) ->
        outcome.onSuccess(fromConsumer(r -> retryFaults.add(r.getSomeString())))));
    final var resultsToRaise = expectedFaults.stream()
      .map(s -> new TestResultClass(TestResult.FAULT, s))
      .collect(Collectors.toList());
    resultsToRaise.add(new TestResultClass(TestResult.FAULT));

    final var result = AsyncPolicyUtils.raiseResults(policy, resultsToRaise.toArray(TestResultClass[]::new))
      .join();

    assertThat(result.getResultCode()).isEqualTo(TestResult.FAULT);
    assertThat(retryFaults).containsExactlyElementsOf(expectedFaults);
  }

  @Test
  public void shouldNotCallOnRetryWhenNoRetriesArePerformed() {
    final var retryCalled = new AtomicBoolean(false);
    final var policy = AsyncRetryPolicy.<TestResult>builder()
      .handleResult(TestResult.FAULT)
      .retry(fromConsumer3Async(r -> (i, c) -> retryCalled.set(true)));

    final var result = AsyncPolicyUtils.raiseResults(policy, TestResult.GOOD)
      .join();

    assertThat(result).isEqualTo(TestResult.GOOD);
    assertThat(retryCalled.get()).isFalse();
  }

  @Test
  public void shouldCallOnRetryWithThePassedContext() {
    final var context = new AtomicReference<Context>();
    final var policy = AsyncRetryPolicy.<TestResult>builder()
      .handleResult(TestResult.FAULT)
      .retry(fromConsumer3Async(r -> (i, ctx) -> context.set(ctx)));

    final var result = AsyncPolicyUtils.raiseResults(
      policy,
      Context.builder()
        .contextData(new HashMap<>() {{
          put("key1", "value1");
          put("key2", "value2");
        }})
        .build(),
      TestResult.FAULT,
      TestResult.GOOD
    )
      .join();

    assertThat(result).isEqualTo(TestResult.GOOD);
    assertThat(context.get()).isNotNull();
    assertThat(context.get().getContextData()).containsKeys("key1", "key2")
      .containsValues("value1", "value2");
  }

  @Test
  public void shouldCallOnRetryWithThePassedContextWhenExecuteAndCapture() {
    final var context = new AtomicReference<Context>();
    final var policy = AsyncRetryPolicy.<TestResult>builder()
      .handleResult(TestResult.FAULT)
      .retry(fromConsumer3Async(r -> (i, ctx) -> context.set(ctx)));

    final var result = AsyncPolicyUtils.raiseResultsOnExecuteAndCapture(
      policy,
      Context.builder()
        .contextData(new HashMap<>() {{
          put("key1", "value1");
          put("key2", "value2");
        }})
        .build(),
      TestResult.FAULT,
      TestResult.GOOD
    )
      .join();

    assertThat(result.isSuccess()).isTrue();
    //noinspection rawtypes
    assertThat(((PolicyResult.Success) result).getResult()).isEqualTo(TestResult.GOOD);
    assertThat(context.get()).isNotNull();
    assertThat(context.get().getContextData()).containsKeys("key1", "key2")
      .containsValues("value1", "value2");
  }

  @Test
  public void contextShouldBeEmptyIfExecuteNotCalledWithContext() {
    final var capturedContext = new AtomicReference<Context>();
    final var policy = AsyncRetryPolicy.<TestResult>builder()
      .handleResult(TestResult.FAULT)
      .retry(fromConsumer3Async(r -> (i, ctx) -> capturedContext.set(ctx)));

    AsyncPolicyUtils.raiseResults(policy, TestResult.FAULT, TestResult.GOOD)
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
    final var policy = AsyncRetryPolicy.<TestResult>builder()
      .handleResult(TestResult.FAULT)
      .retry(fromConsumer3Async(r -> (i, ctx) -> contextValue.set(ctx.getContextData().get("key").toString())));

    AsyncPolicyUtils.raiseResults(policy,
      Context.builder()
        .contextData(new HashMap<>() {{
          put("key", "original_value");
        }}).build(),
      TestResult.FAULT,
      TestResult.GOOD)
      .join();

    assertThat(contextValue.get()).isEqualTo("original_value");

    AsyncPolicyUtils.raiseResults(policy,
      Context.builder()
        .contextData(new HashMap<>() {{
          put("key", "new_value");
        }}).build(),
      TestResult.FAULT,
      TestResult.GOOD)
      .join();

    assertThat(contextValue.get()).isEqualTo("new_value");
  }

  @Test
  public void shouldCreateNewContextForEachCallToExecuteAndCapture() {
    final var contextValue = new AtomicReference<String>();
    final var policy = AsyncRetryPolicy.<TestResult>builder()
      .handleResult(TestResult.FAULT)
      .retry(fromConsumer3Async(r -> (i, ctx) -> contextValue.set(ctx.getContextData().get("key").toString())));

    AsyncPolicyUtils.raiseResultsOnExecuteAndCapture(policy,
      Context.builder()
        .contextData(new HashMap<>() {{
          put("key", "original_value");
        }}).build(),
      TestResult.FAULT,
      TestResult.GOOD)
      .join();

    assertThat(contextValue.get()).isEqualTo("original_value");

    AsyncPolicyUtils.raiseResultsOnExecuteAndCapture(policy,
      Context.builder()
        .contextData(new HashMap<>() {{
          put("key", "new_value");
        }}).build(),
      TestResult.FAULT,
      TestResult.GOOD)
      .join();

    assertThat(contextValue.get()).isEqualTo("new_value");
  }

  @Test
  public void shouldCreateNewStateForEachCallToPolicy() {
    final var policy = AsyncRetryPolicy.<TestResult>builder()
      .handleResult(TestResult.FAULT)
      .retry(1);

    final var result1 = AsyncPolicyUtils.raiseResults(policy, TestResult.FAULT, TestResult.GOOD)
      .join();

    assertThat(result1).isEqualTo(TestResult.GOOD);

    final var result2 = AsyncPolicyUtils.raiseResults(policy, TestResult.FAULT, TestResult.GOOD)
      .join();

    assertThat(result2).isEqualTo(TestResult.GOOD);
  }

  @Test
  public void shouldNotCallOnRetryWhenRetryCountIsZero() {
    final var retryInvoked = new AtomicBoolean(false);
    final var policy = AsyncRetryPolicy.<TestResult>builder()
      .handleResult(TestResult.FAULT)
      .retry(0, fromConsumer3Async(r -> (i, c) -> retryInvoked.set(true)));

    final var result = AsyncPolicyUtils.raiseResults(policy, TestResult.FAULT, TestResult.GOOD)
      .join();

    assertThat(result).isEqualTo(TestResult.FAULT);
    assertThat(retryInvoked.get()).isFalse();
  }

  @Test
  public void shouldWaitAsynchronouslyForAsyncOnRetryDelegate() {
    final var duration = Duration.ofMillis(200);
    final var executeDelegateInvocations = new AtomicInteger(0);
    final var executeDelegateInvocationsWhenOnRetryExits = new AtomicInteger(0);
    final var policy = AsyncRetryPolicy.<TestResult>builder()
      .handleResult(TestResult.FAULT)
      .retry((r, i, c) -> {
        final var executor = CompletableFuture.delayedExecutor(duration.toMillis(), TimeUnit.MILLISECONDS);
        return CompletableFuture
          .runAsync(() -> executeDelegateInvocationsWhenOnRetryExits.set(executeDelegateInvocations.get()), executor)
          .thenApplyAsync(v -> Unit.INSTANCE);
      });

    policy.executeAsync(() -> {
      executeDelegateInvocations.incrementAndGet();
      return CompletableFuture.completedFuture(TestResult.FAULT);
    })
      .whenCompleteAsync((result, e) -> {
        if (e != null) {
          fail("Exception while executing policy.", e);
        } else {
          assertThat(result).isEqualTo(TestResult.FAULT);

          assertThat(executeDelegateInvocationsWhenOnRetryExits.get()).isEqualTo(1);
          assertThat(executeDelegateInvocations.get()).isEqualTo(2);
        }
      })
      .join();
  }

  @Test
  public void shouldExecuteAllTriesWhenFaultingAndNotCancelled() {
    final var policy = AsyncRetryPolicy.<TestResult>builder()
      .handleResult(TestResult.FAULT)
      .retry(3);
    final var attemptsInvoked = new AtomicInteger(0);
    final Function1<TestResult, CompletableFuture<TestResult>> action = r -> {
      attemptsInvoked.incrementAndGet();
      return CompletableFuture.completedFuture(r);
    };

    final var result = AsyncPolicyUtils.raiseResults(policy, action, TestResult.FAULT, TestResult.FAULT,
      TestResult.FAULT, TestResult.GOOD)
      .join();

    assertThat(result).isEqualTo(TestResult.GOOD);
    assertThat(attemptsInvoked.get()).isEqualTo(1 + 3);
  }

  @Test
  public void shouldNotExecuteActionWhenCancelledBeforeExecute() {
    final var policy = AsyncRetryPolicy.<TestResult>builder()
      .handleResult(TestResult.FAULT)
      .retry(3);
    final var attemptsInvoked = new AtomicInteger(0);
    final Executor executor = CompletableFuture.delayedExecutor(200, TimeUnit.MILLISECONDS);
    final CompletableFuture<Void> dummyFuture = CompletableFuture.runAsync(() -> {
    }, executor);
    final Function1<TestResult, CompletableFuture<TestResult>> action = r ->
      dummyFuture.thenComposeAsync(v -> {
        attemptsInvoked.incrementAndGet();
        return CompletableFuture.completedFuture(r);
      });

    dummyFuture.cancel(true);

    assertThatExceptionOfType(CancellationException.class).isThrownBy(() ->
      AsyncPolicyUtils.raiseResults(policy, action, TestResult.FAULT, TestResult.FAULT, TestResult.FAULT, TestResult.GOOD)
        .join());
    assertThat(attemptsInvoked.get()).isEqualTo(0);
  }

  @Test
  public void shouldReportCancellationDuringOtherwiseNonFaultingActionExecutionAndCancelFurtherRetries() {
    final var policy = AsyncRetryPolicy.<TestResult>builder()
      .handleResult(TestResult.FAULT)
      .retry(3);
    final var attemptsInvoked = new AtomicInteger(0);
    final Function1<TestResult, CompletableFuture<TestResult>> action = r -> {
      attemptsInvoked.incrementAndGet();
      return CompletableFuture.completedFuture(r);
    };

    assertThatExceptionOfType(CancellationException.class).isThrownBy(() ->
      AsyncPolicyUtils.raiseResultsAndOrCancellation(policy, 1, action, TestResult.GOOD,
        TestResult.GOOD, TestResult.GOOD, TestResult.GOOD)
        .join());
    assertThat(attemptsInvoked.get()).isEqualTo(1);
  }

  @Test
  public void shouldReportCancellationDuringFaultingInitialActionExecutionAndCancelFurtherRetries() {
    final var policy = AsyncRetryPolicy.<TestResult>builder()
      .handleResult(TestResult.FAULT)
      .retry(3);
    final var attemptsInvoked = new AtomicInteger(0);
    final Function1<TestResult, CompletableFuture<TestResult>> action = r -> {
      attemptsInvoked.incrementAndGet();
      return CompletableFuture.completedFuture(r);
    };

    assertThatExceptionOfType(CancellationException.class).isThrownBy(() ->
      AsyncPolicyUtils.raiseResultsAndOrCancellation(policy, 1, action, TestResult.FAULT,
        TestResult.FAULT, TestResult.FAULT, TestResult.GOOD)
        .join());
    assertThat(attemptsInvoked.get()).isEqualTo(1);
  }

  @Test
  public void shouldReportCancellationDuringFaultingRetriedActionExecutionAndCancelFurtherRetries() {
    final var policy = AsyncRetryPolicy.<TestResult>builder()
      .handleResult(TestResult.FAULT)
      .retry(3);
    final var attemptsInvoked = new AtomicInteger(0);
    final Function1<TestResult, CompletableFuture<TestResult>> action = r -> {
      attemptsInvoked.incrementAndGet();
      return CompletableFuture.completedFuture(r);
    };

    assertThatExceptionOfType(CancellationException.class).isThrownBy(() ->
      AsyncPolicyUtils.raiseResultsAndOrCancellation(policy, 2, action, TestResult.FAULT,
        TestResult.FAULT, TestResult.FAULT, TestResult.GOOD)
        .join());
    assertThat(attemptsInvoked.get()).isEqualTo(2);
  }

  @Test
  public void shouldReportCancellationDuringFaultingLastRetryExecution() {
    final var policy = AsyncRetryPolicy.<TestResult>builder()
      .handleResult(TestResult.FAULT)
      .retry(3);
    final var attemptsInvoked = new AtomicInteger(0);
    final Function1<TestResult, CompletableFuture<TestResult>> action = r -> {
      attemptsInvoked.incrementAndGet();
      return CompletableFuture.completedFuture(r);
    };

    assertThatExceptionOfType(CancellationException.class).isThrownBy(() ->
      AsyncPolicyUtils.raiseResultsAndOrCancellation(policy, 1 + 3, action, TestResult.FAULT,
        TestResult.FAULT, TestResult.FAULT, TestResult.GOOD)
        .join());
    assertThat(attemptsInvoked.get()).isEqualTo(1 + 3);
  }

  @Test
  public void shouldReportCancellationAfterFaultingActionExecutionAndCancelFurtherRetriesIfOnRetryInvokesCancellation() {
    final var policy = AsyncRetryPolicy.<TestResult>builder()
      .handleResult(TestResult.FAULT)
      .retry(3, fromConsumer3Async(r -> (i, c) -> {
        throw new CancellationException();
      }));
    final var attemptsInvoked = new AtomicInteger(0);
    final Function1<TestResult, CompletableFuture<TestResult>> action = r -> {
      attemptsInvoked.incrementAndGet();
      return CompletableFuture.completedFuture(r);
    };

    assertThatExceptionOfType(CancellationException.class).isThrownBy(() ->
      AsyncPolicyUtils.raiseResults(policy, action, TestResult.FAULT, TestResult.FAULT, TestResult.FAULT,
        TestResult.GOOD)
        .join());
    assertThat(attemptsInvoked.get()).isEqualTo(1);
  }
}
