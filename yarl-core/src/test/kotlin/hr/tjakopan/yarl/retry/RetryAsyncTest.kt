package hr.tjakopan.yarl.retry

import hr.tjakopan.yarl.Policy
import hr.tjakopan.yarl.test.helpers.TestResult
import hr.tjakopan.yarl.test.helpers.TestResultClass
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

  @Test
  fun shouldReturnHandledResultWhenOneOfTheHandledResultsIsRaisedMoreTimesThanRetryCount() = runBlockingTest {
    val policy = Policy.asyncRetry<TestResult>()
      .handleResult(TestResult.FAULT)
      .handleResult(TestResult.FAULT_AGAIN)
      .retry(3)

    val result = policy.raiseResults(
      TestResult.FAULT_AGAIN, TestResult.FAULT_AGAIN, TestResult.FAULT_AGAIN, TestResult.FAULT_AGAIN, TestResult.GOOD
    )

    assertThat(result).isEqualTo(TestResult.FAULT_AGAIN)
  }

  @Test
  fun shouldReturnResultWhenResultIsNotTheSpecifiedHandledResult() = runBlockingTest {
    val policy = Policy.asyncRetry<TestResult>()
      .handleResult(TestResult.FAULT)
      .retry()

    val result = policy.raiseResults(TestResult.FAULT_AGAIN, TestResult.GOOD)

    assertThat(result).isEqualTo(TestResult.FAULT_AGAIN)
  }

  @Test
  fun shouldReturnResultWhenResultIsNotOneOfTheSpecifiedHandledResults() = runBlockingTest {
    val policy = Policy.asyncRetry<TestResult>()
      .handleResult(TestResult.FAULT)
      .handleResult(TestResult.FAULT_AGAIN)
      .retry()

    val result = policy.raiseResults(TestResult.FAULT_YET_AGAIN, TestResult.GOOD)

    assertThat(result).isEqualTo(TestResult.FAULT_YET_AGAIN)
  }

  @Test
  fun shouldReturnResultWhenSpecifiedResultPredicateIsNotSatisfied() = runBlockingTest {
    val policy = Policy.asyncRetry<TestResultClass>()
      .handleResult { r -> r.resultCode == TestResult.FAULT }
      .retry()

    val result = policy.raiseResults(TestResultClass(TestResult.FAULT_AGAIN), TestResultClass(TestResult.GOOD))

    assertThat(result.resultCode).isEqualTo(TestResult.FAULT_AGAIN)
  }

  @Test
  fun shouldReturnResultWhenNoneOfTheSpecifiedResultPredicatesAreSatisfied() = runBlockingTest {
    val policy = Policy.asyncRetry<TestResultClass>()
      .handleResult { r -> r.resultCode == TestResult.FAULT }
      .handleResult { r -> r.resultCode == TestResult.FAULT_AGAIN }
      .retry()

    val result = policy.raiseResults(TestResultClass(TestResult.FAULT_YET_AGAIN), TestResultClass(TestResult.GOOD))

    assertThat(result.resultCode).isEqualTo(TestResult.FAULT_YET_AGAIN)
  }

  @Test
  fun shouldNotReturnHandledResultWhenSpecifiedResultPredicateIsSatisfied() = runBlockingTest {
    val policy = Policy.asyncRetry<TestResultClass>()
      .handleResult { r -> r.resultCode == TestResult.FAULT }
      .retry()

    val result = policy.raiseResults(TestResultClass(TestResult.FAULT), TestResultClass(TestResult.GOOD))

    assertThat(result.resultCode).isEqualTo(TestResult.GOOD)
  }

  @Test
  fun shouldNotReturnHandledResultWhenOneOfTheSpecifiedResultPredicatesIsSatisfied() = runBlockingTest {
    val policy = Policy.asyncRetry<TestResultClass>()
      .handleResult { r -> r.resultCode == TestResult.FAULT }
      .handleResult { r -> r.resultCode == TestResult.FAULT_AGAIN }
      .retry()

    val result = policy.raiseResults(TestResultClass(TestResult.FAULT_AGAIN), TestResultClass(TestResult.GOOD))

    assertThat(result.resultCode).isEqualTo(TestResult.GOOD)
  }

  @Test
  fun shouldCallOnRetryOnEachRetryWithTheCurrentRetryCount() = runBlockingTest {
    val expectedRetryCounts = listOf(1, 2, 3)
    val retryCounts = mutableListOf<Int>()
    val policy = Policy.asyncRetry<TestResult>()
      .handleResult(TestResult.FAULT)
      .retry(3) { _, retryCount, _ -> retryCounts.add(retryCount) }

    val result = policy.raiseResults(TestResult.FAULT, TestResult.FAULT, TestResult.FAULT, TestResult.GOOD)

    assertThat(result).isEqualTo(TestResult.GOOD)
    assertThat(retryCounts).containsExactlyElementsOf(expectedRetryCounts)
  }

  @Test
  fun shouldCallOnRetryOnEachRetryWithTheCurrentHandledResult() = runBlockingTest {
    val expectedFaults = listOf("Fault #1", "Fault #2", "Fault #3")
    val retryFaults = mutableListOf<String?>()
    val policy = Policy.asyncRetry<TestResultClass>()
      .handleResult { r -> r.resultCode == TestResult.FAULT }
      .retry(3) { outcome, _, _ ->
        retryFaults.add(outcome.getOrNull()?.someString)
//        outcome.onSuccess { r -> retryFaults.add(r.someString) }
        println(outcome)
        Unit
      }
    val resultsToRaise = expectedFaults.map { s -> TestResultClass(TestResult.FAULT, s) }.toMutableList()
    resultsToRaise.add(TestResultClass(TestResult.FAULT))

    val result = policy.raiseResults(*resultsToRaise.toTypedArray())

    assertThat(result).isEqualTo(TestResult.FAULT)
    assertThat(retryFaults).containsExactlyElementsOf(expectedFaults)
  }
}
