package hr.tjakopan.yarl.retry;

import hr.tjakopan.yarl.Policy;
import hr.tjakopan.yarl.test.helpers.AsyncPolicyUtils;
import hr.tjakopan.yarl.test.helpers.TestResult;
import org.assertj.core.api.ThrowableAssert;
import org.junit.Test;

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
}
