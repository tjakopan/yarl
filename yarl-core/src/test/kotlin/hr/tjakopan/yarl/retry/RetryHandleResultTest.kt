package hr.tjakopan.yarl.retry

import hr.tjakopan.yarl.Context
import hr.tjakopan.yarl.Policy
import hr.tjakopan.yarl.PolicyResult
import hr.tjakopan.yarl.test.helpers.TestResult
import hr.tjakopan.yarl.test.helpers.TestResultClass
import hr.tjakopan.yarl.test.helpers.raiseResults
import hr.tjakopan.yarl.test.helpers.raiseResultsOnExecuteAndCapture
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import kotlin.test.Test

@Suppress("UsePropertyAccessSyntax")
class RetryHandleResultTest {
  @Test
  fun `should throw when retry count is less than zero`() {
    assertThatThrownBy {
      Policy.retry<TestResult>()
        .handleResult(TestResult.FAULT)
        .retry(-1)
    }
      .isInstanceOf(IllegalArgumentException::class.java)
      .hasMessageContaining("Retry count")
  }

  @Test
  fun `should not return handled result when handled result raised same number of times as retry count`() {
    val policy = Policy.retry<TestResult>()
      .handleResult(TestResult.FAULT)
      .retry(3)

    val result = policy.raiseResults(TestResult.FAULT, TestResult.FAULT, TestResult.FAULT, TestResult.GOOD)

    assertThat(result).isEqualTo(TestResult.GOOD)
  }

  @Test
  fun `should not return handled result when one of the handled results raised same number of times as retry count`() {
    val policy = Policy.retry<TestResult>()
      .handleResult(TestResult.FAULT)
      .handleResult(TestResult.FAULT_AGAIN)
      .retry(3)

    val result = policy.raiseResults(TestResult.FAULT, TestResult.FAULT_AGAIN, TestResult.FAULT, TestResult.GOOD)

    assertThat(result).isEqualTo(TestResult.GOOD)
  }

  @Test
  fun `should not return handled result when handled result raised less number of times than retry count`() {
    val policy = Policy.retry<TestResult>()
      .handleResult(TestResult.FAULT)
      .retry(3)

    val result = policy.raiseResults(TestResult.FAULT, TestResult.GOOD)

    assertThat(result).isEqualTo(TestResult.GOOD)
  }

  @Test
  fun `should not return handled result when all of the handled results raised less number of times than retry count`() {
    val policy = Policy.retry<TestResult>()
      .handleResult(TestResult.FAULT)
      .handleResult(TestResult.FAULT_AGAIN)
      .retry(3)

    val result = policy.raiseResults(TestResult.FAULT, TestResult.FAULT_AGAIN, TestResult.GOOD)

    assertThat(result).isEqualTo(TestResult.GOOD)
  }

  @Test
  fun `should return handled result when handled result raised more times then retry count`() {
    val policy = Policy.retry<TestResult>()
      .handleResult(TestResult.FAULT)
      .retry(3)

    val result =
      policy.raiseResults(TestResult.FAULT, TestResult.FAULT, TestResult.FAULT, TestResult.FAULT, TestResult.GOOD)

    assertThat(result).isEqualTo(TestResult.FAULT)
  }

  @Test
  fun `should return handled result when one of the handled results is raised more times then retry count`() {
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
  fun `should return result when result is not the specified handled result`() {
    val policy = Policy.retry<TestResult>()
      .handleResult(TestResult.FAULT)
      .retry()

    val result = policy.raiseResults(TestResult.FAULT_AGAIN, TestResult.GOOD)

    assertThat(result).isEqualTo(TestResult.FAULT_AGAIN)
  }

  @Test
  fun `should return result when result is not one of the specified handled results`() {
    val policy = Policy.retry<TestResult>()
      .handleResult(TestResult.FAULT)
      .handleResult(TestResult.FAULT_AGAIN)
      .retry()

    val result = policy.raiseResults(TestResult.FAULT_YET_AGAIN, TestResult.GOOD)

    assertThat(result).isEqualTo(TestResult.FAULT_YET_AGAIN)
  }

  @Test
  fun `should return result when specified result predicate is not satisfied`() {
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
  fun `should return result when none of the specified result predicates are satisfied`() {
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
  fun `should not return handled result when specified result predicate is satisfied`() {
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
  fun `should not return handled result when one of the specified result predicates is satisfied`() {
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
  fun `should call on retry on each retry with the current retry count`() {
    val expectedRetryCounts = listOf(1, 2, 3)
    val retryCounts = mutableListOf<Int>()
    val policy = Policy.retry<TestResult>()
      .handleResult(TestResult.FAULT)
      .retry(3) { _, retryCount, _ -> retryCounts.add(retryCount) }

    policy.raiseResults(TestResult.FAULT, TestResult.FAULT, TestResult.FAULT, TestResult.GOOD)

    assertThat(retryCounts).containsExactlyElementsOf(expectedRetryCounts)
  }

  @Test
  fun `should call on retry on each retry with the current handled result`() {
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

  @Test
  fun `should not call on retry when no retries are performed`() {
    var retryCalled = false
    val policy = Policy.retry<TestResult>()
      .handleResult(TestResult.FAULT)
      .retry { _, _, _ -> retryCalled = true }

    policy.raiseResults(TestResult.GOOD)

    assertThat(retryCalled).isFalse()
  }

  @Test
  fun `should call on retry with the passed context`() {
    var capturedContext: Context? = null
    val policy = Policy.retry<TestResult>()
      .handleResult(TestResult.FAULT)
      .retry { _, _, context -> capturedContext = context }
    val context = Context(mapOf("key1" to "value1", "key2" to "value2"))

    val result = policy.raiseResults(context, TestResult.FAULT, TestResult.GOOD)

    assertThat(result).isEqualTo(TestResult.GOOD)
    assertThat(capturedContext).containsKeys("key1", "key2")
      .containsValues("value1", "value2")
  }

  @Test
  fun `should call on retry with the passed context when execute and capture`() {
    var capturedContext: Context? = null
    val policy = Policy.retry<TestResult>()
      .handleResult(TestResult.FAULT)
      .retry { _, _, context -> capturedContext = context }
    val context = Context(mapOf("key1" to "value1", "key2" to "value2"))

    val result = policy.raiseResultsOnExecuteAndCapture(context, TestResult.FAULT, TestResult.GOOD)

    assertThat(result.isSuccess).isTrue()
    assertThat((result as PolicyResult.Success).result).isEqualTo(TestResult.GOOD)
    assertThat(capturedContext).containsKeys("key1", "key2")
      .containsValues("value1", "value2")
  }

  @Test
  fun `context should be empty if execute not called with any data`() {
    var capturedContext: Context? = null
    val policy = Policy.retry<TestResult>()
      .handleResult(TestResult.FAULT)
      .retry { _, _, context -> capturedContext = context }

    policy.raiseResults(TestResult.FAULT, TestResult.GOOD)

    assertThat(capturedContext).isEmpty()
  }

  @Test
  fun `should create new context for each call to execute`() {
    var contextValue: String? = null
    val policy = Policy.retry<TestResult>()
      .handleResult(TestResult.FAULT)
      .retry { _, _, context -> contextValue = context["key"].toString() }
    val context1 = Context(mapOf("key" to "original_value"))
    val context2 = Context(mapOf("key" to "new_value"))

    policy.raiseResults(context1, TestResult.FAULT, TestResult.GOOD)

    assertThat(contextValue).isEqualTo("original_value")

    policy.raiseResults(context2, TestResult.FAULT, TestResult.GOOD)

    assertThat(contextValue).isEqualTo("new_value")
  }

  @Test
  fun `should create new context for each call to execute and capture`() {
    var contextValue: String? = null
    val policy = Policy.retry<TestResult>()
      .handleResult(TestResult.FAULT)
      .retry { _, _, context -> contextValue = context["key"].toString() }
    val context1 = Context(mapOf("key" to "original_value"))
    val context2 = Context(mapOf("key" to "new_value"))

    policy.raiseResultsOnExecuteAndCapture(context1, TestResult.FAULT, TestResult.GOOD)

    assertThat(contextValue).isEqualTo("original_value")

    policy.raiseResultsOnExecuteAndCapture(context2, TestResult.FAULT, TestResult.GOOD)

    assertThat(contextValue).isEqualTo("new_value")
  }

  @Test
  fun `should create new state for each call to policy`() {
    val policy = Policy.retry<TestResult>()
      .handleResult(TestResult.FAULT)
      .retry(1)

    val result1 = policy.raiseResults(TestResult.FAULT, TestResult.GOOD)
    val result2 = policy.raiseResults(TestResult.FAULT, TestResult.GOOD)

    assertThat(result1).isEqualTo(TestResult.GOOD)
    assertThat(result2).isEqualTo(TestResult.GOOD)
  }

  @Test
  fun `should not call on retry when retry count is zero`() {
    var retryInvoked = false
    val policy = Policy.retry<TestResult>()
      .handleResult(TestResult.FAULT)
      .retry(0) { _, _, _ -> retryInvoked = true }

    val result = policy.raiseResults(TestResult.FAULT, TestResult.GOOD)

    assertThat(result).isEqualTo(TestResult.FAULT)
    assertThat(retryInvoked).isFalse()
  }
}
