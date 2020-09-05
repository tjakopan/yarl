package hr.tjakopan.yarl.retry

import hr.tjakopan.yarl.Policy
import hr.tjakopan.yarl.test.helpers.TestResult
import hr.tjakopan.yarl.test.helpers.TestResultClass
import hr.tjakopan.yarl.test.helpers.raiseResults
import hr.tjakopan.yarl.test.helpers.raiseResultsAndOrExceptions
import org.assertj.core.api.Assertions.assertThat
import kotlin.test.Test
import kotlin.test.assertFailsWith

class RetryPolicyHandleMixedTest {
  @Test
  fun shouldHandleExceptionWhenHandlingExceptionsOnly() {
    val policy = Policy.retry<TestResult>()
      .handle(ArithmeticException::class)
      .retry(1)

    val result = policy.raiseResultsAndOrExceptions(ArithmeticException(), TestResult.GOOD)

    assertThat(result).isEqualTo(TestResult.GOOD)
  }

  @Test
  fun shouldThrowUnhandledExceptionWhenHandlingExceptionsOnly() {
    val policy = Policy.retry<TestResult>()
      .handle(ArithmeticException::class)
      .retry(1)

    assertFailsWith(IllegalArgumentException::class) {
      policy.raiseResultsAndOrExceptions(IllegalArgumentException(), TestResult.GOOD)
    }
  }

  @Test
  fun shouldHandleBothExceptionAndSpecifiedResultIfRaisedSameNumberOfTimesAsRetryCountWhenConfiguringResultsBeforeExceptions() {
    val policy = Policy.retry<TestResult>()
      .handleResult(TestResult.FAULT)
      .handle(ArithmeticException::class)
      .retry(2)

    val result = policy.raiseResultsAndOrExceptions(TestResult.FAULT, ArithmeticException(), TestResult.GOOD)

    assertThat(result).isEqualTo(TestResult.GOOD)
  }

  @Test
  fun shouldHandleBothExceptionAndSpecifiedResultIfRaisedSameNumberOfTimesAsRetryCountWhenConfiguringExceptionBeforeResult() {
    val policy = Policy.retry<TestResult>()
      .handle(ArithmeticException::class)
      .handleResult(TestResult.FAULT)
      .retry(2)

    val result = policy.raiseResultsAndOrExceptions(TestResult.FAULT, ArithmeticException(), TestResult.GOOD)

    assertThat(result).isEqualTo(TestResult.GOOD)
  }

  @Test
  fun shouldHandleBothExceptionsAndSpecifiedResultsIfRaisedSameNumberOfTimesAsRetryCountMixingExceptionsAndResultsSpecifyingExceptionsFirst() {
    val policy = Policy.retry<TestResult>()
      .handle(ArithmeticException::class)
      .handleResult(TestResult.FAULT)
      .handle(IllegalArgumentException::class)
      .handleResult(TestResult.FAULT_AGAIN)
      .retry(4)

    val result = policy.raiseResultsAndOrExceptions(
      TestResult.FAULT,
      ArithmeticException(),
      IllegalArgumentException(),
      TestResult.FAULT_AGAIN,
      TestResult.GOOD
    )

    assertThat(result).isEqualTo(TestResult.GOOD)
  }

  @Test
  fun shouldHandleBothExceptionsAndSpecifiedResultsIfRaisedSameNumberOfTimesAsRetryCountMixingExceptionsAndResultsSpecifyingResultsFirst() {
    val policy = Policy.retry<TestResult>()
      .handleResult(TestResult.FAULT)
      .handle(ArithmeticException::class)
      .handleResult(TestResult.FAULT_AGAIN)
      .handle(IllegalArgumentException::class)
      .retry(4)

    val result = policy.raiseResultsAndOrExceptions(
      TestResult.FAULT,
      ArithmeticException(),
      IllegalArgumentException(),
      TestResult.FAULT_AGAIN,
      TestResult.GOOD
    )

    assertThat(result).isEqualTo(TestResult.GOOD)
  }

  @Test
  fun shouldReturnHandledResultWhenHandledResultReturnedNextAfterRetriesExhaustHandlingBothExceptionsAndSpecifiedResultsMixingExceptionsAndResultsSpecifyingResultsFirst() {
    val policy = Policy.retry<TestResult>()
      .handleResult(TestResult.FAULT)
      .handle(ArithmeticException::class)
      .handleResult(TestResult.FAULT_AGAIN)
      .handle(IllegalArgumentException::class)
      .retry(3)

    val result = policy.raiseResultsAndOrExceptions(
      TestResult.FAULT,
      ArithmeticException(),
      IllegalArgumentException(),
      TestResult.FAULT_AGAIN,
      TestResult.GOOD
    )

    assertThat(result).isEqualTo(TestResult.FAULT_AGAIN)
  }

  @Test
  fun shouldThrowWhenExceptionThrownNextAfterRetriesExhaustHandlingBothExceptionsAndSpecifiedResultsMixingExceptionsAndResultsSpecifyingResultsFirst() {
    val policy = Policy.retry<TestResult>()
      .handleResult(TestResult.FAULT)
      .handle(ArithmeticException::class)
      .handleResult(TestResult.FAULT_AGAIN)
      .handle(IllegalArgumentException::class)
      .retry(3)

    assertFailsWith(IllegalArgumentException::class) {
      policy.raiseResultsAndOrExceptions(
        TestResult.FAULT,
        ArithmeticException(),
        TestResult.FAULT_AGAIN,
        IllegalArgumentException(),
        TestResult.GOOD
      )
    }
  }

  @Test
  fun shouldReturnHandledResultWhenHandledResultReturnedNextAfterRetriesExhaustHandlingBothExceptionsAndSpecifiedResultsMixingExceptionsAndResultsSpecifyingExceptionsFirst() {
    val policy = Policy.retry<TestResult>()
      .handle(ArithmeticException::class)
      .handleResult(TestResult.FAULT)
      .handle(IllegalArgumentException::class)
      .handleResult(TestResult.FAULT_AGAIN)
      .retry(3)

    val result = policy.raiseResultsAndOrExceptions(
      TestResult.FAULT,
      ArithmeticException(),
      IllegalArgumentException(),
      TestResult.FAULT_AGAIN,
      TestResult.GOOD
    )

    assertThat(result).isEqualTo(TestResult.FAULT_AGAIN)
  }

  @Test
  fun shouldThrowWhenExceptionThrownNextAfterRetriesExhaustHandlingBothExceptionsAndSpecifiedResultsMixingExceptionsAndResultsSpecifyingExceptionsFirst() {
    val policy = Policy.retry<TestResult>()
      .handle(ArithmeticException::class)
      .handleResult(TestResult.FAULT)
      .handle(IllegalArgumentException::class)
      .handleResult(TestResult.FAULT_AGAIN)
      .retry(3)

    assertFailsWith(IllegalArgumentException::class) {
      policy.raiseResultsAndOrExceptions(
        TestResult.FAULT,
        ArithmeticException(),
        TestResult.FAULT_AGAIN,
        IllegalArgumentException(),
        TestResult.GOOD
      )
    }
  }

  @Test
  fun shouldReturnUnhandledResultIfNotOneOfResultsOrExceptionsSpecified() {
    val policy = Policy.retry<TestResult>()
      .handleResult(TestResult.FAULT)
      .handle(ArithmeticException::class)
      .retry(2)

    val result = policy.raiseResults(TestResult.FAULT_AGAIN)

    assertThat(result).isEqualTo(TestResult.FAULT_AGAIN)
  }

  @Test
  fun shouldThrowIfNotOneOfResultsOrExceptionsHandled() {
    val policy = Policy.retry<TestResult>()
      .handle(ArithmeticException::class)
      .handleResult(TestResult.FAULT)
      .retry(2)

    assertFailsWith(IllegalArgumentException::class) {
      policy.raiseResultsAndOrExceptions(
        IllegalArgumentException(),
        TestResult.GOOD
      )
    }
  }

  @Test
  fun shouldHandleBothExceptionsAndSpecifiedResultsWithPredicates() {
    val policy = Policy.retry<TestResultClass>()
      .handle(IllegalArgumentException::class) { it.message == "key" }
      .handleResult { it.resultCode == TestResult.FAULT }
      .retry(2)

    val result = policy.raiseResultsAndOrExceptions(
      TestResultClass(TestResult.FAULT),
      IllegalArgumentException("key"),
      TestResultClass(TestResult.GOOD)
    )

    assertThat(result.resultCode).isEqualTo(TestResult.GOOD)
  }

  @Test
  fun shouldThrowIfExceptionPredicateNotMatched() {
    val policy = Policy.retry<TestResultClass>()
      .handle(IllegalArgumentException::class) { it.message == "key" }
      .handleResult { it.resultCode == TestResult.FAULT }
      .retry(2)

    assertFailsWith(IllegalArgumentException::class) {
      policy.raiseResultsAndOrExceptions(
        TestResultClass(TestResult.FAULT),
        IllegalArgumentException("value"),
        TestResultClass(TestResult.GOOD)
      )
    }
  }

  @Test
  fun shouldReturnUnhandledResultIfResultPredicateNotMatched() {
    val policy = Policy.retry<TestResultClass>()
      .handle(IllegalArgumentException::class) { it.message == "key" }
      .handleResult { it.resultCode == TestResult.FAULT }
      .retry(2)

    val result = policy.raiseResultsAndOrExceptions(
      IllegalArgumentException("key"),
      TestResultClass(TestResult.FAULT_AGAIN),
      TestResultClass(TestResult.GOOD)
    )

    assertThat(result.resultCode).isEqualTo(TestResult.FAULT_AGAIN)
  }
}
