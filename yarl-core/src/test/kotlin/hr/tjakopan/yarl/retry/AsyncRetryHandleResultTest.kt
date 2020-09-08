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

@ExperimentalCoroutinesApi
class AsyncRetryHandleResultTest {
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
  fun shouldNotCallOnRetryWhenNoRetriesArePerformed() = runBlockingTest {
    var retryCalled = false
    val policy = Policy.asyncRetry<TestResult>()
      .handleResult(TestResult.FAULT)
      .retry { _, _, _ -> retryCalled = true }

    val result = policy.raiseResults(TestResult.GOOD)

    assertThat(result).isEqualTo(TestResult.GOOD)
    assertThat(retryCalled).isFalse();
  }

  @Test
  fun shouldCallOnRetryWithThePassedContext() = runBlockingTest {
    var context: Context? = null
    val policy = Policy.asyncRetry<TestResult>()
      .handleResult(TestResult.FAULT)
      .retry { _, _, ctx -> context = ctx }

    val result = policy.raiseResults(
      Context(contextData = mutableMapOf("key1" to "value1", "key2" to "value2")),
      TestResult.FAULT,
      TestResult.GOOD
    )

    assertThat(result).isEqualTo(TestResult.GOOD)
    @Suppress("UsePropertyAccessSyntax")
    assertThat(context).isNotNull()
    assertThat(context?.contextData).containsKeys("key1", "key2")
      .containsValues("value1", "value2")
  }

  @Test
  fun shouldCallOnRetryWithThePassedContextWhenExecuteAndCapture() = runBlockingTest {
    var context: Context? = null
    val policy = Policy.asyncRetry<TestResult>()
      .handleResult(TestResult.FAULT)
      .retry { _, _, ctx -> context = ctx }

    val result = policy.raiseResultsOnExecuteAndCapture(
      Context(contextData = mutableMapOf("key1" to "value1", "key2" to "value2")),
      TestResult.FAULT,
      TestResult.GOOD
    )

    assertThat(result.isSuccess).isTrue()
    assertThat((result as PolicyResult.Success).result).isEqualTo(TestResult.GOOD)
    @Suppress("UsePropertyAccessSyntax")
    assertThat(context).isNotNull()
    assertThat(context?.contextData).containsKeys("key1", "key2")
      .containsValues("value1", "value2")
  }

  @Test
  fun contextShouldBeEmptyIfExecuteNotCalledWithContext() = runBlockingTest {
    var capturedContext: Context? = null
    val policy = Policy.asyncRetry<TestResult>()
      .handleResult(TestResult.FAULT)
      .retry { _, _, ctx -> capturedContext = ctx }

    policy.raiseResults(TestResult.FAULT, TestResult.GOOD)

    assertThat(capturedContext).isNotNull
    assertThat(capturedContext?.policyWrapKey).isNull()
    assertThat(capturedContext?.policyKey).isNotNull()
    assertThat(capturedContext?.operationKey).isNull()
    assertThat(capturedContext?.contextData).isEmpty()
  }

  @Test
  fun shouldCreateNewContextForEachCallToExecute() = runBlockingTest {
    var contextValue: String? = null
    val policy = Policy.asyncRetry<TestResult>()
      .handleResult(TestResult.FAULT)
      .retry { _, _, ctx -> contextValue = ctx.contextData["key"].toString() }

    policy.raiseResults(Context(contextData = mutableMapOf("key" to "original_value")), TestResult.FAULT, TestResult.GOOD)

    assertThat(contextValue).isEqualTo("original_value")

    policy.raiseResults(Context(contextData = mutableMapOf("key" to "new_value")), TestResult.FAULT, TestResult.GOOD)

    assertThat(contextValue).isEqualTo("new_value")
  }

  @Test
  fun shouldCreateNewContextForEachCallToExecuteAndCapture() = runBlockingTest {
    var contextValue: String? = null
    val policy = Policy.asyncRetry<TestResult>()
      .handleResult(TestResult.FAULT)
      .retry { _, _, ctx -> contextValue = ctx.contextData["key"].toString() }

    policy.raiseResultsOnExecuteAndCapture(
      Context(contextData = mutableMapOf("key" to "original_value")),
      TestResult.FAULT,
      TestResult.GOOD
    )

    assertThat(contextValue).isEqualTo("original_value")

    policy.raiseResultsOnExecuteAndCapture(
      Context(contextData = mutableMapOf("key" to "new_value")),
      TestResult.FAULT,
      TestResult.GOOD
    )

    assertThat(contextValue).isEqualTo("new_value")
  }

  @Test
  fun shouldCreateNewStateForEachCallToPolicy() = runBlockingTest {
    val policy = Policy.asyncRetry<TestResult>()
      .handleResult(TestResult.FAULT)
      .retry(1)

    val result1 = policy.raiseResults(TestResult.FAULT, TestResult.GOOD)

    assertThat(result1).isEqualTo(TestResult.GOOD)

    val result2 = policy.raiseResults(TestResult.FAULT, TestResult.GOOD)

    assertThat(result2).isEqualTo(TestResult.GOOD)
  }

  @Test
  fun shouldNotCallOnRetryWhenRetryCountIsZero() = runBlockingTest {
    var retryInvoked = false
    val policy = Policy.asyncRetry<TestResult>()
      .handleResult(TestResult.FAULT)
      .retry(0) { _, _, _ -> retryInvoked = true }

    val result = policy.raiseResults(TestResult.FAULT, TestResult.GOOD)

    assertThat(result).isEqualTo(TestResult.FAULT)
    assertThat(retryInvoked).isFalse()
  }

  @Test
  fun shouldWaitAsynchronouslyForAsyncOnRetryDelegate() = runBlockingTest {
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
  fun shouldExecuteAllTriesWhenFaultingAndNotCancelled() = runBlockingTest {
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
  fun shouldNotExecuteActionWhenCancelledBeforeExecute() {
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
  fun shouldReportCancellationDuringOtherwiseNonFaultingActionExecutionAndCancelFurtherRetries() =
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
  fun shouldReportCancellationDuringFaultingInitialActionExecutionAndCancelFurtherRetries() =
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
  fun shouldReportCancellationDuringFaultingRetriedActionExecutionAndCancelFurtherRetries() = runBlockingTest {
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
  fun shouldReportCancellationDuringFaultingLastRetryExecution() = runBlockingTest {
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
  fun shouldReportCancellationAfterFaultingActionExecutionAndCancelFurtherRetriesIfOnRetryInvokesCancellation() {
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
