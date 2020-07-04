package hr.tjakopan.yarl

import hr.tjakopan.yarl.test.helpers.TestResult
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.assertj.core.api.Assertions.assertThat
import kotlin.test.Test
import kotlin.test.fail

const val RESULT_SUCCESS = "Result was a success."
const val RESULT_FAILURE = "Result was a failure."
const val RESULT_FAILURE_WITH_RESULT = "Result was a failure with handled result."
const val RESULT_FAILURE_WITH_EXCEPTION = "Result was a failure with exception."

@ExperimentalCoroutinesApi
class AsyncPolicyTest {

  //<editor-fold desc="execute tests">
  @Test
  fun executingThePolicyFunctionShouldExecuteTheSpecifiedFunctionAndReturnTheResult() = runBlockingTest {
    val policy = Policy.asyncRetry<TestResult>()
      .handleResult(TestResult.FAULT)
      .retry()

    val result: TestResult = policy.execute { TestResult.GOOD }

    assertThat(result).isEqualTo(TestResult.GOOD)
  }
  //</editor-fold>

  //<editor-fold desc="executeAndCapture tests">
  @Test
  fun executingThePolicyFunctionSuccessfullyShouldReturnSuccessResult() = runBlockingTest {
    val result = Policy.asyncRetry<TestResult>()
      .handleResult(TestResult.FAULT)
      .retry()
      .executeAndCapture { TestResult.GOOD }

    result.onSuccess { r, _ -> assertThat(r).isEqualTo(TestResult.GOOD) }
      .onFailure { _, _ -> fail(RESULT_FAILURE) }
  }

  @Test
  fun executingThePolicyFunctionAndFailingWithAHandledExceptionTypeShouldReturnFailureResultIndicatingThatExceptionTypeIsOneHandledByThisPolicy() =
    runBlockingTest {
      val handledException = ArithmeticException()

      val result = Policy.asyncRetry<Unit>()
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
  fun executingThePolicyFunctionAndFailingWithAnUnhandledExceptionTypeShouldReturnFailureResultIndicatingThatExceptionTypeIsUnhandledByThisPolicy() =
    runBlockingTest {
      val unhandledException = Exception()

      val result = Policy.asyncRetry<Unit>()
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
  fun executingThePolicyFunctionAndFailingWithAHandledResultShouldReturnFailureResultIndicatingThatResultIsOneHandledByThisPolicy() =
    runBlockingTest {
      val handledResult = TestResult.FAULT

      val result = Policy.asyncRetry<TestResult>()
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
  fun executingThePolicyFunctionAndReturningAnUnhandledResultShouldReturnResultNotIndicatingAnyFailure() =
    runBlockingTest {
      val handledResult = TestResult.FAULT
      val unhandledResult = TestResult.GOOD

      val result = Policy.asyncRetry<TestResult>()
        .handleResult(handledResult)
        .retry()
        .executeAndCapture { unhandledResult }

      @Suppress("UsePropertyAccessSyntax")
      assertThat(result.isSuccess).isTrue()
      assertThat((result as PolicyResult.Success).result).isSameAs(unhandledResult)
    }
  //</editor-fold>

  //<editor-fold desc="context tests">
  @Test
  fun executingThePolicyFunctionShouldPassContextToExecutedDelegate() = runBlockingTest {
    val operationKey = "SomeKey"
    val executionContext = Context(operationKey = operationKey)
    var capturedContext: Context? = null

    Policy.asyncNoOp<Unit>()
      .noOp()
      .execute(executionContext) { context -> capturedContext = context }

    assertThat(capturedContext?.operationKey).isEqualTo(operationKey)
  }

  @Test
  fun executingAndCapturingThePolicyFunctionShouldPassContextToExecutedDelegate() = runBlockingTest {
    val operationKey = "SomeKey"
    val executionContext = Context(operationKey = operationKey)
    var capturedContext: Context? = null

    Policy.asyncNoOp<Unit>()
      .noOp()
      .executeAndCapture(executionContext) { context -> capturedContext = context }

    assertThat(capturedContext?.operationKey).isEqualTo(operationKey)
  }

  @Test
  fun executingAndCapturingThePolicyFunctionShouldPassContextToPolicyResult() = runBlockingTest {
    val operationKey = "SomeKey";
    val executionContext = Context(operationKey = operationKey)

    val result = Policy.asyncNoOp<Unit>()
      .noOp()
      .executeAndCapture(executionContext) { _ -> }

    assertThat(result.context.operationKey).isEqualTo(operationKey)
  }
  //</editor-fold>
}
