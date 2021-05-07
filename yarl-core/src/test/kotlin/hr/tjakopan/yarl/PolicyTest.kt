package hr.tjakopan.yarl

import hr.tjakopan.yarl.retry.retry
import hr.tjakopan.yarl.test.helpers.TestResult
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import kotlin.test.fail

class PolicyTest {
  //<editor-fold desc="execute tests">
  @Test
  fun `executing the policy function should execute the specified function and return the result`() {
    val policy = Policy.retry<TestResult>()
      .handleResult(TestResult.FAULT)
      .retry()

    val result = policy.execute { TestResult.GOOD }

    assertThat(result).isEqualTo(TestResult.GOOD);
  }
  //</editor-fold>

  //<editor-fold desc="executeAndCapture tests">
  @Test
  fun `executing the policy function successfully should return success result`() {
    val result = Policy.retry<TestResult>()
      .handleResult(TestResult.FAULT)
      .retry()
      .executeAndCapture { TestResult.GOOD }

    result.onSuccess { r, _ -> assertThat(r).isEqualTo(TestResult.GOOD) }
      .onFailure { _, _ -> fail(RESULT_FAILURE) }
  }

  @Test
  fun `executing the policy function and failing with a handled exception type should return failure result indicating that exception type is one handled by this policy`() {
    val handledException = ArithmeticException()

    val result = Policy.retry<Unit>()
      .handle(ArithmeticException::class.java)
      .retry()
      .executeAndCapture { throw handledException }

    result.onFailureWithException { throwable, exceptionType, faultType, _ ->
      assertThat(throwable).isSameAs(handledException)
      assertThat(exceptionType).isSameAs(ExceptionType.HANDLED_BY_THIS_POLICY)
      assertThat(faultType).isSameAs(FaultType.EXCEPTION_HANDLED_BY_THIS_POLICY)
    }
      .onSuccess { _, _ -> fail(RESULT_SUCCESS) }
      .onFailureWithResult { _, _, _ -> fail(RESULT_FAILURE_WITH_RESULT) }
  }

  @Test
  fun `executing the policy function and failing with an unhandled exception type should return failure result indicating that exception type is unhandled by this policy`() {
    val unhandledException = Exception()

    val result = Policy.retry<Unit>()
      .handle(ArithmeticException::class.java)
      .retry()
      .executeAndCapture { throw unhandledException }

    result.onFailureWithException { throwable, exceptionType, faultType, _ ->
      assertThat(throwable).isSameAs(unhandledException)
      assertThat(exceptionType).isSameAs(ExceptionType.UNHANDLED)
      assertThat(faultType).isSameAs(FaultType.UNHANDLED_EXCEPTION)
    }
      .onSuccess { _, _ -> fail(RESULT_SUCCESS) }
      .onFailureWithResult { _, _, _ -> fail(RESULT_FAILURE_WITH_RESULT) }
  }

  @Test
  fun `executing the policy function and failing with a handled result should return failure result indicating that result is one handled by this policy`() {
    val handledResult = TestResult.FAULT

    val result = Policy.retry<TestResult>()
      .handleResult(handledResult)
      .retry()
      .executeAndCapture { handledResult }

    result.onFailureWithResult { testResult, faultType, _ ->
      assertThat(testResult).isSameAs(handledResult)
      assertThat(faultType).isSameAs(FaultType.RESULT_HANDLED_BY_THIS_POLICY)
    }
      .onSuccess { _, _ -> fail(RESULT_SUCCESS) }
      .onFailureWithException { _, _, _, _ -> fail(RESULT_FAILURE_WITH_EXCEPTION) }
  }

  @Test
  fun `executing the policy function and returning an unhandled result should return result not indicating any failure`() {
    val handledResult = TestResult.FAULT
    val unhandledResult = TestResult.GOOD

    val result = Policy.retry<TestResult>()
      .handleResult(handledResult)
      .retry()
      .executeAndCapture { unhandledResult }

    @Suppress("UsePropertyAccessSyntax")
    assertThat(result.isSuccess).isTrue()
    assertThat((result as PolicyResult.Success).result).isSameAs(unhandledResult)
  }
  //</editor-fold>
}
