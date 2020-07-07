package hr.tjakopan.yarl

import hr.tjakopan.yarl.retry.retry
import hr.tjakopan.yarl.test.helpers.TestResult
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import kotlin.test.fail

class PolicyTest {
  //<editor-fold desc="execute tests">
  @Test
  fun executingThePolicyFunctionShouldExecuteTheSpecifiedFunctionAndReturnTheResult() {
    val policy = Policy.retry<TestResult>()
      .handleResult(TestResult.FAULT)
      .retry()

    val result = policy.execute { TestResult.GOOD }

    assertThat(result).isEqualTo(TestResult.GOOD);
  }
  //</editor-fold>

  //<editor-fold desc="executeAndCapture tests">
  @Test
  fun executingThePolicyFunctionSuccessfullyShouldReturnSuccessResult() {
    val result = Policy.retry<TestResult>()
      .handleResult(TestResult.FAULT)
      .retry()
      .executeAndCapture { TestResult.GOOD }

    result.onSuccess { r, _ -> assertThat(r).isEqualTo(TestResult.GOOD) }
      .onFailure { _, _ -> fail(RESULT_FAILURE) }
  }

  @Test
  fun executingThePolicyFunctionAndFailingWithAHandledExceptionTypeShouldReturnFailureResultIndicatingThatExceptionTypeIsOneHandledByThisPolicy() {
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
  fun executingThePolicyFunctionAndFailingWithAnUnhandledExceptionTypeShouldReturnFailureResultIndicatingThatExceptionTypeIsUnhandledByThisPolicy() {
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
  fun executingThePolicyFunctionAndFailingWithAHandledResultShouldReturnFailureResultIndicatingThatResultIsOneHandledByThisPolicy() {
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
  fun executingThePolicyFunctionAndReturningAnUnhandledResultShouldReturnResultNotIndicatingAnyFailure() {
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
