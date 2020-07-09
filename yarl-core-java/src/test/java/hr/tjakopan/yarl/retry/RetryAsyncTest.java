package hr.tjakopan.yarl.retry;

import hr.tjakopan.yarl.test.helpers.AsyncPolicyUtils;
import hr.tjakopan.yarl.test.helpers.TestResult;
import hr.tjakopan.yarl.test.helpers.TestResultClass;
import kotlin.Unit;
import org.assertj.core.api.ThrowableAssert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class RetryAsyncTest {
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
        .retryAsync(1, null);
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

    final var result = AsyncPolicyUtils.raiseResults(policy, TestResult.FAULT, TestResult.FAULT, TestResult.FAULT, TestResult.GOOD);

    assertThat(result).isEqualTo(TestResult.GOOD);
  }

  @Test
  public void shouldNotReturnHandledResultWhenOneOfTheHandledResultsRaisedSameNumberOfTimesAsRetryCount() {
    final var policy = AsyncRetryPolicy.<TestResult>builder()
      .handleResult(TestResult.FAULT)
      .handleResult(TestResult.FAULT_AGAIN)
      .retry(3);

    final var result = AsyncPolicyUtils.raiseResults(policy, TestResult.FAULT, TestResult.FAULT_AGAIN, TestResult.FAULT, TestResult.GOOD);

    assertThat(result).isEqualTo(TestResult.GOOD);
  }

  @Test
  public void shouldNotReturnHandledResultWhenHandledResultRaisedLessNumberOfTimesThanRetryCount() {
    final var policy = AsyncRetryPolicy.<TestResult>builder()
      .handleResult(TestResult.FAULT)
      .retry(3);

    final var result = AsyncPolicyUtils.raiseResults(policy, TestResult.FAULT, TestResult.GOOD);

    assertThat(result).isEqualTo(TestResult.GOOD);
  }

  @Test
  public void shouldNotReturnHandledResultWhenAllOfTheHandledResultsRaisedLessNumberOfTimesThanRetryCount() {
    final var policy = AsyncRetryPolicy.<TestResult>builder()
      .handleResult(TestResult.FAULT)
      .handleResult(TestResult.FAULT_AGAIN)
      .retry(3);

    final var result = AsyncPolicyUtils.raiseResults(policy, TestResult.FAULT, TestResult.FAULT_AGAIN, TestResult.GOOD);

    assertThat(result).isEqualTo(TestResult.GOOD);
  }

  @Test
  public void shouldReturnHandledResultWhenHandledResultRaisedMoreTimesThanRetryCount() {
    final var policy = AsyncRetryPolicy.<TestResult>builder()
      .handleResult(TestResult.FAULT)
      .retry(3);

    final var result = AsyncPolicyUtils.raiseResults(policy, TestResult.FAULT, TestResult.FAULT, TestResult.FAULT, TestResult.FAULT, TestResult.GOOD);

    assertThat(result).isEqualTo(TestResult.FAULT);
  }

  @Test
  public void shouldReturnHandledResultWhenOneOfTheHandledResultsIsRaisedMoreTimesThanRetryCount() {
    final var policy = AsyncRetryPolicy.<TestResult>builder()
      .handleResult(TestResult.FAULT)
      .handleResult(TestResult.FAULT_AGAIN)
      .retry(3);

    final var result = AsyncPolicyUtils.raiseResults(policy, TestResult.FAULT_AGAIN, TestResult.FAULT_AGAIN,
      TestResult.FAULT_AGAIN, TestResult.FAULT_AGAIN, TestResult.GOOD);

    assertThat(result).isEqualTo(TestResult.FAULT_AGAIN);
  }

  @Test
  public void shouldReturnResultWhenResultIsNotTheSpecifiedHandledResult() {
    final var policy = AsyncRetryPolicy.<TestResult>builder()
      .handleResult(TestResult.FAULT)
      .retry();

    final var result = AsyncPolicyUtils.raiseResults(policy, TestResult.FAULT_AGAIN, TestResult.GOOD);

    assertThat(result).isEqualTo(TestResult.FAULT_AGAIN);
  }

  @Test
  public void shouldReturnResultWhenResultIsNotOneOfTheSpecifiedHandledResults() {
    final var policy = AsyncRetryPolicy.<TestResult>builder()
      .handleResult(TestResult.FAULT)
      .handleResult(TestResult.FAULT_AGAIN)
      .retry();

    final var result = AsyncPolicyUtils.raiseResults(policy, TestResult.FAULT_YET_AGAIN, TestResult.GOOD);

    assertThat(result).isEqualTo(TestResult.FAULT_YET_AGAIN);
  }

  @Test
  public void shouldReturnResultWhenSpecifiedResultPredicateIsNotSatisfied() {
    final var policy = AsyncRetryPolicy.<TestResultClass>builder()
      .handleResult(r -> r.getResultCode() == TestResult.FAULT)
      .retry();

    final var result = AsyncPolicyUtils.raiseResults(policy, new TestResultClass(TestResult.FAULT_AGAIN),
      new TestResultClass(TestResult.GOOD));

    assertThat(result.getResultCode()).isEqualTo(TestResult.FAULT_AGAIN);
  }

  @Test
  public void shouldReturnResultWhenNoneOfTheSpecifiedResultPredicatesAreSatisfied() {
    final var policy = AsyncRetryPolicy.<TestResultClass>builder()
      .handleResult(r -> r.getResultCode() == TestResult.FAULT)
      .handleResult(r -> r.getResultCode() == TestResult.FAULT_AGAIN)
      .retry();

    final var result = AsyncPolicyUtils.raiseResults(policy, new TestResultClass(TestResult.FAULT_YET_AGAIN),
      new TestResultClass(TestResult.GOOD));

    assertThat(result.getResultCode()).isEqualTo(TestResult.FAULT_YET_AGAIN);
  }

  @Test
  public void shouldNotReturnHandledResultWhenSpecifiedResultPredicateIsSatisfied() {
    final var policy = AsyncRetryPolicy.<TestResultClass>builder()
      .handleResult(r -> r.getResultCode() == TestResult.FAULT)
      .retry();

    final var result = AsyncPolicyUtils.raiseResults(policy, new TestResultClass(TestResult.FAULT),
      new TestResultClass(TestResult.GOOD));

    assertThat(result.getResultCode()).isEqualTo(TestResult.GOOD);
  }

  @Test
  public void shouldNotReturnHandledResultWhenOneOfTheSpecifiedResultPredicatesIsSatisfied() {
    final var policy = AsyncRetryPolicy.<TestResultClass>builder()
      .handleResult(r -> r.getResultCode() == TestResult.FAULT)
      .handleResult(r -> r.getResultCode() == TestResult.FAULT_AGAIN)
      .retry();

    final var result = AsyncPolicyUtils.raiseResults(policy, new TestResultClass(TestResult.FAULT_AGAIN),
      new TestResultClass(TestResult.GOOD));

    assertThat(result.getResultCode()).isEqualTo(TestResult.GOOD);
  }

  @Test
  public void shouldCallOnRetryOnEachRetryWithTheCurrentRetryCount() {
    final var expectedRetryCounts = List.of(1, 2, 3);
    final var retryCounts = new ArrayList<Integer>(3);
    final var policy = AsyncRetryPolicy.<TestResult>builder()
      .handleResult(TestResult.FAULT)
      .retryAsync(3, (r, retryCount, ctx) -> {
        retryCounts.add(retryCount);
        return CompletableFuture.completedFuture(Unit.INSTANCE);
      });

    final var result = AsyncPolicyUtils.raiseResults(policy, TestResult.FAULT, TestResult.FAULT, TestResult.FAULT,
      TestResult.GOOD);

    assertThat(result).isEqualTo(TestResult.GOOD);
    assertThat(retryCounts).containsExactlyElementsOf(expectedRetryCounts);
  }

  @Test
  public void shouldCallOnRetryOnEachRetryWithTheCurrentHandledResult() {
//    final var expectedFaults = List.of("Fault #1", "Fault #2", "Fault #3");
//    final var retryFaults = new ArrayList<String>();
//    final var policy = AsyncRetryPolicy.<TestResultClass>builder()
//      .handleResult(r -> r.getResultCode() == TestResult.FAULT)
//      .retryAsync()
  }
}
