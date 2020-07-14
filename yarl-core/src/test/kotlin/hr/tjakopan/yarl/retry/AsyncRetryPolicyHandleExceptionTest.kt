package hr.tjakopan.yarl.retry

import hr.tjakopan.yarl.Context
import hr.tjakopan.yarl.Policy
import hr.tjakopan.yarl.test.helpers.raiseExceptions
import hr.tjakopan.yarl.test.helpers.raiseExceptionsAndOrCancellation
import hr.tjakopan.yarl.test.helpers.raiseExceptionsOnExecuteAndCapture
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.runBlockingTest
import org.assertj.core.api.Assertions.assertThat
import java.time.Duration
import kotlin.test.Test
import kotlin.test.assertFailsWith

@ExperimentalCoroutinesApi
class AsyncRetryPolicyHandleExceptionTest {
  @Test
  fun shouldNotThrowWhenSpecifiedExceptionThrownSameNumberOfTimesAsRetryCount() = runBlockingTest {
    val policy = Policy.asyncRetry<Unit>()
      .handle(ArithmeticException::class)
      .retry(3)

    policy.raiseExceptions(3) { ArithmeticException() }
  }

  @Test
  fun shouldNotThrowWhenOneOfTheSpecifiedExceptionsThrownSameNumberOfTriesAsRetryCount() = runBlockingTest {
    val policy = Policy.asyncRetry<Unit>()
      .handle(ArithmeticException::class)
      .handle(IllegalArgumentException::class)
      .retry(3)

    policy.raiseExceptions(3) { IllegalArgumentException() }
  }

  @Test
  fun shouldNotThrowWhenSpecifiedExceptionThrownLessNumberOfTimesThanRetryCount() = runBlockingTest {
    val policy = Policy.asyncRetry<Unit>()
      .handle(ArithmeticException::class)
      .retry(3)

    policy.raiseExceptions(1) { ArithmeticException() }
  }

  @Test
  fun shouldNotThrowWhenOneOfTheSpecifiedExceptionsThrownLessNumberOfTimesThanRetryCount() = runBlockingTest {
    val policy = Policy.asyncRetry<Unit>()
      .handle(ArithmeticException::class)
      .handle(IllegalArgumentException::class)
      .retry(3)

    policy.raiseExceptions(1) { IllegalArgumentException() }
  }

  @Test
  fun shouldThrowWhenSpecifiedExceptionThrownMoreTimesThanRetryCount() = runBlockingTest {
    val policy = Policy.asyncRetry<Unit>()
      .handle(ArithmeticException::class)
      .retry(3)

    assertFailsWith(ArithmeticException::class) {
      policy.raiseExceptions(3 + 1) { ArithmeticException() }
    }
  }

  @Test
  fun shouldThrowWhenOneOfTheSpecifiedExceptionsThrownMoreTimesThanRetryCount() = runBlockingTest {
    val policy = Policy.asyncRetry<Unit>()
      .handle(ArithmeticException::class)
      .handle(IllegalArgumentException::class)
      .retry(3)

    assertFailsWith(IllegalArgumentException::class) {
      policy.raiseExceptions(3 + 1) { IllegalArgumentException() }
    }
  }

  @Test
  fun shouldThrowWhenExceptionThrownIsNotTheSpecifiedExceptionType() = runBlockingTest {
    val policy = Policy.asyncRetry<Unit>()
      .handle(ArithmeticException::class)
      .retry(3)

    assertFailsWith(NullPointerException::class) {
      policy.raiseExceptions(1) { NullPointerException() }
    }
  }

  @Test
  fun shouldThrowWhenSpecifiedExceptionPredicateIsNotSatisfied() = runBlockingTest {
    val policy = Policy.asyncRetry<Unit>()
      .handle(ArithmeticException::class) { false }
      .retry()

    assertFailsWith(ArithmeticException::class) {
      policy.raiseExceptions(1) { ArithmeticException() }
    }
  }

  @Test
  fun shouldThrowWhenNoneOfTheSpecifiedExceptionPredicatesAreSatisfied() = runBlockingTest {
    val policy = Policy.asyncRetry<Unit>()
      .handle(ArithmeticException::class) { false }
      .handle(IllegalArgumentException::class) { false }
      .retry()

    assertFailsWith(IllegalArgumentException::class) {
      policy.raiseExceptions(1) { IllegalArgumentException() }
    }
  }

  @Test
  fun shouldNotThrowWhenSpecifiedExceptionPredicateIsSatisfied() = runBlockingTest {
    val policy = Policy.asyncRetry<Unit>()
      .handle(ArithmeticException::class) { true }
      .retry()

    policy.raiseExceptions(1) { ArithmeticException() }
  }

  @Test
  fun shouldNotThrowWhenOneOfTheSpecifiedExceptionPredicatesIsSatisfied() = runBlockingTest {
    val policy = Policy.asyncRetry<Unit>()
      .handle(ArithmeticException::class) { true }
      .handle(IllegalArgumentException::class) { true }
      .retry()

    policy.raiseExceptions(1) { IllegalArgumentException() }
  }

  @Test
  fun shouldCallOnRetryOnEachRetryWithTheCurrentRetryCount() = runBlockingTest {
    val expectedRetryCounts = listOf(1, 2, 3)
    val retryCounts = mutableListOf<Int>()
    val policy = Policy.asyncRetry<Unit>()
      .handle(ArithmeticException::class)
      .retry(3) { _, retryCount, _ -> retryCounts.add(retryCount) }

    policy.raiseExceptions(3) { ArithmeticException() }

    assertThat(retryCounts).containsExactlyElementsOf(expectedRetryCounts)
  }

  @Test
  fun shouldCallOnRetryOnEachRetryWithTheCurrentException() = runBlockingTest {
    val expectedExceptions = listOf("Exception #1", "Exception #2", "Exception #3")
    val retryExceptions = mutableListOf<Throwable>()
    val policy = Policy.asyncRetry<Unit>()
      .handle(ArithmeticException::class)
      .retry(3) { outcome, _, _ ->
        outcome.onFailure { e -> retryExceptions.add(e) }
      }

    policy.raiseExceptions(3) { i -> ArithmeticException("Exception #$i") }

    assertThat(retryExceptions.map { e -> e.message }).containsExactlyElementsOf(expectedExceptions)
  }

  @Test
  fun shouldCallOnRetryWithAHandledCauseException() = runBlockingTest {
    var exceptionPassedToOnRetry: Throwable? = null
    val policy = Policy.asyncRetry<Unit>()
      .handleCause(ArithmeticException::class)
      .retry(3) { outcome, _, _ ->
        outcome.onFailure { e -> exceptionPassedToOnRetry = e }
      }
    val causeException = ArithmeticException()
    val exception = Exception(causeException)

    policy.raiseExceptions(1) { exception }

    assertThat(exceptionPassedToOnRetry).isSameAs(exception)
    assertThat(exceptionPassedToOnRetry?.cause).isSameAs(causeException)
  }

  @Test
  fun shouldNotCallOnRetryWhenNoRetriesArePerformed() = runBlockingTest {
    var retryCalled = false
    val policy = Policy.asyncRetry<Unit>()
      .handle(ArithmeticException::class)
      .retry { _, _, _ -> retryCalled = true }

    assertFailsWith(IllegalArgumentException::class) {
      policy.raiseExceptions(1) { IllegalArgumentException() }
    }
    assertThat(retryCalled).isFalse()
  }

  @Test
  fun shouldCreateNewStateForEachCallToPolicy() = runBlockingTest {
    val policy = Policy.asyncRetry<Unit>()
      .handle(ArithmeticException::class)
      .retry()

    policy.raiseExceptions(1) { ArithmeticException() }
    policy.raiseExceptions(1) { ArithmeticException() }
  }

  @Test
  fun shouldCallOnRetryWithThePassedContext() = runBlockingTest {
    var context: Context? = null
    val policy = Policy.asyncRetry<Unit>()
      .handle(ArithmeticException::class)
      .retry { _, _, ctx -> context = ctx }

    policy.raiseExceptions(
      Context(contextData = mapOf("key1" to "value1", "key2" to "value2")),
      1
    ) { ArithmeticException() }

    @Suppress("UsePropertyAccessSyntax")
    assertThat(context).isNotNull()
    assertThat(context?.contextData).containsKeys("key1", "key2")
      .containsValues("value1", "value2")
  }

  @Test
  fun shouldCallOnRetryWithThePassedContextWhenExecuteAndCapture() = runBlockingTest {
    var context: Context? = null
    val policy = Policy.asyncRetry<Unit>()
      .handle(ArithmeticException::class)
      .retry { _, _, ctx -> context = ctx }

    policy.raiseExceptionsOnExecuteAndCapture(
      Context(contextData = mapOf("key1" to "value1", "key2" to "value2")),
      1
    ) { ArithmeticException() }

    @Suppress("UsePropertyAccessSyntax")
    assertThat(context).isNotNull()
    assertThat(context?.contextData).containsKeys("key1", "key2")
      .containsValues("value1", "value2")
  }

  @Test
  fun contextShouldBeEmptyIfExecuteNotCalledWithContext() = runBlockingTest {
    var capturedContext: Context? = null
    val policy = Policy.asyncRetry<Unit>()
      .handle(ArithmeticException::class)
      .retry { _, _, ctx -> capturedContext = ctx }

    policy.raiseExceptions(1) { ArithmeticException() }

    assertThat(capturedContext).isNotNull
    assertThat(capturedContext?.policyWrapKey).isNull()
    assertThat(capturedContext?.policyKey).isNotNull()
    assertThat(capturedContext?.operationKey).isNull()
    assertThat(capturedContext?.contextData).isEmpty()
  }

  @Test
  fun shouldCreateNewContextForEachCallToExecute() = runBlockingTest {
    var contextValue: String? = null
    val policy = Policy.asyncRetry<Unit>()
      .handle(ArithmeticException::class)
      .retry { _, _, ctx -> contextValue = ctx.contextData["key"].toString() }

    policy.raiseExceptions(Context(contextData = mapOf("key" to "original_value")), 1) { ArithmeticException() }

    assertThat(contextValue).isEqualTo("original_value")

    policy.raiseExceptions(Context(contextData = mapOf("key" to "new_value")), 1) { ArithmeticException() }

    assertThat(contextValue).isEqualTo("new_value")
  }

  @Test
  fun shouldCreateNewContextForEachCallToExecuteAndCapture() = runBlockingTest {
    var contextValue: String? = null
    val policy = Policy.asyncRetry<Unit>()
      .handle(ArithmeticException::class)
      .retry { _, _, ctx -> contextValue = ctx.contextData["key"].toString() }

    policy.raiseExceptionsOnExecuteAndCapture(
      Context(contextData = mapOf("key" to "original_value")),
      1
    ) { ArithmeticException() }

    assertThat(contextValue).isEqualTo("original_value")

    policy.raiseExceptionsOnExecuteAndCapture(
      Context(contextData = mapOf("key" to "new_value")),
      1
    ) { ArithmeticException() }

    assertThat(contextValue).isEqualTo("new_value")
  }

  @Test
  fun shouldNotCallOnRetryWhenRetryCountIsZero() = runBlockingTest {
    var retryInvoked = false
    val policy = Policy.asyncRetry<Unit>()
      .handle(ArithmeticException::class)
      .retry(0) { _, _, _ -> retryInvoked = true }

    assertFailsWith(ArithmeticException::class) {
      policy.raiseExceptions(1) { ArithmeticException() }
    }
    assertThat(retryInvoked).isFalse()
  }

  @Test
  fun shouldWaitAsynchronouslyForAsyncOnRetryDelegate() = runBlockingTest {
    val duration = Duration.ofMillis(200)
    var executeDelegateInvocations = 0
    var executeDelegateInvocationsWhenOnRetryExits = 0
    val policy = Policy.asyncRetry<Unit>()
      .handle(ArithmeticException::class)
      .retry { _, _, _ ->
        delay(duration.toMillis())
        executeDelegateInvocationsWhenOnRetryExits = executeDelegateInvocations
      }

    assertFailsWith(ArithmeticException::class) {
      policy.execute {
        executeDelegateInvocations++
        throw ArithmeticException()
      }
    }
    assertThat(executeDelegateInvocationsWhenOnRetryExits).isEqualTo(1)
    assertThat(executeDelegateInvocations).isEqualTo(2)
  }

  @Test
  fun shouldExecuteActionWhenNonFaultingAndNotCancelled() = runBlockingTest {
    val policy = Policy.asyncRetry<Unit>()
      .handle(ArithmeticException::class)
      .retry(3)
    var attemptsInvoked = 0
    val onExecute: () -> Unit = { attemptsInvoked++ }

    policy.raiseExceptions(0, onExecute) { ArithmeticException() }

    assertThat(attemptsInvoked).isEqualTo(1)
  }

  @Test
  fun shouldExecuteAllTriesWhenFaultingAndNotCancelled() = runBlockingTest {
    val policy = Policy.asyncRetry<Unit>()
      .handle(ArithmeticException::class)
      .retry(3)
    var attemptsInvoked = 0
    val onExecute: () -> Unit = { attemptsInvoked++ }

    assertFailsWith(ArithmeticException::class) {
      policy.raiseExceptions(1 + 3, onExecute) { ArithmeticException() }
    }
    assertThat(attemptsInvoked).isEqualTo(1 + 3)
  }

  @Test
  fun shouldNotExecuteActionWhenCancelledBeforeExecute() {
    assertFailsWith(CancellationException::class) {
      runBlockingTest {
        val policy = Policy.asyncRetry<Unit>()
          .handle(ArithmeticException::class)
          .retry(3)
        var attemptsInvoked = 0
        val onExecute: () -> Unit = { attemptsInvoked++ }

        coroutineContext.cancel()

        assertFailsWith(CancellationException::class) {
          policy.raiseExceptions(1 + 3, onExecute) { ArithmeticException() }
        }
        assertThat(attemptsInvoked).isEqualTo(0)
      }
    }
  }

  @Test
  fun shouldReportCancellationDuringOtherwiseNonFaultingActionExecutionAndCancelFurtherRetries() =
    runBlockingTest {
      val policy = Policy.asyncRetry<Unit>()
        .handle(ArithmeticException::class)
        .retry(3)
      var attemptsInvoked = 0
      val onExecute: () -> Unit = { attemptsInvoked++ }

      assertFailsWith(CancellationException::class) {
        policy.raiseExceptionsAndOrCancellation(0, 1, onExecute) {
          ArithmeticException()
        }
      }
      assertThat(attemptsInvoked).isEqualTo(1)
    }

  @Test
  fun shouldReportCancellationDuringFaultingInitialActionExecutionAndCancelFurtherRetries() =
    runBlockingTest {
      val policy = Policy.asyncRetry<Unit>()
        .handle(ArithmeticException::class)
        .retry(3)
      var attemptsInvoked = 0
      val onExecute: () -> Unit = { attemptsInvoked++ }

      assertFailsWith(CancellationException::class) {
        policy.raiseExceptionsAndOrCancellation(1 + 3, 1, onExecute) {
          ArithmeticException()
        }
      }
      assertThat(attemptsInvoked).isEqualTo(1)
    }

  @Test
  fun shouldReportCancellationDuringFaultingRetriedActionExecutionAndCancelFurtherRetries() = runBlockingTest {
    val policy = Policy.asyncRetry<Unit>()
      .handle(ArithmeticException::class)
      .retry(3)
    var attemptsInvoked = 0
    val onExecute: () -> Unit = { attemptsInvoked++ }

    assertFailsWith(CancellationException::class) {
      policy.raiseExceptionsAndOrCancellation(1 + 3, 2, onExecute) {
        ArithmeticException()
      }
    }
    assertThat(attemptsInvoked).isEqualTo(2)
  }

  @Test
  fun shouldReportCancellationDuringFaultingLastRetryExecution() = runBlockingTest {
    val policy = Policy.asyncRetry<Unit>()
      .handle(ArithmeticException::class)
      .retry(3)
    var attemptsInvoked = 0
    val onExecute: () -> Unit = { attemptsInvoked++ }

    assertFailsWith(CancellationException::class) {
      policy.raiseExceptionsAndOrCancellation(1 + 3, 1 + 3, onExecute) {
        ArithmeticException()
      }
    }
    assertThat(attemptsInvoked).isEqualTo(1 + 3)
  }

  @Test
  fun shouldReportCancellationAfterFaultingActionExecutionAndCancelFurtherRetriesIfOnRetryInvokesCancellation() {
    assertFailsWith(CancellationException::class) {
      runBlockingTest {
        val policy = Policy.asyncRetry<Unit>()
          .handle(ArithmeticException::class)
          .retry(3) { _, _, _ -> coroutineContext.cancel() }
        var attemptsInvoked = 0
        val onExecute: () -> Unit = { attemptsInvoked++ }

        assertFailsWith(CancellationException::class) {
          policy.raiseExceptions(1 + 3, onExecute) { ArithmeticException() }
        }
        assertThat(attemptsInvoked).isEqualTo(1)
      }
    }
  }

  @Test
  fun shouldExecuteFunctionReturningValueWhenNotCancelled() = runBlockingTest {
    val policy = Policy.asyncRetry<Boolean>()
      .handle(ArithmeticException::class)
      .retry(3)
    var attemptsInvoked = 0
    val onExecute: () -> Unit = { attemptsInvoked++ }

    val result = policy.raiseExceptions(0, onExecute, true) {
      ArithmeticException()
    }

    assertThat(result).isTrue()
    assertThat(attemptsInvoked).isEqualTo(1)
  }

  @Test
  fun shouldHonourAndReportCancellationDuringFunctionExecution() = runBlockingTest {
    val policy = Policy.asyncRetry<Boolean>()
      .handle(ArithmeticException::class)
      .retry(3)
    var attemptsInvoked = 0
    val onExecute: () -> Unit = { attemptsInvoked++ }

    assertFailsWith(CancellationException::class) {
      policy.raiseExceptionsAndOrCancellation(
        0, 1, onExecute,
        true
      ) { ArithmeticException() }
    }
    assertThat(attemptsInvoked).isEqualTo(1)
  }
}
