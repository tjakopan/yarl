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

@Suppress("UsePropertyAccessSyntax")
class WaitAndRetryHandleExceptionTest {
  @Test
  fun `should not throw when specified exception thrown same number of times as there are sleep durations`() {
    val policy = Policy.retry<Unit>()
      .handle(ArithmeticException::class)
      .waitAndRetry(listOf(Duration.ofMillis(1), Duration.ofMillis(2), Duration.ofMillis(3)))

    policy.raiseExceptions(1) { ArithmeticException() }
  }

  @Test
  fun `should not throw when one of the specified exceptions thrown same number of times as there are sleep durations`() {
    val policy = Policy.retry<Unit>()
      .handle(ArithmeticException::class)
      .handle(IllegalArgumentException::class)
      .waitAndRetry(listOf(Duration.ofMillis(1), Duration.ofMillis(2), Duration.ofMillis(3)))

    policy.raiseExceptions(1) { IllegalArgumentException() }
  }

  @Test
  fun `should not throw when specified exception thrown less number of times than there are sleep durations`() {
    val policy = Policy.retry<Unit>()
      .handle(ArithmeticException::class)
      .waitAndRetry(listOf(Duration.ofMillis(1), Duration.ofMillis(2), Duration.ofMillis(3)))

    policy.raiseExceptions(2) { ArithmeticException() }
  }

  @Test
  fun `should not throw when one of the specified exceptions thrown less number then times as there are sleep durations`() {
    val policy = Policy.retry<Unit>()
      .handle(ArithmeticException::class)
      .handle(IllegalArgumentException::class)
      .waitAndRetry(listOf(Duration.ofMillis(1), Duration.ofMillis(2), Duration.ofMillis(3)))

    policy.raiseExceptions(2) { IllegalArgumentException() }
  }

  @Test
  fun `should throw when specified exception thrown more times than there are sleep durations`() {
    val policy = Policy.retry<Unit>()
      .handle(ArithmeticException::class)
      .waitAndRetry(listOf(Duration.ofMillis(1), Duration.ofMillis(2), Duration.ofMillis(3)))

    assertFailsWith(ArithmeticException::class) {
      policy.raiseExceptions(3 + 1) { ArithmeticException() }
    }
  }

  @Test
  fun `should throw when one of the specified exceptions are thrown more times there are sleep durations`() {
    val policy = Policy.retry<Unit>()
      .handle(ArithmeticException::class)
      .handle(IllegalArgumentException::class)
      .waitAndRetry(listOf(Duration.ofMillis(1), Duration.ofMillis(2), Duration.ofMillis(3)))

    assertFailsWith(IllegalArgumentException::class) {
      policy.raiseExceptions(3 + 1) { IllegalArgumentException() }
    }
  }

  @Test
  fun `should throw when exception thrown is not the specified exception type`() {
    val policy = Policy.retry<Unit>()
      .handle(ArithmeticException::class)
      .waitAndRetry(listOf())

    assertFailsWith(NullPointerException::class) {
      policy.raiseExceptions(1) { NullPointerException() }
    }
  }

  @Test
  fun `should throw when exception thrown is not one of the specified exception types`() {
    val policy = Policy.retry<Unit>()
      .handle(ArithmeticException::class)
      .handle(IllegalArgumentException::class)
      .waitAndRetry(listOf())

    assertFailsWith(NullPointerException::class) {
      policy.raiseExceptions(1) { NullPointerException() }
    }
  }

  @Test
  fun `should throw when specified exception predicate is not satisfied`() {
    val policy = Policy.retry<Unit>()
      .handle(ArithmeticException::class) { false }
      .waitAndRetry(listOf())

    assertFailsWith(ArithmeticException::class) {
      policy.raiseExceptions(1) { ArithmeticException() }
    }
  }

  @Test
  fun `should throw when none of the specified exception predicates are satisfied`() {
    val policy = Policy.retry<Unit>()
      .handle(ArithmeticException::class) { false }
      .handle(IllegalArgumentException::class) { false }
      .waitAndRetry(listOf())

    assertFailsWith(IllegalArgumentException::class) {
      policy.raiseExceptions(1) { IllegalArgumentException() }
    }
  }

  @Test
  fun `should not throw when specified exception predicate is satisfied`() {
    val policy = Policy.retry<Unit>()
      .handle(ArithmeticException::class) { true }
      .waitAndRetry(listOf(Duration.ofMillis(1)))

    policy.raiseExceptions(1) { ArithmeticException() }
  }

  @Test
  fun `should not throw when one of the specified exception predicates are satisfied`() {
    val policy = Policy.retry<Unit>()
      .handle(IllegalArgumentException::class) { true }
      .waitAndRetry(listOf(Duration.ofMillis(1)))

    policy.raiseExceptions(1) { IllegalArgumentException() }
  }

  @Test
  fun `should call on retry on each retry with the current duration`() {
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
  fun `should call on retry on each retry with the current exception`() {
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
  fun `should call on retry on each retry with the current retry count`() {
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
  fun `should not call on retry when no retries are performed`() {
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
  fun `should create new state for each call to policy`() {
    val policy = Policy.retry<Unit>()
      .handle(ArithmeticException::class)
      .waitAndRetry(listOf(Duration.ofMillis(1)))

    policy.raiseExceptions(1) { ArithmeticException() }

    policy.raiseExceptions(1) { ArithmeticException() }
  }

  @Test
  fun `should call on retry with the passed context`() {
    var contextData: MutableMap<String, Any>? = null
    val policy = Policy.retry<Unit>()
      .handle(ArithmeticException::class)
      .waitAndRetry(
        listOf(
          Duration.ofMillis(1),
          Duration.ofMillis(2),
          Duration.ofMillis(3)
        )
      ) { _, _, _, context -> contextData = context }

    policy.raiseExceptions(
      Context(mapOf("key1" to "value1", "key2" to "value2")),
      1
    ) { ArithmeticException() }

    assertThat(contextData).containsKeys("key1", "key2")
      .containsValues("value1", "value2")
  }

  @Test
  fun `should create new context for each call to execute`() {
    var contextValue: String? = null
    val policy = Policy.retry<Unit>()
      .handle(ArithmeticException::class)
      .waitAndRetry(listOf(Duration.ofMillis(1))) { _, _, _, context ->
        contextValue = context["key"].toString()
      }

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
  fun `should throw when retry count is less than zero`() {
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
  fun `should be able to pass handled exception to sleep duration provider`() {
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
  fun `should be able to calculate retry durations based on the handled fault`() {
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
  fun `should be able to pass retry duration from execution to sleep duration provider via context`() {
    val expectedRetryDuration = Duration.ofMillis(1)
    var actualRetryDuration: Duration? = null
    val defaultRetryAfter = Duration.ofMillis(30)
    val policy = Policy.retry<Unit>()
      .handle(ArithmeticException::class)
      .waitAndRetry(1, { _, _, context ->
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

  @Test
  fun `should not call on retry when retry count is zero`() {
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
