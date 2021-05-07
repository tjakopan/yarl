package hr.tjakopan.yarl.retry

import hr.tjakopan.yarl.Context
import hr.tjakopan.yarl.Policy
import hr.tjakopan.yarl.test.helpers.raiseExceptions
import org.assertj.core.api.Assertions.assertThat
import kotlin.test.Test
import kotlin.test.assertFailsWith

class RetryForeverTest {
  @Test
  fun `should not throw regardless of how many times the specified exception is raised`() {
    val policy = Policy.retry<Unit>()
      .handle(ArithmeticException::class)
      .retryForever()

    policy.raiseExceptions(3) { ArithmeticException() }
  }

  @Test
  fun `should not throw regardless of how many times one of the specified exception is raised`() {
    val policy = Policy.retry<Unit>()
      .handle(ArithmeticException::class)
      .handle(IllegalArgumentException::class)
      .retryForever()

    policy.raiseExceptions(3) { IllegalArgumentException() }
  }

  @Test
  fun `should throw when exception thrown is not the specified exception type`() {
    val policy = Policy.retry<Unit>()
      .handle(ArithmeticException::class)
      .retryForever()

    assertFailsWith(NullPointerException::class) {
      policy.raiseExceptions(3) { NullPointerException() }
    }
  }

  @Test
  fun `should throw when exception thrown is not one of the specified exception types`() {
    val policy = Policy.retry<Unit>()
      .handle(ArithmeticException::class)
      .handle(IllegalArgumentException::class)
      .retryForever()

    assertFailsWith(NullPointerException::class) {
      policy.raiseExceptions(3) { NullPointerException() }
    }
  }

  @Test
  fun `should throw when specified exception predicate is not satisfied`() {
    val policy = Policy.retry<Unit>()
      .handle(ArithmeticException::class) { false }
      .retryForever()

    assertFailsWith(ArithmeticException::class) {
      policy.raiseExceptions(1) { ArithmeticException() }
    }
  }

  @Test
  fun `should throw when none of the specified exception predicates are satisfied`() {
    val policy = Policy.retry<Unit>()
      .handle(ArithmeticException::class) { false }
      .handle(IllegalArgumentException::class) { false }
      .retryForever()

    assertFailsWith(IllegalArgumentException::class) {
      policy.raiseExceptions(1) { IllegalArgumentException() }
    }
  }

  @Test
  fun `should not throw when specified exception predicate is satisfied`() {
    val policy = Policy.retry<Unit>()
      .handle(ArithmeticException::class) { true }
      .retryForever()

    policy.raiseExceptions(1) { ArithmeticException() }
  }

  @Test
  fun `should not throw when one of the specified exception predicates are satisfied`() {
    val policy = Policy.retry<Unit>()
      .handle(ArithmeticException::class) { true }
      .handle(IllegalArgumentException::class) { true }
      .retryForever()

    policy.raiseExceptions(1) { IllegalArgumentException() }
  }

  @Test
  fun `should call on retry on each retry with the current exception`() {
    val expectedExceptions = listOf("Exception #1", "Exception #2", "Exception #3")
    val retryExceptions = mutableListOf<Throwable>()
    val policy = Policy.retry<Unit>()
      .handle(ArithmeticException::class)
      .retryForever { outcome, _, _ ->
        outcome.onFailure { retryExceptions.add(it) }
      }

    policy.raiseExceptions(3) { ArithmeticException("Exception #$it") }

    assertThat(retryExceptions.map { e -> e.message }).containsExactlyElementsOf(expectedExceptions)
  }

  @Test
  fun `should call on retry with the passed context`() {
    var context: Context? = null
    val policy = Policy.retry<Unit>()
      .handle(ArithmeticException::class)
      .retryForever { _, _, ctx -> context = ctx }

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
  fun `should call on retry on each retry with the current retry count`() {
    val expectedRetryCounts = listOf(1, 2, 3)
    val retryCounts = mutableListOf<Int>()
    val policy = Policy.retry<Unit>()
      .handle(ArithmeticException::class)
      .retryForever { _, retryCount, _ -> retryCounts.add(retryCount) }

    policy.raiseExceptions(3) { ArithmeticException() }

    assertThat(retryCounts).containsExactlyElementsOf(expectedRetryCounts)
  }

  @Test
  fun `should not call on retry when no retries are performed`() {
    var retryCount = 0
    val policy = Policy.retry<Unit>()
      .handle(ArithmeticException::class)
      .retryForever { _, _, _ -> retryCount++ }

    assertFailsWith(IllegalArgumentException::class) {
      policy.raiseExceptions(1) { IllegalArgumentException() }
    }
    assertThat(retryCount).isEqualTo(0)
  }

  @Test
  fun `should create new context for each call to execute`() {
    var contextValue: String? = null
    val policy = Policy.retry<Unit>()
      .handle(ArithmeticException::class)
      .retryForever { _, _, context -> contextValue = context["key"].toString() }

    policy.raiseExceptions(Context(mapOf("key" to "original_value")), 1) { ArithmeticException() }

    assertThat(contextValue).isEqualTo("original_value")

    policy.raiseExceptions(Context(mapOf("key" to "new_value")), 1) { ArithmeticException() }

    assertThat(contextValue).isEqualTo("new_value")
  }
}
