package hr.tjakopan.yarl.retry

import hr.tjakopan.yarl.Context
import hr.tjakopan.yarl.Policy
import hr.tjakopan.yarl.test.helpers.raiseExceptions
import org.assertj.core.api.Assertions.assertThat
import kotlin.test.Test
import kotlin.test.assertFailsWith

class RetryForeverTest {
  @Test
  fun shouldNotThrowRegardlessOfHowManyTimesTheSpecifiedExceptionIsRaised() {
    val policy = Policy.retry<Unit>()
      .handle(ArithmeticException::class)
      .retryForever()

    policy.raiseExceptions(3) { ArithmeticException() }
  }

  @Test
  fun shouldNotThrowRegardlessOfHowManyTimesOneOfTheSpecifiedExceptionIsRaised() {
    val policy = Policy.retry<Unit>()
      .handle(ArithmeticException::class)
      .handle(IllegalArgumentException::class)
      .retryForever()

    policy.raiseExceptions(3) { IllegalArgumentException() }
  }

  @Test
  fun shouldThrowWhenExceptionThrownIsNotTheSpecifiedExceptionType() {
    val policy = Policy.retry<Unit>()
      .handle(ArithmeticException::class)
      .retryForever()

    assertFailsWith(NullPointerException::class) {
      policy.raiseExceptions(3) { NullPointerException() }
    }
  }

  @Test
  fun shouldThrowWhenExceptionThrownIsNotOneOfTheSpecifiedExceptionTypes() {
    val policy = Policy.retry<Unit>()
      .handle(ArithmeticException::class)
      .handle(IllegalArgumentException::class)
      .retryForever()

    assertFailsWith(NullPointerException::class) {
      policy.raiseExceptions(3) { NullPointerException() }
    }
  }

  @Test
  fun shouldThrowWhenSpecifiedExceptionPredicateIsNotSatisfied() {
    val policy = Policy.retry<Unit>()
      .handle(ArithmeticException::class) { false }
      .retryForever()

    assertFailsWith(ArithmeticException::class) {
      policy.raiseExceptions(1) { ArithmeticException() }
    }
  }

  @Test
  fun shouldThrowWhenNoneOfTheSpecifiedExceptionPredicatesAreSatisfied() {
    val policy = Policy.retry<Unit>()
      .handle(ArithmeticException::class) { false }
      .handle(IllegalArgumentException::class) { false }
      .retryForever()

    assertFailsWith(IllegalArgumentException::class) {
      policy.raiseExceptions(1) { IllegalArgumentException() }
    }
  }

  @Test
  fun shouldNotThrowWhenSpecifiedExceptionPredicateIsSatisfied() {
    val policy = Policy.retry<Unit>()
      .handle(ArithmeticException::class) { true }
      .retryForever()

    policy.raiseExceptions(1) { ArithmeticException() }
  }

  @Test
  fun shouldNotThrowWhenOneOfTheSpecifiedExceptionPredicatesAreSatisfied() {
    val policy = Policy.retry<Unit>()
      .handle(ArithmeticException::class) { true }
      .handle(IllegalArgumentException::class) { true }
      .retryForever()

    policy.raiseExceptions(1) { IllegalArgumentException() }
  }

  @Test
  fun shouldCallOnRetryOnEachRetryWithTheCurrentException() {
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
  fun shouldCallOnRetryWithThePassedContext() {
    var context: Context? = null
    val policy = Policy.retry<Unit>()
      .handle(ArithmeticException::class)
      .retryForever { _, _, ctx -> context = ctx }

    policy.raiseExceptions(
      Context(contextData = mutableMapOf("key1" to "value1", "key2" to "value2")),
      1
    ) { ArithmeticException() }

    @Suppress("UsePropertyAccessSyntax")
    assertThat(context).isNotNull()
    assertThat(context?.contextData).containsKeys("key1", "key2")
      .containsValues("value1", "value2")
  }

  @Test
  fun shouldCallOnRetryOnEachRetryWithTheCurrentRetryCount() {
    val expectedRetryCounts = listOf(1, 2, 3)
    val retryCounts = mutableListOf<Int>()
    val policy = Policy.retry<Unit>()
      .handle(ArithmeticException::class)
      .retryForever { _, retryCount, _ -> retryCounts.add(retryCount) }

    policy.raiseExceptions(3) { ArithmeticException() }

    assertThat(retryCounts).containsExactlyElementsOf(expectedRetryCounts)
  }

  @Test
  fun shouldNotCallOnRetryWhenNoRetriesArePerformed() {
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
  fun shouldCreateNewContextForEachCallToExecute() {
    var contextValue: String? = null
    val policy = Policy.retry<Unit>()
      .handle(ArithmeticException::class)
      .retryForever { _, _, context -> contextValue = context.contextData["key"].toString() }

    policy.raiseExceptions(Context(contextData = mutableMapOf("key" to "original_value")), 1) { ArithmeticException() }

    assertThat(contextValue).isEqualTo("original_value")

    policy.raiseExceptions(Context(contextData = mutableMapOf("key" to "new_value")), 1) { ArithmeticException() }

    assertThat(contextValue).isEqualTo("new_value")
  }
}
