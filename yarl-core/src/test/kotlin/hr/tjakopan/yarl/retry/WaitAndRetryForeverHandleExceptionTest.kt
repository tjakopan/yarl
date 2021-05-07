package hr.tjakopan.yarl.retry

import hr.tjakopan.yarl.Context
import hr.tjakopan.yarl.Policy
import hr.tjakopan.yarl.test.helpers.raiseExceptions
import org.assertj.core.api.Assertions.assertThat
import java.time.Duration
import kotlin.math.pow
import kotlin.test.Test
import kotlin.test.assertFailsWith

@Suppress("UsePropertyAccessSyntax")
class WaitAndRetryForeverHandleExceptionTest {
  @Test
  fun `should not throw regardless of how many times the specified exception is raised`() {
    val policy = Policy.retry<Unit>()
      .handle(ArithmeticException::class)
      .waitAndRetryForever { _, _, _ -> Duration.ZERO }

    policy.raiseExceptions(3) { ArithmeticException() }
  }

  @Test
  fun `should not throw regardless of how many times one of the specified exception is raised`() {
    val policy = Policy.retry<Unit>()
      .handle(ArithmeticException::class)
      .handle(IllegalArgumentException::class)
      .waitAndRetryForever { _, _, _ -> Duration.ZERO }

    policy.raiseExceptions(3) { IllegalArgumentException() }
  }

  @Test
  fun `should throw when exception thrown is not the specified exception type`() {
    val policy = Policy.retry<Unit>()
      .handle(ArithmeticException::class)
      .waitAndRetryForever { _, _, _ -> Duration.ZERO }

    assertFailsWith(NullPointerException::class) {
      policy.raiseExceptions(1) { NullPointerException() }
    }
  }

  @Test
  fun `should throw when exception thrown is not one of the specified exception types`() {
    val policy = Policy.retry<Unit>()
      .handle(ArithmeticException::class)
      .handle(IllegalArgumentException::class)
      .waitAndRetryForever { _, _, _ -> Duration.ZERO }

    assertFailsWith(NullPointerException::class) {
      policy.raiseExceptions(1) { NullPointerException() }
    }
  }

  @Test
  fun `should throw when specified exception predicate is not satisfied`() {
    val policy = Policy.retry<Unit>()
      .handle(ArithmeticException::class) { false }
      .waitAndRetryForever { _, _, _ -> Duration.ZERO }

    assertFailsWith(ArithmeticException::class) {
      policy.raiseExceptions(1) { ArithmeticException() }
    }
  }

  @Test
  fun `should throw when none of the specified exception predicates are satisfied`() {
    val policy = Policy.retry<Unit>()
      .handle(ArithmeticException::class) { false }
      .handle(IllegalArgumentException::class) { false }
      .waitAndRetryForever { _, _, _ -> Duration.ZERO }

    assertFailsWith(IllegalArgumentException::class) {
      policy.raiseExceptions(1) { IllegalArgumentException() }
    }
  }

  @Test
  fun `should not throw when specified exception predicate is satisfied`() {
    val policy = Policy.retry<Unit>()
      .handle(ArithmeticException::class) { true }
      .waitAndRetryForever { _, _, _ -> Duration.ofMillis(1) }

    policy.raiseExceptions(1) { ArithmeticException() }
  }

  @Test
  fun `should not throw when one of the specified exception predicates are satisfied`() {
    val policy = Policy.retry<Unit>()
      .handle(IllegalArgumentException::class) { true }
      .waitAndRetryForever { _, _, _ -> Duration.ofMillis(1) }

    policy.raiseExceptions(1) { IllegalArgumentException() }
  }

  @Test
  fun `should call on retry on each retry with the current exception`() {
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
  fun `should call on retry on each retry with the current retry count`() {
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
  fun `should not call on retry when no retries are performed`() {
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
  fun `should create new context for each call to execute`() {
    var contextValue: String? = null
    val policy = Policy.retry<Unit>()
      .handle(ArithmeticException::class)
      .waitAndRetryForever({ _, _, _ -> Duration.ofMillis(1) })
      { _, _, _, context -> contextValue = context["key"].toString() }

    policy.raiseExceptions(Context(mapOf("key" to "original_value")), 1) {
      ArithmeticException()
    }

    assertThat(contextValue).isEqualTo("original_value")

    policy.raiseExceptions(Context(mapOf("key" to "new_value")), 1) {
      ArithmeticException()
    }

    assertThat(contextValue).isEqualTo("new_value")
  }

  @Test
  fun `should calculate retry durations from current retry attempt and duration provider`() {
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
  fun `should be able to calculate retry durations based on the handled fault`() {
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
  fun `should be able to pass retry duration from execution to sleep duration provider via context`() {
    val expectedRetryDuration = Duration.ofMillis(1)
    var actualRetryDuration: Duration? = null
    val defaultRetryAfter = Duration.ofMillis(30)
    val policy = Policy.retry<Unit>()
      .handle(ArithmeticException::class)
      .waitAndRetryForever({ _, _, context ->
        when (context.containsKey("RetryAfter")) {
          true -> context["RetryAfter"] as Duration
          else -> defaultRetryAfter
        }
      }) { _, duration, _, _ -> actualRetryDuration = duration }

    var failedOnce = false
    policy.execute(mapOf<String, Any>("RetryAfter" to defaultRetryAfter)) { context: Context ->
      context["RetryAfter"] = expectedRetryDuration
      if (!failedOnce) {
        failedOnce = true
        throw ArithmeticException()
      }
    }

    assertThat(actualRetryDuration).isEqualTo(expectedRetryDuration)
  }
}
