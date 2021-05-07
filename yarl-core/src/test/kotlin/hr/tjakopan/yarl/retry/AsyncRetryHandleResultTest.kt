package hr.tjakopan.yarl.retry

import hr.tjakopan.yarl.Context
import hr.tjakopan.yarl.Policy
import hr.tjakopan.yarl.PolicyResult
import hr.tjakopan.yarl.test.helpers.*
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.runBlockingTest
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import java.time.Duration
import kotlin.test.Test
import kotlin.test.assertFailsWith

@Suppress("UsePropertyAccessSyntax")
@ExperimentalCoroutinesApi
class AsyncRetryHandleResultTest {
  @Test
  fun `should throw when retry count is less than zero`() {
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
  fun `should not return handled result when handled result raised same number of times as retry count`() =
    runBlockingTest {
      val policy = Policy.asyncRetry<TestResult>()
        .handleResult(TestResult.FAULT)
        .retry(3)

      val result = policy.raiseResults(TestResult.FAULT, TestResult.FAULT, TestResult.FAULT, TestResult.GOOD)

      assertThat(result).isEqualTo(TestResult.GOOD)
    }

  @Test
  fun `should not return handled result when one of the handled results raised same number of times as retry count`() =
    runBlockingTest {
      val policy = Policy.asyncRetry<TestResult>()
        .handleResult(TestResult.FAULT)
        .handleResult(TestResult.FAULT_AGAIN)
        .retry(3)

      val result = policy.raiseResults(TestResult.FAULT, TestResult.FAULT_AGAIN, TestResult.FAULT, TestResult.GOOD)

      assertThat(result).isEqualTo(TestResult.GOOD)
    }

  @Test
  fun `should not return handled result when handled result raised less number of times than retry count`() =
    runBlockingTest {
      val policy = Policy.asyncRetry<TestResult>()
        .handleResult(TestResult.FAULT)
        .retry(3)

      val result = policy.raiseResults(TestResult.FAULT, TestResult.GOOD)

      assertThat(result).isEqualTo(TestResult.GOOD)
    }

  @Test
  fun `should not return handled result when all of the handled results raised less number of times than retry count`() =
    runBlockingTest {
      val policy = Policy.asyncRetry<TestResult>()
        .handleResult(TestResult.FAULT)
        .handleResult(TestResult.FAULT_AGAIN)
        .retry(3)

      val result = policy.raiseResults(TestResult.FAULT, TestResult.FAULT_AGAIN, TestResult.GOOD)

      assertThat(result).isEqualTo(TestResult.GOOD)
    }

  @Test
  fun `should return handled result when handled result raised more times than retry count`() = runBlockingTest {
    val policy = Policy.asyncRetry<TestResult>()
      .handleResult(TestResult.FAULT)
      .retry(3)

    val result =
      policy.raiseResults(TestResult.FAULT, TestResult.FAULT, TestResult.FAULT, TestResult.FAULT, TestResult.GOOD)

    assertThat(result).isEqualTo(TestResult.FAULT)
  }

  @Test
  fun `should return handled result when one of the handled results is raised more times than retry count`() =
    runBlockingTest {
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
  fun `should return result when result is not the specified handled result`() = runBlockingTest {
    val policy = Policy.asyncRetry<TestResult>()
      .handleResult(TestResult.FAULT)
      .retry()

    val result = policy.raiseResults(TestResult.FAULT_AGAIN, TestResult.GOOD)

    assertThat(result).isEqualTo(TestResult.FAULT_AGAIN)
  }

  @Test
  fun `should return result when result is not one of the specified handled results`() = runBlockingTest {
    val policy = Policy.asyncRetry<TestResult>()
      .handleResult(TestResult.FAULT)
      .handleResult(TestResult.FAULT_AGAIN)
      .retry()

    val result = policy.raiseResults(TestResult.FAULT_YET_AGAIN, TestResult.GOOD)

    assertThat(result).isEqualTo(TestResult.FAULT_YET_AGAIN)
  }

  @Test
  fun `should return result when specified result predicate is not satisfied`() = runBlockingTest {
    val policy = Policy.asyncRetry<TestResultClass>()
      .handleResult { r -> r.resultCode == TestResult.FAULT }
      .retry()

    val result = policy.raiseResults(TestResultClass(TestResult.FAULT_AGAIN), TestResultClass(TestResult.GOOD))

    assertThat(result.resultCode).isEqualTo(TestResult.FAULT_AGAIN)
  }

  @Test
  fun `should return result when none of the specified result predicates are satisfied`() = runBlockingTest {
    val policy = Policy.asyncRetry<TestResultClass>()
      .handleResult { r -> r.resultCode == TestResult.FAULT }
      .handleResult { r -> r.resultCode == TestResult.FAULT_AGAIN }
      .retry()

    val result = policy.raiseResults(TestResultClass(TestResult.FAULT_YET_AGAIN), TestResultClass(TestResult.GOOD))

    assertThat(result.resultCode).isEqualTo(TestResult.FAULT_YET_AGAIN)
  }

  @Test
  fun `should not return handled result when specified result predicate is satisfied`() = runBlockingTest {
    val policy = Policy.asyncRetry<TestResultClass>()
      .handleResult { r -> r.resultCode == TestResult.FAULT }
      .retry()

    val result = policy.raiseResults(TestResultClass(TestResult.FAULT), TestResultClass(TestResult.GOOD))

    assertThat(result.resultCode).isEqualTo(TestResult.GOOD)
  }

  @Test
  fun `should not return handled result when one of the specified result predicates is satisfied`() = runBlockingTest {
    val policy = Policy.asyncRetry<TestResultClass>()
      .handleResult { r -> r.resultCode == TestResult.FAULT }
      .handleResult { r -> r.resultCode == TestResult.FAULT_AGAIN }
      .retry()

    val result = policy.raiseResults(TestResultClass(TestResult.FAULT_AGAIN), TestResultClass(TestResult.GOOD))

    assertThat(result.resultCode).isEqualTo(TestResult.GOOD)
  }

  @Test
  fun `should call on retry on each retry with the current retry count`() = runBlockingTest {
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
  fun `should call on retry on each retry with the current handled result`() = runBlockingTest {
    val expectedFaults = listOf("Fault #1", "Fault #2", "Fault #3")
    val retryFaults = mutableListOf<String?>()
    val policy = Policy.asyncRetry<TestResultClass>()
      .handleResult { r -> r.resultCode == TestResult.FAULT }
      .retry(3) { outcome, _, _ ->
        outcome.onSuccess { r -> retryFaults.add(r.someString) }
      }
    val resultsToRaise = expectedFaults.map { s -> TestResultClass(TestResult.FAULT, s) }
      .toMutableList()
    resultsToRaise.add(TestResultClass(TestResult.FAULT))

    val result = policy.raiseResults(*resultsToRaise.toTypedArray())

    assertThat(result.resultCode).isEqualTo(TestResult.FAULT)
    assertThat(retryFaults).containsExactlyElementsOf(expectedFaults)
  }

  @Test
  fun `should not call on retry when no retries are performed`() = runBlockingTest {
    var retryCalled = false
    val policy = Policy.asyncRetry<TestResult>()
      .handleResult(TestResult.FAULT)
      .retry { _, _, _ -> retryCalled = true }

    val result = policy.raiseResults(TestResult.GOOD)

    assertThat(result).isEqualTo(TestResult.GOOD)
    assertThat(retryCalled).isFalse()
  }

  @Test
  fun `should call on retry with the passed context`() = runBlockingTest {
    var context: Context? = null
    val policy = Policy.asyncRetry<TestResult>()
      .handleResult(TestResult.FAULT)
      .retry { _, _, ctx -> context = ctx }

    val result = policy.raiseResults(
      Context(mapOf("key1" to "value1", "key2" to "value2")),
      TestResult.FAULT,
      TestResult.GOOD
    )

    assertThat(result).isEqualTo(TestResult.GOOD)
    assertThat(context).isNotNull()
    assertThat(context).containsKeys("key1", "key2")
      .containsValues("value1", "value2")
  }

  @Test
  fun `should call on retry with the passed context when execute and capture`() = runBlockingTest {
    var context: Context? = null
    val policy = Policy.asyncRetry<TestResult>()
      .handleResult(TestResult.FAULT)
      .retry { _, _, ctx -> context = ctx }

    val result = policy.raiseResultsOnExecuteAndCapture(
      Context(mapOf("key1" to "value1", "key2" to "value2")),
      TestResult.FAULT,
      TestResult.GOOD
    )

    assertThat(result.isSuccess).isTrue()
    assertThat((result as PolicyResult.Success).result).isEqualTo(TestResult.GOOD)
    assertThat(context).isNotNull()
    assertThat(context).containsKeys("key1", "key2")
      .containsValues("value1", "value2")
  }

  @Test
  fun `context should be empty if execute not called with any data`() = runBlockingTest {
    var capturedContext: Context? = null
    val policy = Policy.asyncRetry<TestResult>()
      .handleResult(TestResult.FAULT)
      .retry { _, _, ctx -> capturedContext = ctx }

    policy.raiseResults(TestResult.FAULT, TestResult.GOOD)

    assertThat(capturedContext).isEmpty()
  }

  @Test
  fun `should create new context for each call to execute`() = runBlockingTest {
    var contextValue: String? = null
    val policy = Policy.asyncRetry<TestResult>()
      .handleResult(TestResult.FAULT)
      .retry { _, _, ctx -> contextValue = ctx["key"].toString() }

    policy.raiseResults(
      Context(mapOf("key" to "original_value")),
      TestResult.FAULT,
      TestResult.GOOD
    )

    assertThat(contextValue).isEqualTo("original_value")

    policy.raiseResults(Context(mapOf("key" to "new_value")), TestResult.FAULT, TestResult.GOOD)

    assertThat(contextValue).isEqualTo("new_value")
  }

  @Test
  fun `should create new context for each call to execute and capture`() = runBlockingTest {
    var contextValue: String? = null
    val policy = Policy.asyncRetry<TestResult>()
      .handleResult(TestResult.FAULT)
      .retry { _, _, ctx -> contextValue = ctx["key"].toString() }

    policy.raiseResultsOnExecuteAndCapture(
      Context(mapOf("key" to "original_value")),
      TestResult.FAULT,
      TestResult.GOOD
    )

    assertThat(contextValue).isEqualTo("original_value")

    policy.raiseResultsOnExecuteAndCapture(
      Context(mapOf("key" to "new_value")),
      TestResult.FAULT,
      TestResult.GOOD
    )

    assertThat(contextValue).isEqualTo("new_value")
  }

  @Test
  fun `should create new state for each call to policy`() = runBlockingTest {
    val policy = Policy.asyncRetry<TestResult>()
      .handleResult(TestResult.FAULT)
      .retry(1)

    val result1 = policy.raiseResults(TestResult.FAULT, TestResult.GOOD)

    assertThat(result1).isEqualTo(TestResult.GOOD)

    val result2 = policy.raiseResults(TestResult.FAULT, TestResult.GOOD)

    assertThat(result2).isEqualTo(TestResult.GOOD)
  }

  @Test
  fun `should not call on retry when retry count is zero`() = runBlockingTest {
    var retryInvoked = false
    val policy = Policy.asyncRetry<TestResult>()
      .handleResult(TestResult.FAULT)
      .retry(0) { _, _, _ -> retryInvoked = true }

    val result = policy.raiseResults(TestResult.FAULT, TestResult.GOOD)

    assertThat(result).isEqualTo(TestResult.FAULT)
    assertThat(retryInvoked).isFalse()
  }

  @Test
  fun `should wait asynchronously for async on retry delegate`() = runBlockingTest {
    val duration = Duration.ofMillis(200)
    var executeDelegateInvocations = 0
    var executeDelegateInvocationsWhenOnRetryExits = 0
    val policy = Policy.asyncRetry<TestResult>()
      .handleResult(TestResult.FAULT)
      .retry { _, _, _ ->
        delay(duration.toMillis())
        executeDelegateInvocationsWhenOnRetryExits = executeDelegateInvocations
      }

    val result = policy.execute {
      executeDelegateInvocations++
      TestResult.FAULT
    }

    assertThat(result).isEqualTo(TestResult.FAULT)

    assertThat(executeDelegateInvocationsWhenOnRetryExits).isEqualTo(1)
    assertThat(executeDelegateInvocations).isEqualTo(2)
  }

  @Test
  fun `should execute all tries when faulting and not cancelled`() = runBlockingTest {
    val policy = Policy.asyncRetry<TestResult>()
      .handleResult(TestResult.FAULT)
      .retry(3)
    var attemptsInvoked = 0
    val onExecute: () -> Unit = { attemptsInvoked++ }

    val result = policy.raiseResults(
      onExecute,
      TestResult.FAULT,
      TestResult.FAULT,
      TestResult.FAULT,
      TestResult.GOOD
    )

    assertThat(result).isEqualTo(TestResult.GOOD)
    assertThat(attemptsInvoked).isEqualTo(1 + 3)
  }

  @Test
  fun `should not execute action when cancelled before execute`() {
    assertFailsWith(CancellationException::class) {
      runBlockingTest {
        val policy = Policy.asyncRetry<TestResult>()
          .handleResult(TestResult.FAULT)
          .retry(3)
        var attemptsInvoked = 0
        val onExecute: () -> Unit = { attemptsInvoked++ }

        coroutineContext.cancel()

        assertFailsWith(CancellationException::class) {
          policy.raiseResults(
            onExecute,
            TestResult.FAULT,
            TestResult.FAULT,
            TestResult.FAULT,
            TestResult.GOOD
          )
        }
        assertThat(attemptsInvoked).isEqualTo(0)
      }
    }
  }

  @Test
  fun `should report cancellation during otherwise non faulting action execution and cancel further retries`() =
    runBlockingTest {
      val policy = Policy.asyncRetry<TestResult>()
        .handleResult(TestResult.FAULT)
        .retry(3)
      var attemptsInvoked = 0
      val onExecute: () -> Unit = { attemptsInvoked++ }

      assertFailsWith(CancellationException::class) {
        policy.raiseResultsAndOrCancellation(
          1,
          onExecute,
          TestResult.GOOD,
          TestResult.GOOD,
          TestResult.GOOD,
          TestResult.GOOD
        )
      }
      assertThat(attemptsInvoked).isEqualTo(1)
    }

  @Test
  fun `should report cancellation during faulting initial action execution and cancel further retries`() =
    runBlockingTest {
      val policy = Policy.asyncRetry<TestResult>()
        .handleResult(TestResult.FAULT)
        .retry(3)
      var attemptsInvoked = 0
      val onExecute: () -> Unit = { attemptsInvoked++ }

      assertFailsWith(CancellationException::class) {
        policy.raiseResultsAndOrCancellation(
          1,
          onExecute,
          TestResult.FAULT,
          TestResult.FAULT,
          TestResult.FAULT,
          TestResult.GOOD
        )
      }
      assertThat(attemptsInvoked).isEqualTo(1)
    }

  @Test
  fun `should report cancellation during faulting retried action execution and cancel further retries`() =
    runBlockingTest {
      val policy = Policy.asyncRetry<TestResult>()
        .handleResult(TestResult.FAULT)
        .retry(3)
      var attemptsInvoked = 0
      val onExecute: () -> Unit = { attemptsInvoked++ }

      assertFailsWith(CancellationException::class) {
        policy.raiseResultsAndOrCancellation(
          2,
          onExecute,
          TestResult.FAULT,
          TestResult.FAULT,
          TestResult.FAULT,
          TestResult.GOOD
        )
      }
      assertThat(attemptsInvoked).isEqualTo(2)
    }

  @Test
  fun `should report cancellation during faulting last retry execution`() = runBlockingTest {
    val policy = Policy.asyncRetry<TestResult>()
      .handleResult(TestResult.FAULT)
      .retry(3)
    var attemptsInvoked = 0
    val onExecute: () -> Unit = { attemptsInvoked++ }

    assertFailsWith(CancellationException::class) {
      policy.raiseResultsAndOrCancellation(
        1 + 3,
        onExecute,
        TestResult.FAULT,
        TestResult.FAULT,
        TestResult.FAULT,
        TestResult.GOOD
      )
    }
    assertThat(attemptsInvoked).isEqualTo(1 + 3)
  }

  @Test
  fun `should report cancellation after faulting action execution and cancel further retries if on retry invokes cancellation`() {
    assertFailsWith(CancellationException::class) {
      runBlockingTest {
        val policy = Policy.asyncRetry<TestResult>()
          .handleResult(TestResult.FAULT)
          .retry(3) { _, _, _ -> coroutineContext.cancel() }
        var attemptsInvoked = 0
        val onExecute: () -> Unit = { attemptsInvoked++ }

        assertFailsWith(CancellationException::class) {
          policy.raiseResults(onExecute, TestResult.FAULT, TestResult.FAULT, TestResult.FAULT, TestResult.GOOD)
        }
        assertThat(attemptsInvoked).isEqualTo(1)
      }
    }
  }
}
