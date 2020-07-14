package hr.tjakopan.yarl.retry;

import hr.tjakopan.yarl.test.helpers.PolicyUtils;
import hr.tjakopan.yarl.test.helpers.TestResult;
import hr.tjakopan.yarl.test.helpers.TestResultClass;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static hr.tjakopan.yarl.Functions.fromConsumer3;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class RetryPolicyHandleResultTest {
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
}
