package hr.tjakopan.yarl.retry

import hr.tjakopan.yarl.Context
import hr.tjakopan.yarl.Policy
import hr.tjakopan.yarl.test.helpers.raiseExceptions
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import java.time.Duration
import kotlin.math.pow
import kotlin.test.Test
import kotlin.test.assertFailsWith

class WaitAndRetryHandleExceptionTest {
  @Test
  fun shouldNotThrowWhenSpecifiedExceptionThrownSameNumberOfTimesAsThereAreSleepDurations() {
    val policy = Policy.retry<Unit>()
      .handle(ArithmeticException::class)
      .waitAndRetry(listOf(Duration.ofMillis(1), Duration.ofMillis(2), Duration.ofMillis(3)))

    policy.raiseExceptions(1) { ArithmeticException() }
  }

  @Test
  fun shouldNotThrowWhenOneOfTheSpecifiedExceptionsThrownSameNumberOfTimesAsThereAreSleepDurations() {
    val policy = Policy.retry<Unit>()
      .handle(ArithmeticException::class)
      .handle(IllegalArgumentException::class)
      .waitAndRetry(listOf(Duration.ofMillis(1), Duration.ofMillis(2), Duration.ofMillis(3)))

    policy.raiseExceptions(1) { IllegalArgumentException() }
  }

  @Test
  fun shouldNotThrowWhenSpecifiedExceptionThrownLessNumberOfTimesThanThereAreSleepDurations() {
    val policy = Policy.retry<Unit>()
      .handle(ArithmeticException::class)
      .waitAndRetry(listOf(Duration.ofMillis(1), Duration.ofMillis(2), Duration.ofMillis(3)))

    policy.raiseExceptions(2) { ArithmeticException() }
  }

  @Test
  fun shouldNotThrowWhenOneOfTheSpecifiedExceptionsThrownLessNumberThenTimesAsThereAreSleepDurations() {
    val policy = Policy.retry<Unit>()
      .handle(ArithmeticException::class)
      .handle(IllegalArgumentException::class)
      .waitAndRetry(listOf(Duration.ofMillis(1), Duration.ofMillis(2), Duration.ofMillis(3)))

    policy.raiseExceptions(2) { IllegalArgumentException() }
  }

  @Test
  fun shouldThrowWhenSpecifiedExceptionThrownMoreTimesThanThereAreSleepDurations() {
    val policy = Policy.retry<Unit>()
      .handle(ArithmeticException::class)
      .waitAndRetry(listOf(Duration.ofMillis(1), Duration.ofMillis(2), Duration.ofMillis(3)))

    assertFailsWith(ArithmeticException::class) {
      policy.raiseExceptions(3 + 1) { ArithmeticException() }
    }
  }

  @Test
  fun shouldThrowWhenOneOfTheSpecifiedExceptionsAreThrownMoreTimesThereAreSleepDurations() {
    val policy = Policy.retry<Unit>()
      .handle(ArithmeticException::class)
      .handle(IllegalArgumentException::class)
      .waitAndRetry(listOf(Duration.ofMillis(1), Duration.ofMillis(2), Duration.ofMillis(3)))

    assertFailsWith(IllegalArgumentException::class) {
      policy.raiseExceptions(3 + 1) { IllegalArgumentException() }
    }
  }

  @Test
  fun shouldThrowWhenExceptionThrownIsNotTheSpecifiedExceptionType() {
    val policy = Policy.retry<Unit>()
      .handle(ArithmeticException::class)
      .waitAndRetry(listOf())

    assertFailsWith(NullPointerException::class) {
      policy.raiseExceptions(1) { NullPointerException() }
    }
  }

  @Test
  fun shouldThrowWhenExceptionThrownIsNotOneOfTheSpecifiedExceptionTypes() {
    val policy = Policy.retry<Unit>()
      .handle(ArithmeticException::class)
      .handle(IllegalArgumentException::class)
      .waitAndRetry(listOf())

    assertFailsWith(NullPointerException::class) {
      policy.raiseExceptions(1) { NullPointerException() }
    }
  }

  @Test
  fun shouldThrowWhenSpecifiedExceptionPredicateIsNotSatisfied() {
    val policy = Policy.retry<Unit>()
      .handle(ArithmeticException::class) { false }
      .waitAndRetry(listOf())

    assertFailsWith(ArithmeticException::class) {
      policy.raiseExceptions(1) { ArithmeticException() }
    }
  }

  @Test
  fun shouldThrowWhenNoneOfTheSpecifiedExceptionPredicatesAreSatisfied() {
    val policy = Policy.retry<Unit>()
      .handle(ArithmeticException::class) { false }
      .handle(IllegalArgumentException::class) { false }
      .waitAndRetry(listOf())

    assertFailsWith(IllegalArgumentException::class) {
      policy.raiseExceptions(1) { IllegalArgumentException() }
    }
  }

  @Test
  fun shouldNotThrowWhenSpecifiedExceptionPredicateIsSatisfied() {
    val policy = Policy.retry<Unit>()
      .handle(ArithmeticException::class) { true }
      .waitAndRetry(listOf(Duration.ofMillis(1)))

    policy.raiseExceptions(1) { ArithmeticException() }
  }

  @Test
  fun shouldNotThrowWhenOneOfTheSpecifiedExceptionPredicatesAreSatisfied() {
    val policy = Policy.retry<Unit>()
      .handle(IllegalArgumentException::class) { true }
      .waitAndRetry(listOf(Duration.ofMillis(1)))

    policy.raiseExceptions(1) { IllegalArgumentException() }
  }

  @Test
  fun shouldCallOnRetryOnEachRetryWithTheCurrentTimespan() {
    val expectedRetryWaits = listOf(Duration.ofMillis(1), Duration.ofMillis(2), Duration.ofMillis(3))
    val actualRetryWaits = mutableListOf<Duration>()
    val policy = Policy.retry<Unit>()
      .handle(ArithmeticException::class)
      .waitAndRetry(
        listOf(
          Duration.ofMillis(1),
          Duration.ofMillis(2),
          Duration.ofMillis(3)
        )
      ) { _, duration, _, _ -> actualRetryWaits.add(duration) }

    policy.raiseExceptions(3) { ArithmeticException() }

    assertThat(actualRetryWaits).containsExactlyElementsOf(expectedRetryWaits)
  }

  @Test
  fun shouldCallOnRetryOnEachRetryWithTheCurrentException() {
    val expectedExceptions = listOf("Exception #1", "Exception #2", "Exception #3")
    val retryExceptions = mutableListOf<Throwable>()
    val policy = Policy.retry<Unit>()
      .handle(ArithmeticException::class)
      .waitAndRetry(
        listOf(
          Duration.ofMillis(1),
          Duration.ofMillis(2),
          Duration.ofMillis(3)
        )
      ) { outcome, _, _, _ -> outcome.onFailure { retryExceptions.add(it) } }

    policy.raiseExceptions(3) { ArithmeticException("Exception #$it") }

    assertThat(retryExceptions.map { it.message }).containsExactlyElementsOf(expectedExceptions)
  }

  @Test
  fun shouldCallOnRetryOnEachRetryWithTheCurrentRetryCount() {
    val expectedRetryCounts = listOf(1, 2, 3)
    val retryCounts = mutableListOf<Int>()
    val policy = Policy.retry<Unit>()
      .handle(ArithmeticException::class)
      .waitAndRetry(
        listOf(
          Duration.ofMillis(1),
          Duration.ofMillis(2),
          Duration.ofMillis(3)
        )
      ) { _, _, retryCount, _ -> retryCounts.add(retryCount) }

    policy.raiseExceptions(3) { ArithmeticException() }

    assertThat(retryCounts).containsExactlyElementsOf(expectedRetryCounts)
  }

  @Test
  fun shouldNotCallOnRetryWhenNoRetriesArePerformed() {
    var onRetryCalled = false
    val policy = Policy.retry<Unit>()
      .handle(ArithmeticException::class)
      .waitAndRetry(listOf()) { _, _, _, _ -> onRetryCalled = true }

    assertFailsWith(IllegalArgumentException::class) {
      policy.raiseExceptions(1) { IllegalArgumentException() }
    }
    assertThat(onRetryCalled).isFalse()
  }

  @Test
  fun shouldCreateNewStateForEachCallToPolicy() {
    val policy = Policy.retry<Unit>()
      .handle(ArithmeticException::class)
      .waitAndRetry(listOf(Duration.ofMillis(1)))

    policy.raiseExceptions(1) { ArithmeticException() }

    policy.raiseExceptions(1) { ArithmeticException() }
  }

  @Test
  fun shouldCallOnRetryWithThePassedContext() {
    var contextData: MutableMap<String, Any>? = null
    val policy = Policy.retry<Unit>()
      .handle(ArithmeticException::class)
      .waitAndRetry(
        listOf(
          Duration.ofMillis(1),
          Duration.ofMillis(2),
          Duration.ofMillis(3)
        )
      ) { _, _, _, context -> contextData = context.contextData }

    policy.raiseExceptions(
      Context(contextData = mutableMapOf("key1" to "value1", "key2" to "value2")),
      1
    ) { ArithmeticException() }

    assertThat(contextData).containsKeys("key1", "key2")
      .containsValues("value1", "value2")
  }

  @Test
  fun shouldCreateNewContextForEachCallToExecute() {
    var contextValue: String? = null
    val policy = Policy.retry<Unit>()
      .handle(ArithmeticException::class)
      .waitAndRetry(listOf(Duration.ofMillis(1))) { _, _, _, context ->
        contextValue = context.contextData["key"].toString()
      }

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
  fun shouldThrowWhenRetryCountIsLessThanZero() {
    assertThatThrownBy {
      Policy.retry<Unit>()
        .handle(ArithmeticException::class)
        .waitAndRetry(-1, { _, _, _ -> Duration.ZERO })
        { _, _, _, _ -> }
    }
      .isInstanceOf(IllegalArgumentException::class.java)
      .hasMessageContaining("Retry count")
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
      .waitAndRetry(
        5,
        { retryAttempt, _, _ ->
          Duration.ofMillis(
            2.0.pow(retryAttempt).toLong()
          )
        }) { _, duration, _, _ -> actualRetryWaits.add(duration) }

    policy.raiseExceptions(5) { ArithmeticException() }

    assertThat(actualRetryWaits).containsExactlyElementsOf(expectedRetryWaits)
  }

  @Test
  fun shouldBeAbleToPassHandledExceptionToSleepDurationProvider() {
    var capturedExceptionInstance: Any? = null
    val exceptionInstance = ArithmeticException()
    val policy = Policy.retry<Unit>()
      .handle(ArithmeticException::class)
      .waitAndRetry(5, { _, outcome, _ ->
        outcome.onFailure { capturedExceptionInstance = it }
        Duration.ZERO
      }) { _, _, _, _ -> }

    policy.raiseExceptions(1) { exceptionInstance }

    assertThat(capturedExceptionInstance).isSameAs(exceptionInstance)
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
      .waitAndRetry(
        2,
        { _, outcome, _ ->
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
      .waitAndRetry(1, { _, _, context ->
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

  @Test
  fun shouldNotCallOnRetryWhenRetryCountIsZero() {
    var retryInvoked = false
    val policy = Policy.retry<Unit>()
      .handle(ArithmeticException::class)
      .waitAndRetry(0, { _, _, _ -> Duration.ofMillis(1) })
      { _, _, _, _ -> retryInvoked = true }

    assertFailsWith(ArithmeticException::class) {
      policy.raiseExceptions(1) { ArithmeticException() }
    }
    assertThat(retryInvoked).isFalse()
  }
}
