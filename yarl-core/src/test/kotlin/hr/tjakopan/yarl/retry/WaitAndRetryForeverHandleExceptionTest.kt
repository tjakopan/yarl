package hr.tjakopan.yarl.retry

import hr.tjakopan.yarl.Context
import hr.tjakopan.yarl.Policy
import hr.tjakopan.yarl.test.helpers.raiseExceptions
import org.assertj.core.api.Assertions.assertThat
import java.time.Duration
import kotlin.math.pow
import kotlin.test.Test
import kotlin.test.assertFailsWith

class WaitAndRetryForeverHandleExceptionTest {
  @Test
  fun shouldNotThrowRegardlessOfHowManyTimesTheSpecifiedExceptionIsRaised() {
    val policy = Policy.retry<Unit>()
      .handle(ArithmeticException::class)
      .waitAndRetryForever { _, _, _ -> Duration.ZERO }

    policy.raiseExceptions(3) { ArithmeticException() }
  }

  @Test
  fun shouldNotThrowRegardlessOfHowManyTimesOneOfTheSpecifiedExceptionIsRaised() {
    val policy = Policy.retry<Unit>()
      .handle(ArithmeticException::class)
      .handle(IllegalArgumentException::class)
      .waitAndRetryForever { _, _, _ -> Duration.ZERO }

    policy.raiseExceptions(3) { IllegalArgumentException() }
  }

  @Test
  fun shouldThrowWhenExceptionThrownIsNotTheSpecifiedExceptionType() {
    val policy = Policy.retry<Unit>()
      .handle(ArithmeticException::class)
      .waitAndRetryForever { _, _, _ -> Duration.ZERO }

    assertFailsWith(NullPointerException::class) {
      policy.raiseExceptions(1) { NullPointerException() }
    }
  }

  @Test
  fun shouldThrowWhenExceptionThrownIsNotOneOfTheSpecifiedExceptionTypes() {
    val policy = Policy.retry<Unit>()
      .handle(ArithmeticException::class)
      .handle(IllegalArgumentException::class)
      .waitAndRetryForever { _, _, _ -> Duration.ZERO }

    assertFailsWith(NullPointerException::class) {
      policy.raiseExceptions(1) { NullPointerException() }
    }
  }

  @Test
  fun shouldThrowWhenSpecifiedExceptionPredicateIsNotSatisfied() {
    val policy = Policy.retry<Unit>()
      .handle(ArithmeticException::class) { false }
      .waitAndRetryForever { _, _, _ -> Duration.ZERO }

    assertFailsWith(ArithmeticException::class) {
      policy.raiseExceptions(1) { ArithmeticException() }
    }
  }

  @Test
  fun shouldThrowWhenNoneOfTheSpecifiedExceptionPredicatesAreSatisfied() {
    val policy = Policy.retry<Unit>()
      .handle(ArithmeticException::class) { false }
      .handle(IllegalArgumentException::class) { false }
      .waitAndRetryForever { _, _, _ -> Duration.ZERO }

    assertFailsWith(IllegalArgumentException::class) {
      policy.raiseExceptions(1) { IllegalArgumentException() }
    }
  }

  @Test
  fun shouldNotThrowWhenSpecifiedExceptionPredicateIsSatisfied() {
    val policy = Policy.retry<Unit>()
      .handle(ArithmeticException::class) { true }
      .waitAndRetryForever { _, _, _ -> Duration.ofMillis(1) }

    policy.raiseExceptions(1) { ArithmeticException() }
  }

  @Test
  fun shouldNotThrowWhenOneOfTheSpecifiedExceptionPredicatesAreSatisfied() {
    val policy = Policy.retry<Unit>()
      .handle(IllegalArgumentException::class) { true }
      .waitAndRetryForever { _, _, _ -> Duration.ofMillis(1) }

    policy.raiseExceptions(1) { IllegalArgumentException() }
  }

  @Test
  fun shouldCallOnRetryOnEachRetryWithTheCurrentException() {
    val expectedExceptions = listOf("Exception #1", "Exception #2", "Exception #3")
    val retryExceptions = mutableListOf<Throwable>()
    val policy = Policy.retry<Unit>()
      .handle(ArithmeticException::class)
      .waitAndRetryForever({ _, _, _ -> Duration.ZERO })
      { outcome, _, _, _ -> outcome.onFailure { retryExceptions.add(it) } }

    policy.raiseExceptions(3) { ArithmeticException("Exception #$it") }

    assertThat(retryExceptions.map { it.message }).containsExactlyElementsOf(expectedExceptions)
  }

  @Test
  fun shouldCallOnRetryOnEachRetryWithTheCurrentRetryCount() {
    val expectedRetryCounts = listOf(1, 2, 3)
    val retryCounts = mutableListOf<Int>()
    val policy = Policy.retry<Unit>()
      .handle(ArithmeticException::class)
      .waitAndRetryForever({ _, _, _ -> Duration.ZERO })
      { _, _, retryCount, _ -> retryCounts.add(retryCount) }

    policy.raiseExceptions(3) { ArithmeticException() }

    assertThat(retryCounts).containsExactlyElementsOf(expectedRetryCounts)
  }

  @Test
  fun shouldNotCallOnRetryWhenNoRetriesArePerformed() {
    var onRetryCalled = false
    val policy = Policy.retry<Unit>()
      .handle(ArithmeticException::class)
      .waitAndRetryForever({ _, _, _ -> Duration.ofMillis(1) })
      { _, _, _, _ -> onRetryCalled = true }

    assertFailsWith(IllegalArgumentException::class) {
      policy.raiseExceptions(1) { IllegalArgumentException() }
    }
    assertThat(onRetryCalled).isFalse()
  }

  @Test
  fun shouldCreateNewContextForEachCallToExecute() {
    var contextValue: String? = null
    val policy = Policy.retry<Unit>()
      .handle(ArithmeticException::class)
      .waitAndRetryForever({ _, _, _ -> Duration.ofMillis(1) })
      { _, _, _, context -> contextValue = context.contextData["key"].toString() }

    policy.raiseExceptions(Context(contextData = mutableMapOf("key" to "original_value")), 1) {
      ArithmeticException()
    }

    assertThat(contextValue).isEqualTo("original_value")

    policy.raiseExceptions(Context(contextData = mutableMapOf("key" to "new_value")), 1) {
      ArithmeticException()
    }

    assertThat(contextValue).isEqualTo("new_value")
  }

  @Test
  fun shouldCalculateRetryDurationsFromCurrentRetryAttemptAndDurationProvider() {
    val expectedRetryWaits = listOf(
      Duration.ofMillis(2),
      Duration.ofMillis(4),
      Duration.ofMillis(8),
      Duration.ofMillis(16),
      Duration.ofMillis(32)
    )
    val actualRetryWaits = mutableListOf<Duration>()
    val policy = Policy.retry<Unit>()
      .handle(ArithmeticException::class)
      .waitAndRetryForever({ retryAttempt, _, _ ->
        Duration.ofMillis(2.0.pow(retryAttempt).toLong())
      })
      { _, duration, _, _ -> actualRetryWaits.add(duration) }

    policy.raiseExceptions(5) { ArithmeticException() }

    assertThat(actualRetryWaits).containsExactlyElementsOf(expectedRetryWaits)
  }

  @Test
  fun shouldBeAbleToCalculateRetryDurationsBasedOnTheHandledFault() {
    val expectedRetryWaits = mapOf<RuntimeException, Duration>(
      ArithmeticException() to Duration.ofMillis(2),
      IllegalArgumentException() to Duration.ofMillis(4)
    )
    val actualRetryWaits = mutableListOf<Duration>()
    val policy = Policy.retry<Unit>()
      .handle(RuntimeException::class)
      .waitAndRetryForever({ _, outcome, _ ->
        outcome.fold(
          { Duration.ZERO },
          { expectedRetryWaits[it] }) ?: Duration.ZERO
      }) { _, duration, _, _ -> actualRetryWaits.add(duration) }

    val iterator = expectedRetryWaits.iterator()
    policy.execute {
      if (iterator.hasNext()) {
        throw iterator.next().key
      }
    }

    assertThat(actualRetryWaits).containsExactlyElementsOf(expectedRetryWaits.values)
  }

  @Test
  fun shouldBeAbleToPassRetryDurationFromExecutionToSleepDurationProviderViaContext() {
    val expectedRetryDuration = Duration.ofMillis(1)
    var actualRetryDuration: Duration? = null
    val defaultRetryAfter = Duration.ofMillis(30)
    val policy = Policy.retry<Unit>()
      .handle(ArithmeticException::class)
      .waitAndRetryForever({ _, _, context ->
        when (context.contextData.containsKey("RetryAfter")) {
          true -> context.contextData["RetryAfter"] as Duration
          else -> defaultRetryAfter
        }
      }) { _, duration, _, _ -> actualRetryDuration = duration }

    var failedOnce = false
    policy.execute(mutableMapOf<String, Any>("RetryAfter" to defaultRetryAfter)) { context: Context ->
      context.contextData["RetryAfter"] = expectedRetryDuration
      if (!failedOnce) {
        failedOnce = true
        throw ArithmeticException()
      }
    }

    assertThat(actualRetryDuration).isEqualTo(expectedRetryDuration)
  }
}
