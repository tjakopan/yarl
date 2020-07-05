package hr.tjakopan.yarl.retry

import hr.tjakopan.yarl.Policy
import hr.tjakopan.yarl.test.helpers.TestResult
import hr.tjakopan.yarl.test.helpers.raiseResults
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import kotlin.test.Test

@ExperimentalCoroutinesApi
class RetryAsyncTest {
  @Test
  fun shouldThrowWhenRetryCountIsLessThanZero() {
    val shouldThrow = {
      Policy.asyncRetry<TestResult>()
        .handleResult(TestResult.FAULT)
        .retry(-1)
      Unit
    }

    assertThatThrownBy(shouldThrow)
      .isInstanceOf(IllegalArgumentException::class.java)
      .hasMessageContaining("Retry count")
  }

  @Test
  fun shouldNotReturnHandledResultWhenHandledResultRaisedSameNumberOfTimesAsRetryCount() = runBlockingTest {
    val policy = Policy.asyncRetry<TestResult>()
      .handleResult(TestResult.FAULT)
      .retry(3)

    val result = policy.raiseResults(TestResult.FAULT, TestResult.FAULT, TestResult.FAULT, TestResult.GOOD)

    assertThat(result).isEqualTo(TestResult.GOOD)
  }

  @Test
  fun shouldNotReturnHandledResultWhenOneOfTheHandledResultsRaisedSameNumberOfTimesAsRetryCount() = runBlockingTest {
    val policy = Policy.asyncRetry<TestResult>()
      .handleResult(TestResult.FAULT)
      .handleResult(TestResult.FAULT_AGAIN)
      .retry(3)

    val result = policy.raiseResults(TestResult.FAULT, TestResult.FAULT_AGAIN, TestResult.FAULT, TestResult.GOOD)

    assertThat(result).isEqualTo(TestResult.GOOD)
  }

  @Test
  fun shouldNotReturnHandledResultWhenHandledResultRaisedLessNumberOfTimesThanRetryCount() = runBlockingTest {
    val policy = Policy.asyncRetry<TestResult>()
      .handleResult(TestResult.FAULT)
      .retry(3)

    val result = policy.raiseResults(TestResult.FAULT, TestResult.GOOD)

    assertThat(result).isEqualTo(TestResult.GOOD)
  }

  @Test
  fun shouldNotReturnHandledResultWhenAllOfTheHandledResultsRaisedLessNumberOfTimesThanRetryCount() = runBlockingTest {
    val policy = Policy.asyncRetry<TestResult>()
      .handleResult(TestResult.FAULT)
      .handleResult(TestResult.FAULT_AGAIN)
      .retry(3)

    val result = policy.raiseResults(TestResult.FAULT, TestResult.FAULT_AGAIN, TestResult.GOOD)

    assertThat(result).isEqualTo(TestResult.GOOD)
  }

  @Test
  fun shouldReturnHandledResultWhenHandledResultRaisedMoreTimesThanRetryCount() = runBlockingTest {
    val policy = Policy.asyncRetry<TestResult>()
      .handleResult(TestResult.FAULT)
      .retry(3)

    val result =
      policy.raiseResults(TestResult.FAULT, TestResult.FAULT, TestResult.FAULT, TestResult.FAULT, TestResult.GOOD)

    assertThat(result).isEqualTo(TestResult.FAULT)
  }
}
