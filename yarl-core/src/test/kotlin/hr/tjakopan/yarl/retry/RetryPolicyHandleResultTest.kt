package hr.tjakopan.yarl.retry

import hr.tjakopan.yarl.Policy
import hr.tjakopan.yarl.test.helpers.TestResult
import hr.tjakopan.yarl.test.helpers.TestResultClass
import hr.tjakopan.yarl.test.helpers.raiseResults
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import kotlin.test.Test

class RetryPolicyHandleResultTest {
  @Test
  fun shouldThrowWhenRetryCountIsLessThanZero() {
    assertThatThrownBy {
      Policy.retry<TestResult>()
        .handleResult(TestResult.FAULT)
        .retry(-1)
    }
      .isInstanceOf(IllegalArgumentException::class.java)
      .hasMessageContaining("Retry count")
  }

  @Test
  fun shouldNotReturnHandledResultWhenHandledResultRaisedSameNumberOfTimesAsRetryCount() {
    val policy = Policy.retry<TestResult>()
      .handleResult(TestResult.FAULT)
      .retry(3)

    val result = policy.raiseResults(TestResult.FAULT, TestResult.FAULT, TestResult.FAULT, TestResult.GOOD)

    assertThat(result).isEqualTo(TestResult.GOOD)
  }

  @Test
  fun shouldNotReturnHandledResultWhenOneOfTheHandledResultsRaisedSameNumberOfTimesAsRetryCount() {
    val policy = Policy.retry<TestResult>()
      .handleResult(TestResult.FAULT)
      .handleResult(TestResult.FAULT_AGAIN)
      .retry(3)

    val result = policy.raiseResults(TestResult.FAULT, TestResult.FAULT_AGAIN, TestResult.FAULT, TestResult.GOOD)

    assertThat(result).isEqualTo(TestResult.GOOD)
  }

  @Test
  fun shouldNotReturnHandledResultWhenHandledResultRaisedLessNumberOfTimesThanRetryCount() {
    val policy = Policy.retry<TestResult>()
      .handleResult(TestResult.FAULT)
      .retry(3)

    val result = policy.raiseResults(TestResult.FAULT, TestResult.GOOD)

    assertThat(result).isEqualTo(TestResult.GOOD)
  }

  @Test
  fun shouldNotReturnHandledResultWhenAllOfTheHandledResultsRaisedLessNumberOfTimesThanRetryCount() {
    val policy = Policy.retry<TestResult>()
      .handleResult(TestResult.FAULT)
      .handleResult(TestResult.FAULT_AGAIN)
      .retry(3)

    val result = policy.raiseResults(TestResult.FAULT, TestResult.FAULT_AGAIN, TestResult.GOOD)

    assertThat(result).isEqualTo(TestResult.GOOD)
  }

  @Test
  fun shouldReturnHandledResultWhenHandledResultRaisedMoreTimesThenRetryCount() {
    val policy = Policy.retry<TestResult>()
      .handleResult(TestResult.FAULT)
      .retry(3)

    val result =
      policy.raiseResults(TestResult.FAULT, TestResult.FAULT, TestResult.FAULT, TestResult.FAULT, TestResult.GOOD)

    assertThat(result).isEqualTo(TestResult.FAULT)
  }

  @Test
  fun shouldReturnHandledResultWhenOneOfTheHandledResultsIsRaisedMoreTimesThenRetryCount() {
    val policy = Policy.retry<TestResult>()
      .handleResult(TestResult.FAULT)
      .handleResult(TestResult.FAULT_AGAIN)
      .retry(3)

    val result = policy.raiseResults(
      TestResult.FAULT_AGAIN,
      TestResult.FAULT_AGAIN,
      TestResult.FAULT_AGAIN,
      TestResult.FAULT_AGAIN,
      TestResult.GOOD
    )

    assertThat(result).isEqualTo(TestResult.FAULT_AGAIN)
  }

  @Test
  fun shouldReturnResultWhenResultIsNotTheSpecifiedHandledResult() {
    val policy = Policy.retry<TestResult>()
      .handleResult(TestResult.FAULT)
      .retry()

    val result = policy.raiseResults(TestResult.FAULT_AGAIN, TestResult.GOOD)

    assertThat(result).isEqualTo(TestResult.FAULT_AGAIN)
  }

  @Test
  fun shouldReturnResultWhenResultIsNotOneOfTheSpecifiedHandledResults() {
    val policy = Policy.retry<TestResult>()
      .handleResult(TestResult.FAULT)
      .handleResult(TestResult.FAULT_AGAIN)
      .retry()

    val result = policy.raiseResults(TestResult.FAULT_YET_AGAIN, TestResult.GOOD)

    assertThat(result).isEqualTo(TestResult.FAULT_YET_AGAIN)
  }

  @Test
  fun shouldReturnResultWhenSpecifiedResultPredicateIsNotSatisfied() {
    val policy = Policy.retry<TestResultClass>()
      .handleResult { it.resultCode == TestResult.FAULT }
      .retry()

    val result = policy.raiseResults(
      TestResultClass(TestResult.FAULT_AGAIN),
      TestResultClass(TestResult.GOOD)
    )

    assertThat(result.resultCode).isEqualTo(TestResult.FAULT_AGAIN)
  }

  @Test
  fun shouldReturnResultWhenNoneOfTheSpecifiedResultPredicatesAreSatisfied() {
    val policy = Policy.retry<TestResultClass>()
      .handleResult { it.resultCode == TestResult.FAULT_AGAIN }
      .retry()

    val result = policy.raiseResults(
      TestResultClass(TestResult.FAULT_YET_AGAIN),
      TestResultClass(TestResult.GOOD)
    )

    assertThat(result.resultCode).isEqualTo(TestResult.FAULT_YET_AGAIN)
  }

  @Test
  fun shouldNotReturnHandledResultWhenSpecifiedResultPredicateIsSatisfied() {
    val policy = Policy.retry<TestResultClass>()
      .handleResult { it.resultCode == TestResult.FAULT }
      .retry()

    val result = policy.raiseResults(
      TestResultClass(TestResult.FAULT),
      TestResultClass(TestResult.GOOD)
    )

    assertThat(result.resultCode).isEqualTo(TestResult.GOOD)
  }

  @Test
  fun shouldNotReturnHandledResultWhenOneOfTheSpecifiedResultPredicatesIsSatisfied() {
    val policy = Policy.retry<TestResultClass>()
      .handleResult { it.resultCode == TestResult.FAULT }
      .handleResult { it.resultCode == TestResult.FAULT_AGAIN }
      .retry()

    val result = policy.raiseResults(
      TestResultClass(TestResult.FAULT_AGAIN),
      TestResultClass(TestResult.GOOD)
    )

    assertThat(result.resultCode).isEqualTo(TestResult.GOOD)
  }

  @Test
  fun shouldCallOnRetryOnEachRetryWithTheCurrentRetryCount() {
    val expectedRetryCounts = listOf(1, 2, 3)
    val retryCounts = mutableListOf<Int>()
    val policy = Policy.retry<TestResult>()
      .handleResult(TestResult.FAULT)
      .retry(3) { _, retryCount, _ -> retryCounts.add(retryCount) }

    policy.raiseResults(TestResult.FAULT, TestResult.FAULT, TestResult.FAULT, TestResult.GOOD)

    assertThat(retryCounts).containsExactlyElementsOf(expectedRetryCounts)
  }

  @Test
  fun shouldCallOnRetryOnEachRetryWithTheCurrentHandledResult() {
    val expectedFaults = listOf("Fault #1", "Fault #2", "Fault #3")
    val retryFaults = mutableListOf<String?>()
    val policy = Policy.retry<TestResultClass>()
      .handleResult { it.resultCode == TestResult.FAULT }
      .retry(3) { outcome, _, _ ->
        outcome.onSuccess { retryFaults.add(it.someString) }
      }
    val resultsToRaise = expectedFaults.map { TestResultClass(TestResult.FAULT, it) }
      .toMutableList()
    resultsToRaise.add(TestResultClass(TestResult.FAULT))

    policy.raiseResults(*resultsToRaise.toTypedArray())

    assertThat(retryFaults).containsExactlyElementsOf(expectedFaults)
  }
}
