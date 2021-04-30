package hr.tjakopan.yarl;

import hr.tjakopan.yarl.noop.NoOpPolicy;
import hr.tjakopan.yarl.retry.RetryPolicy;
import hr.tjakopan.yarl.test.helpers.TestResult;
import org.junit.Test;

import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static hr.tjakopan.yarl.Functions.*;
import static org.assertj.core.api.Assertions.*;

public class PolicyTest {
  private static final String RESULT_SUCCESS = "Result was a success.";
  private static final String RESULT_FAILURE = "Result was a failure.";
  private static final String RESULT_FAILURE_WITH_RESULT = "Result was a failure with handled result.";
  private static final String RESULT_FAILURE_WITH_EXCEPTION = "Result was a failure with exception.";
  private static final String NULL_PARAMETER = "Parameter specified as non-null is null";

  //<editor-fold desc="execute tests">
  @Test
  public void executingThePolicyFunctionShouldExecuteTheSpecifiedFunctionAndReturnTheResult() {
    final var policy = RetryPolicy.<TestResult>builder()
      .handleResult(TestResult.FAULT)
      .retry();

    final var result = policy.execute(() -> TestResult.GOOD);

    assertThat(result).isEqualTo(TestResult.GOOD);
  }
  //</editor-fold>

  //<editor-fold desc="executeAndCapture tests">
  @Test
  public void executingThePolicyFunctionSuccessfullyShouldReturnSuccessResult() {
    final var result = RetryPolicy.<TestResult>builder()
      .handleResult(TestResult.FAULT)
      .retry()
      .executeAndCapture(() -> TestResult.GOOD);

    result.onSuccess(fromConsumer2((r, context) -> assertThat(r).isEqualTo(TestResult.GOOD)))
      .onFailure(fromConsumer2(((faultType, context) -> fail(RESULT_FAILURE))));
  }

  @Test
  public void executingThePolicyFunctionAndFailingWithAHandledExceptionTypeShouldReturnFailureResultIndicatingThatExceptionTypeIsOneHandledByThisPolicy() {
    final var handledException = new ArithmeticException();

    final var result = RetryPolicy.builder()
      .handle(ArithmeticException.class)
      .retry()
      .executeAndCapture(() -> {
        throw handledException;
      });

    result.onFailureWithException(fromConsumer4(throwable -> exceptionType -> (faultType, context) -> {
      assertThat(throwable).isSameAs(handledException);
      assertThat(exceptionType).isSameAs(ExceptionType.HANDLED_BY_THIS_POLICY);
      assertThat(faultType).isSameAs(FaultType.EXCEPTION_HANDLED_BY_THIS_POLICY);
    }))
      .onSuccess((integer, context) -> fail(RESULT_SUCCESS))
      .onFailureWithResult((integer, faultType, context) -> fail(RESULT_FAILURE_WITH_RESULT));
  }

  @Test
  public void executingThePolicyFunctionAndFailingWithAnUnhandledExceptionTypeShouldReturnFailureResultIndicatingThatExceptionTypeIsUnhandledByThisPolicy() {
    final var unhandledException = new Exception();

    final var result = RetryPolicy.builder()
      .handle(ArithmeticException.class)
      .retry()
      .executeAndCapture(() -> {
        throw new RuntimeException(unhandledException);
      });

    result.onFailureWithException(fromConsumer4(throwable -> exceptionType -> (faultType, context) -> {
      assertThat(throwable.getCause()).isSameAs(unhandledException);
      assertThat(exceptionType).isSameAs(ExceptionType.UNHANDLED);
      assertThat(faultType).isSameAs(FaultType.UNHANDLED_EXCEPTION);
    }))
      .onSuccess((integer, context) -> fail(RESULT_SUCCESS))
      .onFailureWithResult((integer, faultType, context) -> fail(RESULT_FAILURE_WITH_RESULT));
  }

  @Test
  public void executingThePolicyFunctionAndFailingWithAHandledResultShouldReturnFailureResultIndicatingThatResultIsOneHandledByThisPolicy() {
    final var handledResult = TestResult.FAULT;

    final var result = RetryPolicy.<TestResult>builder()
      .handleResult(handledResult)
      .retry()
      .executeAndCapture(() -> handledResult);

    result.onFailureWithResult(fromConsumer3(testResult -> (faultType, context) -> {
      assertThat(testResult).isSameAs(handledResult);
      assertThat(faultType).isSameAs(FaultType.RESULT_HANDLED_BY_THIS_POLICY);
    }))
      .onSuccess((testResult, context) -> fail(RESULT_SUCCESS))
      .onFailureWithException((throwable, exceptionType, faultType, context) -> fail(RESULT_FAILURE_WITH_EXCEPTION));
  }

  @Test
  public void executingThePolicyFunctionAndReturningAnUnhandledResultShouldReturnResultNotIndicatingAnyFailure() {
    final var handledResult = TestResult.FAULT;
    final var unhandledResult = TestResult.GOOD;

    final var result = RetryPolicy.<TestResult>builder()
      .handleResult(handledResult)
      .retry()
      .executeAndCapture(() -> unhandledResult);

    assertThat(result.isSuccess()).isTrue();
    //noinspection rawtypes
    assertThat(((PolicyResult.Success) result).getResult()).isSameAs(unhandledResult);
  }
  //</editor-fold>

  //<editor-fold desc="context tests">
  @Test
  public void executingThePolicyFunctionShouldThrowWhenContextDataIsNull() {
    //noinspection ConstantConditions
    assertThatExceptionOfType(NullPointerException.class)
      .isThrownBy(() -> RetryPolicy.builder()
        .handle(ArithmeticException.class)
        .retry()
        .execute((Map<String, Object>) null, context -> 2))
      .withMessageContaining(NULL_PARAMETER);
  }

  @Test
  public void executingThePolicyFunctionShouldThrowWhenContextIsNull() {
    //noinspection ConstantConditions
    assertThatExceptionOfType(NullPointerException.class)
      .isThrownBy(() -> RetryPolicy.builder()
        .handle(ArithmeticException.class)
        .retry()
        .execute((Context) null, context -> 2))
      .withMessageContaining(NULL_PARAMETER);
  }

  @Test
  public void executingAndCapturingThePolicyFunctionShouldThrowWhenContextDataIsNull() {
    //noinspection ConstantConditions
    assertThatExceptionOfType(NullPointerException.class)
      .isThrownBy(() -> RetryPolicy.builder()
        .handle(ArithmeticException.class)
        .retry()
        .executeAndCapture((Map<String, Object>) null, context -> 2))
      .withMessageContaining(NULL_PARAMETER);
  }

  @Test
  public void executingAndCapturingThePolicyFunctionShouldThrowWhenContextIsNull() {
    //noinspection ConstantConditions
    assertThatExceptionOfType(NullPointerException.class)
      .isThrownBy(() -> RetryPolicy.builder()
        .handle(ArithmeticException.class)
        .retry()
        .executeAndCapture((Context) null, context -> 2))
      .withMessageContaining(NULL_PARAMETER);
  }

  @Test
  public void executingThePolicyFunctionShouldPassContextToExecutedDelegate() {
    final var operationKey = "SomeKey";
    final var executionContext = Context.builder()
      .operationKey(operationKey)
      .build();
    final var capturedContext = new AtomicReference<Context>();

    new NoOpPolicy<>()
      .execute(executionContext, context -> {
        capturedContext.set(context);
        return null;
      });

    assertThat(capturedContext.get().getOperationKey()).isEqualTo(operationKey);
  }

  @Test
  public void executingAndCapturingThePolicyFunctionShouldPassContextToExecutedDelegate() {
    final var operationKey = "SomeKey";
    final var executionContext = Context.builder()
      .operationKey(operationKey)
      .build();
    final var capturedContext = new AtomicReference<Context>();

    new NoOpPolicy<>()
      .executeAndCapture(executionContext, context -> {
        capturedContext.set(context);
        return null;
      });

    assertThat(capturedContext.get().getOperationKey()).isEqualTo(operationKey);
  }

  @Test
  public void executingAndCapturingThePolicyFunctionShouldPassContextToPolicyResult() {
    final var operationKey = "SomeKey";
    final var executionContext = Context.builder()
      .operationKey(operationKey)
      .build();

    final var result = new NoOpPolicy<>()
      .executeAndCapture(executionContext, context -> null);

    assertThat(result.getContext().getOperationKey()).isEqualTo(operationKey);
  }
  //</editor-fold>
}
