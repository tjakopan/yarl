package hr.tjakopan.yarl.retry

import hr.tjakopan.yarl.Context
import hr.tjakopan.yarl.Policy
import hr.tjakopan.yarl.test.helpers.raiseExceptions
import hr.tjakopan.yarl.test.helpers.raiseExceptionsAndOrCancellation
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.runBlockingTest
import org.assertj.core.api.Assertions.assertThat
import java.time.Duration
import kotlin.math.pow
import kotlin.test.Test
import kotlin.test.assertFailsWith

@ExperimentalCoroutinesApi
class AsyncWaitAndRetryForeverHandleExceptionTest {
  @Test
  fun shouldNotThrowRegardlessOfHowManyTimesTheSpecifiedExceptionIsRaised() = runBlockingTest {
    val policy = Policy.asyncRetry<Unit>()
      .handle(ArithmeticException::class)
      .waitAndRetryForever { _, _, _ -> Duration.ZERO }

    policy.raiseExceptions(3) { ArithmeticException() }
  }

  @Test
  fun shouldNotThrowRegardlessOfHowManyTimesOneOfTheSpecifiedExceptionIsRaised() = runBlockingTest {
    val policy = Policy.asyncRetry<Unit>()
      .handle(ArithmeticException::class)
      .handle(IllegalArgumentException::class)
      .waitAndRetryForever { _, _, _ -> Duration.ZERO }

    policy.raiseExceptions(3) { IllegalArgumentException() }
  }

  @Test
  fun shouldThrowWhenExceptionThrownIsNotTheSpecifiedExceptionType() = runBlockingTest {
    val policy = Policy.asyncRetry<Unit>()
      .handle(ArithmeticException::class)
      .waitAndRetryForever { _, _, _ -> Duration.ZERO }

    assertFailsWith(NullPointerException::class) {
      policy.raiseExceptions(1) { NullPointerException() }
    }
  }

  @Test
  fun shouldThrowWhenExceptionThrownIsNotOneOfTheSpecifiedExceptionTypes() = runBlockingTest {
    val policy = Policy.asyncRetry<Unit>()
      .handle(ArithmeticException::class)
      .handle(IllegalArgumentException::class)
      .waitAndRetryForever { _, _, _ -> Duration.ZERO }

    assertFailsWith(NullPointerException::class) {
      policy.raiseExceptions(1) { NullPointerException() }
    }
  }

  @Test
  fun shouldThrowWhenSpecifiedExceptionPredicateIsNotSatisfied() = runBlockingTest {
    val policy = Policy.asyncRetry<Unit>()
      .handle(ArithmeticException::class) { false }
      .waitAndRetryForever { _, _, _ -> Duration.ZERO }

    assertFailsWith(ArithmeticException::class) {
      policy.raiseExceptions(1) { ArithmeticException() }
    }
  }

  @Test
  fun shouldThrowWhenNoneOfTheSpecifiedExceptionPredicatesAreSatisfied() = runBlockingTest {
    val policy = Policy.asyncRetry<Unit>()
      .handle(ArithmeticException::class) { false }
      .handle(IllegalArgumentException::class) { false }
      .waitAndRetryForever { _, _, _ -> Duration.ZERO }

    assertFailsWith(IllegalArgumentException::class) {
      policy.raiseExceptions(1) { IllegalArgumentException() }
    }
  }

  @Test
  fun shouldNotThrowWhenSpecifiedExceptionPredicateIsSatisfied() = runBlockingTest {
    val policy = Policy.asyncRetry<Unit>()
      .handle(ArithmeticException::class)
      .waitAndRetryForever { _, _, _ -> Duration.ofMillis(1) }

    policy.raiseExceptions(1) { ArithmeticException() }
  }

  @Test
  fun shouldNotThrowWhenOneOfTheSpecifiedExceptionPredicatesAreSatisfied() = runBlockingTest {
    val policy = Policy.asyncRetry<Unit>()
      .handle(ArithmeticException::class)
      .handle(IllegalArgumentException::class)
      .waitAndRetryForever { _, _, _ -> Duration.ofMillis(1) }

    policy.raiseExceptions(1) { IllegalArgumentException() }
  }

  @Test
  fun shouldCallOnRetryOnEachRetryWithTheCurrentException() = runBlockingTest {
    val expectedExceptions = listOf("Exception #1", "Exception #2", "Exception #3")
    val retryExceptions = mutableListOf<Throwable>()
    val policy = Policy.asyncRetry<Unit>()
      .handle(ArithmeticException::class)
      .waitAndRetryForever({ _, _, _ -> Duration.ZERO })
      { outcome, _, _, _ -> outcome.onFailure { retryExceptions.add(it) } }

    policy.raiseExceptions(3) { ArithmeticException("Exception #$it") }

    assertThat(retryExceptions.map { it.message }).containsExactlyElementsOf(expectedExceptions)
  }

  @Test
  fun shouldCallOnRetryOnEachRetryWithTheCurrentRetryCount() = runBlockingTest {
    val expectedRetryCounts = listOf(1, 2, 3)
    val retryCounts = mutableListOf<Int>()
    val policy = Policy.asyncRetry<Unit>()
      .handle(ArithmeticException::class)
      .waitAndRetryForever({ _, _, _ -> Duration.ZERO })
      { _, _, retryCount, _ -> retryCounts.add(retryCount) }

    policy.raiseExceptions(3) { ArithmeticException() }

    assertThat(retryCounts).containsExactlyElementsOf(expectedRetryCounts)
  }

  @Test
  fun shouldNotCallOnRetryWhenNoRetriesArePerformed() = runBlockingTest {
    var onRetryCalled = false
    val policy = Policy.asyncRetry<Unit>()
      .handle(ArithmeticException::class)
      .waitAndRetryForever({ _, _, _ -> Duration.ofMillis(1) })
      { _, _, _, _ -> onRetryCalled = true }

    assertFailsWith(IllegalArgumentException::class) {
      policy.raiseExceptions(1) { IllegalArgumentException() }
    }
    assertThat(onRetryCalled).isFalse()
  }

  @Test
  fun shouldCreateNewContextForEachCallToExecute() = runBlockingTest {
    var contextValue: String? = null
    val policy = Policy.asyncRetry<Unit>()
      .handle(ArithmeticException::class)
      .waitAndRetryForever({ _, _, _ -> Duration.ofMillis(1) })
      { _, _, _, ctx -> contextValue = ctx.contextData["key"].toString() }

    policy.raiseExceptions(Context(contextData = mutableMapOf("key" to "original_value")), 1)
    { ArithmeticException() }

    assertThat(contextValue).isEqualTo("original_value")

    policy.raiseExceptions(Context(contextData = mutableMapOf("key" to "new_value")), 1)
    { ArithmeticException() }

    assertThat(contextValue).isEqualTo("new_value")
  }

  @Test
  fun shouldCalculateRetryDurationsFromCurrentRetryAttemptAndDurationProvider() = runBlockingTest {
    val expectedRetryWaits = listOf(
      Duration.ofSeconds(2),
      Duration.ofSeconds(4),
      Duration.ofSeconds(8),
      Duration.ofSeconds(16),
      Duration.ofSeconds(32)
    )
    val actualRetryWaits = mutableListOf<Duration>()
    val policy = Policy.asyncRetry<Unit>()
      .handle(ArithmeticException::class)
      .waitAndRetryForever({ retryAttempt, _, _ ->
        Duration.ofSeconds(2.0.pow(retryAttempt.toDouble()).toLong())
      }) { _, duration, _, _ -> actualRetryWaits.add(duration) }

    policy.raiseExceptions(5) { ArithmeticException() }

    assertThat(actualRetryWaits).containsExactlyElementsOf(expectedRetryWaits)
  }

  @Test
  fun shouldBeAbleToCalculateRetryDurationsBasedOnTheHandledFault() = runBlockingTest {
    val expectedRetryWaits =
      mapOf(ArithmeticException() to Duration.ofSeconds(2), NullPointerException() to Duration.ofSeconds(4))
    val actualRetryWaits = mutableListOf<Duration>()
    val policy = Policy.asyncRetry<Unit>()
      .handle(RuntimeException::class)
      .waitAndRetryForever({ _, outcome, _ ->
        outcome.fold(
          { Duration.ZERO },
          { expectedRetryWaits[it] }) ?: Duration.ZERO
      }) { _, duration, _, _ -> actualRetryWaits.add(duration) }

    val iterator = expectedRetryWaits.iterator()
    policy.execute {
      if (iterator.hasNext()) throw iterator.next().key
    }

    assertThat(actualRetryWaits).containsExactlyElementsOf(expectedRetryWaits.values)
  }

  @Test
  fun shouldBeAbleToPassRetryDurationFromExecutionToSleepDurationProviderViaContext() = runBlockingTest {
    val expectedRetryDuration = Duration.ofSeconds(1)
    var actualRetryDuration: Duration? = null
    val defaultRetryAfter = Duration.ofSeconds(30)
    val policy = Policy.asyncRetry<Unit>()
      .handle(ArithmeticException::class)
      .waitAndRetryForever({ _, _, context ->
        when (context.contextData.containsKey("RetryAfter")) {
          true -> context.contextData["RetryAfter"] as Duration
          else -> defaultRetryAfter
        }
      }
      ) { _, duration, _, _ -> actualRetryDuration = duration }
    var failedOnce = false

    policy.execute(mutableMapOf<String, Any>("RetryAfter" to defaultRetryAfter)) { context ->
      context.contextData["RetryAfter"] = expectedRetryDuration
      if (!failedOnce) {
        failedOnce = true
        throw ArithmeticException()
      }
    }

    assertThat(actualRetryDuration).isEqualTo(expectedRetryDuration)
  }

  @Test
  fun shouldWaitAsynchronouslyForAsyncOnRetryDelegate() = runBlockingTest {
    val duration = Duration.ofMillis(200)
    var executeDelegateInvocations = 0
    var executeDelegateInvocationsWhenOnRetryExits = 0
    val policy = Policy.asyncRetry<Unit>()
      .handle(ArithmeticException::class)
      .waitAndRetryForever({ _, _, _ -> Duration.ZERO }) { _, _, _, _ ->
        delay(duration.toMillis())
        executeDelegateInvocationsWhenOnRetryExits = executeDelegateInvocations
      }

    policy.execute {
      executeDelegateInvocations++
      if (executeDelegateInvocations == 1) {
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
      .waitAndRetryForever { _, _, _ -> Duration.ZERO }
    var attemptsInvoked = 0
    val onExecute: () -> Unit = { attemptsInvoked++ }

    policy.raiseExceptions(0, onExecute) { ArithmeticException() }

    assertThat(attemptsInvoked).isEqualTo(1)
  }

  @Test
  fun shouldNotExecuteActionWhenCancelledBeforeExecute() {
    assertFailsWith(CancellationException::class) {
      runBlockingTest {
        val policy = Policy.asyncRetry<Unit>()
          .handle(ArithmeticException::class)
          .waitAndRetryForever { _, _, _ -> Duration.ZERO }
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
        .waitAndRetryForever { _, _, _ -> Duration.ZERO }
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
        .waitAndRetryForever { _, _, _ -> Duration.ZERO }
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
      .waitAndRetryForever { _, _, _ -> Duration.ZERO }
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
  fun shouldReportCancellationAfterFaultingActionExecutionAndCancelFurtherRetriesIfOnRetryInvokesCancellation() {
    assertFailsWith(CancellationException::class) {
      runBlockingTest {
        val policy = Policy.asyncRetry<Unit>()
          .handle(ArithmeticException::class)
          .waitAndRetryForever({ _, _, _ -> Duration.ZERO })
          { _, _, _, _ -> coroutineContext.cancel() }
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
      .waitAndRetryForever { _, _, _ -> Duration.ZERO }
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
      .waitAndRetryForever { _, _, _ -> Duration.ZERO }
    var attemptsInvoked = 0
    val onExecute: () -> Unit = { attemptsInvoked++ }
    var result: Boolean? = null

    assertFailsWith(CancellationException::class) {
      result = policy.raiseExceptionsAndOrCancellation(
        0, 1, onExecute,
        true
      ) { ArithmeticException() }
    }
    assertThat(result).isNull()
    assertThat(attemptsInvoked).isEqualTo(1)
  }
}
