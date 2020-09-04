package hr.tjakopan.yarl.retry

import hr.tjakopan.yarl.Context
import hr.tjakopan.yarl.Policy
import hr.tjakopan.yarl.test.helpers.raiseExceptions
import org.assertj.core.api.Assertions.*
import kotlin.test.Test
import kotlin.test.assertFailsWith

class RetryPolicyHandleExceptionTest {
  @Test
  fun shouldThrowWhenRetryCountIsLessThanZero() {
    assertThatThrownBy {
      Policy.retry<Unit>()
        .handle(ArithmeticException::class)
        .retry(-1)
    }
      .isInstanceOf(IllegalArgumentException::class.java)
      .hasMessageContaining("Retry count")
  }

  @Test
  fun shouldNotThrowWhenSpecifiedExceptionThrownSameNumberOfTimesAsRetryCount() {
    val policy = Policy.retry<Unit>()
      .handle(ArithmeticException::class)
      .retry(3)

    policy.raiseExceptions(3) { ArithmeticException() }
  }

  @Test
  fun shouldNotThrowWhenOneOfTheSpecifiedExceptionsThrownSameNumberOfTimesAsRetryCount() {
    val policy = Policy.retry<Unit>()
      .handle(ArithmeticException::class)
      .handle(IllegalArgumentException::class)
      .retry(3)

    policy.raiseExceptions(3) { IllegalArgumentException() }
  }

  @Test
  fun shouldNotThrowWhenSpecifiedExceptionThrownLessNumberOfTimesThanRetryCount() {
    val policy = Policy.retry<Unit>()
      .handle(ArithmeticException::class)
      .retry(3)

    policy.raiseExceptions(1) { ArithmeticException() }
  }

  @Test
  fun shouldNotThrowWhenOneOfTheSpecifiedExceptionsThrownLessNumberOfTimesThanRetryCount() {
    val policy = Policy.retry<Unit>()
      .handle(ArithmeticException::class)
      .handle(IllegalArgumentException::class)
      .retry(3)

    policy.raiseExceptions(1) { IllegalArgumentException() }
  }

  @Test
  fun shouldThrowWhenSpecifiedExceptionThrownMoreTimesThenRetryCount() {
    val policy = Policy.retry<Unit>()
      .handle(ArithmeticException::class)
      .retry(3)

    assertThatExceptionOfType(ArithmeticException::class.java).isThrownBy {
      policy.raiseExceptions(3 + 1) { ArithmeticException() }
    }
  }

  @Test
  fun shouldThrowWhenOneOfTheSpecifiedExceptionsAreThrownMoreTimesThenRetryCount() {
    val policy = Policy.retry<Unit>()
      .handle(ArithmeticException::class)
      .handle(IllegalArgumentException::class)
      .retry(3)

    assertThatExceptionOfType(IllegalArgumentException::class.java).isThrownBy {
      policy.raiseExceptions(3 + 1) { IllegalArgumentException() }
    }
  }

  @Test
  fun shouldThrowWhenExceptionThrownIsNotTheSpecifiedExceptionType() {
    val policy = Policy.retry<Unit>()
      .handle(ArithmeticException::class)
      .retry()

    assertThatExceptionOfType(NullPointerException::class.java).isThrownBy {
      policy.raiseExceptions(3 + 1) { NullPointerException() }
    }
  }

  @Test
  fun shouldThrowWhenExceptionThrownIsNotOneOfTheSpecifiedExceptionTypes() {
    val policy = Policy.retry<Unit>()
      .handle(ArithmeticException::class)
      .handle(IllegalArgumentException::class)
      .retry()

    assertThatExceptionOfType(NullPointerException::class.java).isThrownBy {
      policy.raiseExceptions(3 + 1) { NullPointerException() }
    }
  }

  @Test
  fun shouldThrowWhenSpecifiedExceptionPredicateIsNotSatisfied() {
    val policy = Policy.retry<Unit>()
      .handle(ArithmeticException::class) { false }
      .retry()

    assertThatExceptionOfType(ArithmeticException::class.java).isThrownBy {
      policy.raiseExceptions(1) { ArithmeticException() }
    }
  }

  @Test
  fun shouldThrowWhenNoneOfTheSpecifiedExceptionPredicatesAreSatisfied() {
    val policy = Policy.retry<Unit>()
      .handle(ArithmeticException::class) { false }
      .handle(IllegalArgumentException::class) { false }
      .retry()

    assertThatExceptionOfType(IllegalArgumentException::class.java).isThrownBy {
      policy.raiseExceptions(1) { IllegalArgumentException() }
    }
  }

  @Test
  fun shouldNotThrowWhenSpecifiedExceptionPredicateIsSatisfied() {
    val policy = Policy.retry<Unit>()
      .handle(ArithmeticException::class) { true }
      .retry()

    policy.raiseExceptions(1) { ArithmeticException() }
  }

  @Test
  fun shouldNotThrowWhenOneOfTheSpecifiedExceptionPredicatesAreSatisfied() {
    val policy = Policy.retry<Unit>()
      .handle(ArithmeticException::class) { true }
      .handle(IllegalArgumentException::class) { true }
      .retry()

    policy.raiseExceptions(1) { IllegalArgumentException() }
  }

  @Test
  fun shouldCallOnRetryOnEachRetryWithTheCurrentRetryCount() {
    val expectedRetryCounts = listOf(1, 2, 3)
    val retryCounts = mutableListOf<Int>()
    val policy = Policy.retry<Unit>()
      .handle(ArithmeticException::class)
      .retry(3) { _, retryCount, _ -> retryCounts.add(retryCount) }

    policy.raiseExceptions(3) { ArithmeticException() }

    assertThat(retryCounts).containsExactlyElementsOf(expectedRetryCounts)
  }

  @Test
  fun shouldCallOnRetryOnEachRetryWithTheCurrentException() {
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
  fun shouldCallOnRetryWithAHandledCauseException() {
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
  fun shouldNotCallOnRetryWhenNoRetriesArePerformed() {
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
  fun shouldCallOnRetryWithThePassedContext() {
    var context: Context? = null
    val policy = Policy.retry<Unit>()
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
}
