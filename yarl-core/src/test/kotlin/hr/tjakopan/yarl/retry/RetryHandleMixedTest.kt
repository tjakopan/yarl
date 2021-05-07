package hr.tjakopan.yarl.retry

import hr.tjakopan.yarl.Policy
import hr.tjakopan.yarl.test.helpers.TestResult
import hr.tjakopan.yarl.test.helpers.TestResultClass
import hr.tjakopan.yarl.test.helpers.raiseResults
import hr.tjakopan.yarl.test.helpers.raiseResultsAndOrExceptions
import org.assertj.core.api.Assertions.assertThat
import kotlin.test.Test
import kotlin.test.assertFailsWith

class RetryHandleMixedTest {
  @Test
  fun `should handle exception when handling exceptions only`() {
    val policy = Policy.retry<TestResult>()
      .handle(ArithmeticException::class)
      .retry(1)

    val result = policy.raiseResultsAndOrExceptions(ArithmeticException(), TestResult.GOOD)

    assertThat(result).isEqualTo(TestResult.GOOD)
  }

  @Test
  fun `should throw unhandled exception when handling exceptions only`() {
    val policy = Policy.retry<TestResult>()
      .handle(ArithmeticException::class)
      .retry(1)

    assertFailsWith(IllegalArgumentException::class) {
      policy.raiseResultsAndOrExceptions(IllegalArgumentException(), TestResult.GOOD)
    }
  }

  @Test
  fun `should handle both exception and specified result if raised same number of times as retry count when configuring results before exceptions`() {
    val policy = Policy.retry<TestResult>()
      .handleResult(TestResult.FAULT)
      .handle(ArithmeticException::class)
      .retry(2)

    val result = policy.raiseResultsAndOrExceptions(TestResult.FAULT, ArithmeticException(), TestResult.GOOD)

    assertThat(result).isEqualTo(TestResult.GOOD)
  }

  @Test
  fun `should handle both exception and specified result if raised same number of times as retry count when configuring exception before result`() {
    val policy = Policy.retry<TestResult>()
      .handle(ArithmeticException::class)
      .handleResult(TestResult.FAULT)
      .retry(2)

    val result = policy.raiseResultsAndOrExceptions(TestResult.FAULT, ArithmeticException(), TestResult.GOOD)

    assertThat(result).isEqualTo(TestResult.GOOD)
  }

  @Test
  fun `should handle both exceptions and specified results if raised same number of times as retry count mixing exceptions and results specifying exceptions first`() {
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
  fun `should handle both exceptions and specified results if raised same number of times as retry count mixing exceptions and results specifying results first`() {
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
  fun `should return handled result when handled result returned next after retries exhaust handling both exceptions and specified results mixing exceptions and results specifying results first`() {
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
  fun `should throw when exception thrown next after retries exhaust handling both exceptions and specified results mixing exceptions and results specifying results first`() {
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
  fun `should return handled result when handled result returned next after retries exhaust handling both exceptions and specified results mixing exceptions and results specifying exceptions first`() {
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
  fun `should throw when exception thrown next after retries exhaust handling both exceptions and specified results mixing exceptions and results specifying exceptions first`() {
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
  fun `should return unhandled result if not one of results or exceptions specified`() {
    val policy = Policy.retry<TestResult>()
      .handleResult(TestResult.FAULT)
      .handle(ArithmeticException::class)
      .retry(2)

    val result = policy.raiseResults(TestResult.FAULT_AGAIN)

    assertThat(result).isEqualTo(TestResult.FAULT_AGAIN)
  }

  @Test
  fun `should throw if not one of results or exceptions handled`() {
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
  fun `should handle both exceptions and specified results with predicates`() {
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
  fun `should throw if exception predicate not matched`() {
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
  fun `should return unhandled result if result predicate not matched`() {
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
