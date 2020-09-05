package hr.tjakopan.yarl.retry;

import hr.tjakopan.yarl.Context;
import hr.tjakopan.yarl.PolicyResult;
import hr.tjakopan.yarl.test.helpers.PolicyUtils;
import hr.tjakopan.yarl.test.helpers.TestResult;
import hr.tjakopan.yarl.test.helpers.TestResultClass;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import static hr.tjakopan.yarl.Functions.fromConsumer3;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class RetryHandleResultTest {
  @Test
  public void shouldThrowWhenRetryCountIsLessThanZero() {
    assertThatThrownBy(() ->
      RetryPolicy.<TestResult>builder()
        .handleResult(TestResult.FAULT)
        .retry(-1))
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessageContaining("Retry count");
  }

  @Test
  public void shouldThrowWhenOnRetryActionIsNull() {
    //noinspection ConstantConditions
    assertThatThrownBy(() ->
      RetryPolicy.<TestResult>builder()
        .handleResult(TestResult.FAULT)
        .retry(null))
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessageContaining("onRetry");
  }

  @Test
  public void shouldNotReturnHandledResultWhenHandledResultRaisedSameNumberOfTimesAsRetryCount() {
    final var policy = RetryPolicy.<TestResult>builder()
      .handleResult(TestResult.FAULT)
      .retry(3);

    final var result = PolicyUtils.raiseResults(policy, TestResult.FAULT, TestResult.FAULT, TestResult.FAULT,
      TestResult.GOOD);

    assertThat(result).isEqualTo(TestResult.GOOD);
  }

  @Test
  public void shouldNotReturnHandledResultWhenOneOfTheHandledResultsRaisedSameNumberOfTimesAsRetryCount() {
    final var policy = RetryPolicy.<TestResult>builder()
      .handleResult(TestResult.FAULT)
      .handleResult(TestResult.FAULT_AGAIN)
      .retry(3);

    final var result = PolicyUtils.raiseResults(policy, TestResult.FAULT, TestResult.FAULT_AGAIN, TestResult.FAULT,
      TestResult.GOOD);

    assertThat(result).isEqualTo(TestResult.GOOD);
  }

  @Test
  public void shouldNotReturnHandledResultWhenHandledResultRaisedLessNumberOfTimesThanRetryCount() {
    final var policy = RetryPolicy.<TestResult>builder()
      .handleResult(TestResult.FAULT)
      .retry(3);

    final var result = PolicyUtils.raiseResults(policy, TestResult.FAULT, TestResult.GOOD);

    assertThat(result).isEqualTo(TestResult.GOOD);
  }

  @Test
  public void shouldNotReturnHandledResultWhenAllOfTheHandledResultsRaisedLessNumberOfTimesThanRetryCount() {
    final var policy = RetryPolicy.<TestResult>builder()
      .handleResult(TestResult.FAULT)
      .handleResult(TestResult.FAULT_AGAIN)
      .retry(3);

    final var result = PolicyUtils.raiseResults(policy, TestResult.FAULT, TestResult.FAULT_AGAIN, TestResult.GOOD);

    assertThat(result).isEqualTo(TestResult.GOOD);
  }

  @Test
  public void shouldReturnHandledResultWhenHandledResultRaisedMoreTimesThenRetryCount() {
    final var policy = RetryPolicy.<TestResult>builder()
      .handleResult(TestResult.FAULT)
      .retry(3);

    final var result = PolicyUtils.raiseResults(policy, TestResult.FAULT, TestResult.FAULT, TestResult.FAULT,
      TestResult.FAULT, TestResult.GOOD);


    assertThat(result).isEqualTo(TestResult.FAULT);
  }

  @Test
  public void shouldReturnHandledResultWhenOneOfTheHandledResultsIsRaisedMoreTimesThenRetryCount() {
    final var policy = RetryPolicy.<TestResult>builder()
      .handleResult(TestResult.FAULT)
      .handleResult(TestResult.FAULT_AGAIN)
      .retry(3);

    final var result = PolicyUtils.raiseResults(policy, TestResult.FAULT_AGAIN, TestResult.FAULT_AGAIN,
      TestResult.FAULT_AGAIN, TestResult.FAULT_AGAIN, TestResult.GOOD);

    assertThat(result).isEqualTo(TestResult.FAULT_AGAIN);
  }

  @Test
  public void shouldReturnResultWhenResultIsNotTheSpecifiedHandledResult() {
    final var policy = RetryPolicy.<TestResult>builder()
      .handleResult(TestResult.FAULT)
      .retry();

    final var result = PolicyUtils.raiseResults(policy, TestResult.FAULT_AGAIN, TestResult.GOOD);

    assertThat(result).isEqualTo(TestResult.FAULT_AGAIN);
  }

  @Test
  public void shouldReturnResultWhenResultIsNotOneOfTheSpecifiedHandledResults() {
    final var policy = RetryPolicy.<TestResult>builder()
      .handleResult(TestResult.FAULT)
      .handleResult(TestResult.FAULT_AGAIN)
      .retry();

    final var result = PolicyUtils.raiseResults(policy, TestResult.FAULT_YET_AGAIN, TestResult.GOOD);

    assertThat(result).isEqualTo(TestResult.FAULT_YET_AGAIN);
  }

  @Test
  public void shouldReturnResultWhenSpecifiedResultPredicateIsNotSatisfied() {
    final var policy = RetryPolicy.<TestResultClass>builder()
      .handleResult(r -> r.getResultCode() == TestResult.FAULT)
      .retry();

    final var result = PolicyUtils.raiseResults(policy, new TestResultClass(TestResult.FAULT_AGAIN),
      new TestResultClass(TestResult.GOOD));

    assertThat(result.getResultCode()).isEqualTo(TestResult.FAULT_AGAIN);
  }

  @Test
  public void shouldReturnResultWhenNoneOfTheSpecifiedResultPredicatesAreSatisfied() {
    final var policy = RetryPolicy.<TestResultClass>builder()
      .handleResult(r -> r.getResultCode() == TestResult.FAULT_AGAIN)
      .retry();

    final var result = PolicyUtils.raiseResults(policy, new TestResultClass(TestResult.FAULT_YET_AGAIN),
      new TestResultClass(TestResult.GOOD));

    assertThat(result.getResultCode()).isEqualTo(TestResult.FAULT_YET_AGAIN);
  }

  @Test
  public void shouldNotReturnHandledResultWhenSpecifiedResultPredicateIsSatisfied() {
    final var policy = RetryPolicy.<TestResultClass>builder()
      .handleResult(r -> r.getResultCode() == TestResult.FAULT)
      .retry();

    final var result = PolicyUtils.raiseResults(policy, new TestResultClass(TestResult.FAULT),
      new TestResultClass(TestResult.GOOD));

    assertThat(result.getResultCode()).isEqualTo(TestResult.GOOD);
  }

  @Test
  public void shouldNotReturnHandledResultWhenOneOfTheSpecifiedResultPredicatesIsSatisfied() {
    final var policy = RetryPolicy.<TestResultClass>builder()
      .handleResult(r -> r.getResultCode() == TestResult.FAULT)
      .handleResult(r -> r.getResultCode() == TestResult.FAULT_AGAIN)
      .retry();

    final var result = PolicyUtils.raiseResults(policy, new TestResultClass(TestResult.FAULT_AGAIN),
      new TestResultClass(TestResult.GOOD));

    assertThat(result.getResultCode()).isEqualTo(TestResult.GOOD);
  }

  @Test
  public void shouldCallOnRetryOnEachRetryWithTheCurrentRetryCount() {
    final var expectedRetryCounts = List.of(1, 2, 3);
    final var retryCounts = new ArrayList<Integer>(3);
    final var policy = RetryPolicy.<TestResult>builder()
      .handleResult(TestResult.FAULT)
      .retry(3, fromConsumer3(r -> (retryCount, c) -> retryCounts.add(retryCount)));

    PolicyUtils.raiseResults(policy, TestResult.FAULT, TestResult.FAULT, TestResult.FAULT, TestResult.GOOD);

    assertThat(retryCounts).containsExactlyElementsOf(expectedRetryCounts);
  }

  @Test
  public void shouldCallOnRetryOnEachRetryWithTheCurrentHandledResult() {
    final var expectedFaults = List.of("Fault #1", "Fault #2", "Fault #3");
    final var retryFaults = new ArrayList<String>(3);
    final var policy = RetryPolicy.<TestResultClass>builder()
      .handleResult(r -> r.getResultCode() == TestResult.FAULT)
      .retry(3, fromConsumer3(outcome -> (i, c) -> retryFaults.add(outcome.getOrThrow().getSomeString())));
    final var resultsToRaise = expectedFaults.stream()
      .map(s -> new TestResultClass(TestResult.FAULT, s))
      .collect(Collectors.toList());
    resultsToRaise.add(new TestResultClass(TestResult.FAULT));

    PolicyUtils.raiseResults(policy, resultsToRaise.toArray(TestResultClass[]::new));

    assertThat(retryFaults).containsExactlyElementsOf(expectedFaults);
  }

  @Test
  public void shouldNotCallOnRetryWhenNoRetriesArePerformed() {
    final var retryCalled = new AtomicBoolean(false);
    final var policy = RetryPolicy.<TestResult>builder()
      .handleResult(TestResult.FAULT)
      .retry(fromConsumer3(r -> (i, c) -> retryCalled.set(true)));

    PolicyUtils.raiseResults(policy, TestResult.GOOD);

    assertThat(retryCalled.get()).isFalse();
  }

  @Test
  public void shouldCallOnRetryWithThePassedContext() {
    final var capturedContext = new AtomicReference<Context>();
    final var policy = RetryPolicy.<TestResult>builder()
      .handleResult(TestResult.FAULT)
      .retry(fromConsumer3(r -> (i, context) -> capturedContext.set(context)));
    final var context = Context.builder()
      .contextData(new HashMap<>() {{
        put("key1", "value1");
        put("key2", "value2");
      }})
      .build();

    final var result = PolicyUtils.raiseResults(policy, context, TestResult.FAULT, TestResult.GOOD);

    assertThat(result).isEqualTo(TestResult.GOOD);
    assertThat(capturedContext.get().getContextData()).containsKeys("key1", "key2")
      .containsValues("value1", "value2");
  }

  @Test
  public void shouldCallOnRetryWithThePassedContextWhenExecuteAndCapture() {
    final var capturedContext = new AtomicReference<Context>();
    final var policy = RetryPolicy.<TestResult>builder()
      .handleResult(TestResult.FAULT)
      .retry(fromConsumer3(r -> (i, context) -> capturedContext.set(context)));
    final var context = Context.builder()
      .contextData(new HashMap<>() {{
        put("key1", "value1");
        put("key2", "value2");
      }})
      .build();

    final var result = PolicyUtils.raiseResultsOnExecuteAndCapture(policy, context, TestResult.FAULT, TestResult.GOOD);

    assertThat(result.isSuccess()).isTrue();
    //noinspection rawtypes
    assertThat(((PolicyResult.Success) result).getResult()).isEqualTo(TestResult.GOOD);
    assertThat(capturedContext.get().getContextData()).containsKeys("key1", "key2")
      .containsValues("value1", "value2");
  }

  @Test
  public void contextShouldBeEmptyIfExecuteNotCalledWithContext() {
    final var capturedContext = new AtomicReference<Context>();
    final var policy = RetryPolicy.<TestResult>builder()
      .handleResult(TestResult.FAULT)
      .retry(fromConsumer3(r -> (i, context) -> capturedContext.set(context)));

    PolicyUtils.raiseResults(policy, TestResult.FAULT, TestResult.GOOD);

    assertThat(capturedContext.get()).isNotNull();
    assertThat(capturedContext.get().getPolicyWrapKey()).isNull();
    assertThat(capturedContext.get().getPolicyKey()).isNotNull();
    assertThat(capturedContext.get().getOperationKey()).isNull();
    assertThat(capturedContext.get().getContextData()).isEmpty();
  }

  @Test
  public void shouldCreateNewContextForEachCallToExecute() {
    final var contextValue = new AtomicReference<String>();
    final var policy = RetryPolicy.<TestResult>builder()
      .handleResult(TestResult.FAULT)
      .retry(fromConsumer3(r -> (i, context) -> contextValue.set(context.getContextData().get("key").toString())));
    var context1 = Context.builder()
      .contextData(new HashMap<>() {{
        put("key", "original_value");
      }})
      .build();
    var context2 = Context.builder()
      .contextData(new HashMap<>() {{
        put("key", "new_value");
      }})
      .build();

    PolicyUtils.raiseResults(policy, context1, TestResult.FAULT, TestResult.GOOD);

    assertThat(contextValue.get()).isEqualTo("original_value");

    PolicyUtils.raiseResults(policy, context2, TestResult.FAULT, TestResult.GOOD);

    assertThat(contextValue.get()).isEqualTo("new_value");
  }

  @Test
  public void shouldCreateNewContextForEachCallToExecuteAndCapture() {
    final var contextValue = new AtomicReference<String>();
    final var policy = RetryPolicy.<TestResult>builder()
      .handleResult(TestResult.FAULT)
      .retry(fromConsumer3(r -> (i, context) -> contextValue.set(context.getContextData().get("key").toString())));
    var context1 = Context.builder()
      .contextData(new HashMap<>() {{
        put("key", "original_value");
      }})
      .build();
    var context2 = Context.builder()
      .contextData(new HashMap<>() {{
        put("key", "new_value");
      }})
      .build();

    PolicyUtils.raiseResultsOnExecuteAndCapture(policy, context1, TestResult.FAULT, TestResult.GOOD);

    assertThat(contextValue.get()).isEqualTo("original_value");

    PolicyUtils.raiseResultsOnExecuteAndCapture(policy, context2, TestResult.FAULT, TestResult.GOOD);

    assertThat(contextValue.get()).isEqualTo("new_value");
  }

  @Test
  public void shouldCreateNewStateForEachCallToPolicy() {
    final var policy = RetryPolicy.<TestResult>builder()
      .handleResult(TestResult.FAULT)
      .retry(1);

    final var result1 = PolicyUtils.raiseResults(policy, TestResult.FAULT, TestResult.GOOD);
    final var result2 = PolicyUtils.raiseResults(policy, TestResult.FAULT, TestResult.GOOD);

    assertThat(result1).isEqualTo(TestResult.GOOD);
    assertThat(result2).isEqualTo(TestResult.GOOD);
  }

  @Test
  public void shouldNotCallOnRetryWhenRetryCountIsZero() {
    final var retryInvoked = new AtomicBoolean(false);
    final var policy = RetryPolicy.<TestResult>builder()
      .handleResult(TestResult.FAULT)
      .retry(0, fromConsumer3(r -> (i, c) -> retryInvoked.set(true)));

    final var result = PolicyUtils.raiseResults(policy, TestResult.FAULT, TestResult.GOOD);

    assertThat(result).isEqualTo(TestResult.FAULT);
    assertThat(retryInvoked.get()).isFalse();
  }
}
