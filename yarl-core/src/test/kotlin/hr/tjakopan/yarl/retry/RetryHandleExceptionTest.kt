package hr.tjakopan.yarl.retry

import hr.tjakopan.yarl.Context
import hr.tjakopan.yarl.Policy
import hr.tjakopan.yarl.test.helpers.raiseExceptions
import hr.tjakopan.yarl.test.helpers.raiseExceptionsOnExecuteAndCapture
import org.assertj.core.api.Assertions.*
import kotlin.test.Test
import kotlin.test.assertFailsWith

@Suppress("UsePropertyAccessSyntax")
class RetryHandleExceptionTest {
  @Test
  fun `should throw when retry count is less than zero`() {
    assertThatThrownBy {
      Policy.retry<Unit>()
        .handle(ArithmeticException::class)
        .retry(-1)
    }
      .isInstanceOf(IllegalArgumentException::class.java)
      .hasMessageContaining("Retry count")
  }

  @Test
  fun `should not throw when specified exception thrown same number of times as retry count`() {
    val policy = Policy.retry<Unit>()
      .handle(ArithmeticException::class)
      .retry(3)

    policy.raiseExceptions(3) { ArithmeticException() }
  }

  @Test
  fun `should not throw when one of the specified exceptions thrown same number of times as retry count`() {
    val policy = Policy.retry<Unit>()
      .handle(ArithmeticException::class)
      .handle(IllegalArgumentException::class)
      .retry(3)

    policy.raiseExceptions(3) { IllegalArgumentException() }
  }

  @Test
  fun `should not throw when specified exception thrown less number of times than retry count`() {
    val policy = Policy.retry<Unit>()
      .handle(ArithmeticException::class)
      .retry(3)

    policy.raiseExceptions(1) { ArithmeticException() }
  }

  @Test
  fun `should not throw when one of the specified exceptions thrown less number of times than retry count`() {
    val policy = Policy.retry<Unit>()
      .handle(ArithmeticException::class)
      .handle(IllegalArgumentException::class)
      .retry(3)

    policy.raiseExceptions(1) { IllegalArgumentException() }
  }

  @Test
  fun `should throw when specified exception thrown more times then retry count`() {
    val policy = Policy.retry<Unit>()
      .handle(ArithmeticException::class)
      .retry(3)

    assertThatExceptionOfType(ArithmeticException::class.java).isThrownBy {
      policy.raiseExceptions(3 + 1) { ArithmeticException() }
    }
  }

  @Test
  fun `should throw when one of the specified exceptions are thrown more times then retry count`() {
    val policy = Policy.retry<Unit>()
      .handle(ArithmeticException::class)
      .handle(IllegalArgumentException::class)
      .retry(3)

    assertThatExceptionOfType(IllegalArgumentException::class.java).isThrownBy {
      policy.raiseExceptions(3 + 1) { IllegalArgumentException() }
    }
  }

  @Test
  fun `should throw when exception thrown is not the specified exception type`() {
    val policy = Policy.retry<Unit>()
      .handle(ArithmeticException::class)
      .retry()

    assertThatExceptionOfType(NullPointerException::class.java).isThrownBy {
      policy.raiseExceptions(3 + 1) { NullPointerException() }
    }
  }

  @Test
  fun `should throw when exception thrown is not one of the specified exception types`() {
    val policy = Policy.retry<Unit>()
      .handle(ArithmeticException::class)
      .handle(IllegalArgumentException::class)
      .retry()

    assertThatExceptionOfType(NullPointerException::class.java).isThrownBy {
      policy.raiseExceptions(3 + 1) { NullPointerException() }
    }
  }

  @Test
  fun `should throw when specified exception predicate is not satisfied`() {
    val policy = Policy.retry<Unit>()
      .handle(ArithmeticException::class) { false }
      .retry()

    assertThatExceptionOfType(ArithmeticException::class.java).isThrownBy {
      policy.raiseExceptions(1) { ArithmeticException() }
    }
  }

  @Test
  fun `should throw when none of the specified exception predicates are satisfied`() {
    val policy = Policy.retry<Unit>()
      .handle(ArithmeticException::class) { false }
      .handle(IllegalArgumentException::class) { false }
      .retry()

    assertThatExceptionOfType(IllegalArgumentException::class.java).isThrownBy {
      policy.raiseExceptions(1) { IllegalArgumentException() }
    }
  }

  @Test
  fun `should not throw when specified exception predicate is satisfied`() {
    val policy = Policy.retry<Unit>()
      .handle(ArithmeticException::class) { true }
      .retry()

    policy.raiseExceptions(1) { ArithmeticException() }
  }

  @Test
  fun `should not throw when one of the specified exception predicates are satisfied`() {
    val policy = Policy.retry<Unit>()
      .handle(ArithmeticException::class) { true }
      .handle(IllegalArgumentException::class) { true }
      .retry()

    policy.raiseExceptions(1) { IllegalArgumentException() }
  }

  @Test
  fun `should call on retry on each retry with the current retry count`() {
    val expectedRetryCounts = listOf(1, 2, 3)
    val retryCounts = mutableListOf<Int>()
    val policy = Policy.retry<Unit>()
      .handle(ArithmeticException::class)
      .retry(3) { _, retryCount, _ -> retryCounts.add(retryCount) }

    policy.raiseExceptions(3) { ArithmeticException() }

    assertThat(retryCounts).containsExactlyElementsOf(expectedRetryCounts)
  }

  @Test
  fun `should call on retry on each retry with the current exception`() {
    val expectedExceptions = listOf("Exception #1", "Exception #2", "Exception #3")
    val retryExceptions = mutableListOf<Throwable>()
    val policy = Policy.retry<Unit>()
      .handle(ArithmeticException::class)
      .retry(3) { outcome, _, _ ->
        outcome.onFailure { retryExceptions.add(it) }
      }

    policy.raiseExceptions(3) { ArithmeticException("Exception #$it") }

    assertThat(retryExceptions.map { e -> e.message }).containsExactlyElementsOf(expectedExceptions)
  }

  @Test
  fun `should call on retry with aHandled cause exception`() {
    var passedToOnRetry: Throwable? = null
    val policy = Policy.retry<Unit>()
      .handleCause(ArithmeticException::class)
      .retry(3) { outcome, _, _ ->
        outcome.onFailure { passedToOnRetry = it }
      }
    val toRaiseAsInner = ArithmeticException()
    val withInner = Exception(toRaiseAsInner)

    policy.raiseExceptions(1) { withInner }

    assertThat(passedToOnRetry).isSameAs(toRaiseAsInner)
  }

  @Test
  fun `should not call on retry when no retries are performed`() {
    var retryCount = 0
    val policy = Policy.retry<Unit>()
      .handle(ArithmeticException::class)
      .retry { _, _, _ -> retryCount++ }

    assertFailsWith(IllegalArgumentException::class) {
      policy.raiseExceptions(1) { IllegalArgumentException() }
    }
    assertThat(retryCount).isEqualTo(0)
  }

  @Test
  fun `should call on retry with the passed context`() {
    var context: Context? = null
    val policy = Policy.retry<Unit>()
      .handle(ArithmeticException::class)
      .retry { _, _, ctx -> context = ctx }

    policy.raiseExceptions(
      Context(mapOf("key1" to "value1", "key2" to "value2")),
      1
    ) { ArithmeticException() }

    @Suppress("UsePropertyAccessSyntax")
    assertThat(context).isNotNull()
    assertThat(context).containsKeys("key1", "key2")
      .containsValues("value1", "value2")
  }

  @Test
  fun `should call on retry with the passed context when execute and capture`() {
    var context: Context? = null
    val policy = Policy.retry<Unit>()
      .handle(ArithmeticException::class)
      .retry { _, _, ctx -> context = ctx }

    policy.raiseExceptionsOnExecuteAndCapture(
      Context(mapOf("key1" to "value1", "key2" to "value2")),
      1
    ) { ArithmeticException() }

    assertThat(context).isNotNull
    assertThat(context).containsKeys("key1", "key2")
      .containsValues("value1", "value2")
  }

  @Test
  fun `context should be empty if execute not called with any data`() {
    var capturedContext: Context? = null
    val policy = Policy.retry<Unit>()
      .handle(ArithmeticException::class)
      .retry { _, _, context -> capturedContext = context }

    policy.raiseExceptions(1) { ArithmeticException() }

    assertThat(capturedContext).isEmpty()
  }

  @Test
  fun `should create new context for each call to execute`() {
    var contextValue: String? = null
    val policy = Policy.retry<Unit>()
      .handle(ArithmeticException::class)
      .retry { _, _, context -> contextValue = context["key"].toString() }

    policy.raiseExceptions(Context(mapOf("key" to "original_value")), 1) { ArithmeticException() }

    assertThat(contextValue).isEqualTo("original_value")

    policy.raiseExceptions(Context(mapOf("key" to "new_value")), 1) { ArithmeticException() }

    assertThat(contextValue).isEqualTo("new_value")
  }

  @Test
  fun `should create new context for each call to execute and capture`() {
    var contextValue: String? = null
    val policy = Policy.retry<Unit>()
      .handle(ArithmeticException::class)
      .retry { _, _, context -> contextValue = context["key"].toString() }

    policy.raiseExceptionsOnExecuteAndCapture(
      Context(mapOf("key" to "original_value")),
      1
    ) { ArithmeticException() }

    assertThat(contextValue).isEqualTo("original_value")

    policy.raiseExceptionsOnExecuteAndCapture(
      Context(mapOf("key" to "new_value")),
      1
    ) { ArithmeticException() }

    assertThat(contextValue).isEqualTo("new_value")
  }

  @Test
  fun `should create new state for each call to policy`() {
    val policy = Policy.retry<Unit>()
      .handle(ArithmeticException::class)
      .retry()

    policy.raiseExceptions(1) { ArithmeticException() }
    policy.raiseExceptions(1) { ArithmeticException() }
  }

  @Test
  fun `should not call on retry when retry count is zero`() {
    var retryInvoked = false
    val policy = Policy.retry<Unit>()
      .handle(ArithmeticException::class)
      .retry(0) { _, _, _ -> retryInvoked = true }

    assertFailsWith(ArithmeticException::class) {
      policy.raiseExceptions(1) { ArithmeticException() }
    }
    assertThat(retryInvoked).isFalse()
  }
}
