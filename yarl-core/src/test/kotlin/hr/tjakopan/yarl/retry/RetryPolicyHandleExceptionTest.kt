package hr.tjakopan.yarl.retry

import hr.tjakopan.yarl.Policy
import hr.tjakopan.yarl.test.helpers.raiseExceptions
import org.assertj.core.api.Assertions.*
import kotlin.test.Test

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
}
