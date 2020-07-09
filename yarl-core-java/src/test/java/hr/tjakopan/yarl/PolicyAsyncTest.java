package hr.tjakopan.yarl;

import hr.tjakopan.yarl.PolicyResult.Success;
import hr.tjakopan.yarl.noop.AsyncNoOpPolicy;
import hr.tjakopan.yarl.retry.AsyncRetryPolicy;
import hr.tjakopan.yarl.test.helpers.TestResult;
import kotlin.Unit;
import org.junit.Test;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.*;

public class PolicyAsyncTest {
  private static final String RESULT_SUCCESS = "Result was a success.";
  private static final String RESULT_FAILURE = "Result was a failure.";
  private static final String RESULT_FAILURE_WITH_RESULT = "Result was a failure with handled result.";
  private static final String RESULT_FAILURE_WITH_EXCEPTION = "Result was a failure with exception.";
  private static final String NULL_PARAMETER = "Parameter specified as non-null is null";

  //<editor-fold desc="execute tests">
  @Test
  public void executingThePolicyFunctionShouldExecuteTheSpecifiedAsyncFunctionAndReturnTheResult() {
    final var policy = AsyncRetryPolicy.<TestResult>builder()
      .handleResult(TestResult.FAULT)
      .retry();

    final var future = policy.executeAsync(() -> CompletableFuture.completedFuture(TestResult.GOOD));

    assertThat(future.join()).isEqualTo(TestResult.GOOD);
  }
  //</editor-fold>

  //<editor-fold desc="executeAndCapture tests">
  @Test
  public void executingThePolicyFunctionSuccessfullyShouldReturnSuccessResult() {
    final var future = AsyncRetryPolicy.<TestResult>builder()
      .handleResult(TestResult.FAULT)
      .retry()
      .executeAndCaptureAsync(() -> CompletableFuture.completedFuture(TestResult.GOOD));

    final var result = future.join();

    result.onSuccess((r, context) -> {
      assertThat(r).isEqualTo(TestResult.GOOD);
      return Unit.INSTANCE;
    })
      .onFailure((faultType, context) -> {
        fail(RESULT_FAILURE);
        return Unit.INSTANCE;
      });
  }

  @Test
  public void executingThePolicyFunctionAndFailingWithAHandledExceptionTypeShouldReturnFailureResultIndicatingThatExceptionTypeIsOneHandledByThisPolicy() {
    final var handledException = new ArithmeticException();
    final var future = AsyncRetryPolicy.<TestResult>builder()
      .handle(ArithmeticException.class)
      .retry()
      .executeAndCaptureAsync(() -> {
        throw handledException;
      });

    final var result = future.join();

    result.onFailureWithException((throwable, exceptionType, faultType, context) -> {
      assertThat(throwable).isSameAs(handledException);
      assertThat(exceptionType).isSameAs(ExceptionType.HANDLED_BY_THIS_POLICY);
      assertThat(faultType).isSameAs(FaultType.EXCEPTION_HANDLED_BY_THIS_POLICY);
      return Unit.INSTANCE;
    })
      .onSuccess((integer, context) -> fail(RESULT_SUCCESS))
      .onFailureWithResult((integer, faultType, context) -> fail(RESULT_FAILURE_WITH_RESULT));
  }

  @Test
  public void executingThePolicyFunctionAndFailingWithAnUnhandledExceptionTypeShouldReturnFailureResultIndicatingThatExceptionTypeIsUnhandledByThisPolicy() {
    final var unhandledException = new Exception();
    final var future = AsyncRetryPolicy.builder()
      .handle(ArithmeticException.class)
      .retry()
      .executeAndCaptureAsync(() -> {
        throw new RuntimeException(unhandledException);
      });

    final var result = future.join();

    result.onFailureWithException((throwable, exceptionType, faultType, context) -> {
      assertThat(throwable.getCause()).isSameAs(unhandledException);
      assertThat(exceptionType).isSameAs(ExceptionType.UNHANDLED);
      assertThat(faultType).isSameAs(FaultType.UNHANDLED_EXCEPTION);
      return Unit.INSTANCE;
    })
      .onSuccess((integer, context) -> fail(RESULT_SUCCESS))
      .onFailureWithResult((integer, faultType, context) -> fail(RESULT_FAILURE_WITH_RESULT));
  }

  @Test
  public void executingThePolicyFunctionAndFailingWithAHandledResultShouldReturnFailureResultIndicatingThatResultIsOneHandledByThisPolicy() {
    final var handledResult = TestResult.FAULT;
    final var future = AsyncRetryPolicy.<TestResult>builder()
      .handleResult(handledResult)
      .retry()
      .executeAndCaptureAsync(() -> CompletableFuture.completedFuture(handledResult));

    final var result = future.join();

    result.onFailureWithResult((testResult, faultType, context) -> {
      assertThat(testResult).isSameAs(handledResult);
      assertThat(faultType).isSameAs(FaultType.RESULT_HANDLED_BY_THIS_POLICY);
      return Unit.INSTANCE;
    })
      .onSuccess((testResult, context) -> fail(RESULT_SUCCESS))
      .onFailureWithException((throwable, exceptionType, faultType, context) -> fail(RESULT_FAILURE_WITH_EXCEPTION));
  }

  @Test
  public void executingThePolicyFunctionAndReturningAnUnhandledResultShouldReturnResultNotIndicatingAnyFailure() {
    final var handledResult = TestResult.FAULT;
    final var unhandledResult = TestResult.GOOD;
    final var future = AsyncRetryPolicy.<TestResult>builder()
      .handleResult(handledResult)
      .retry()
      .executeAndCaptureAsync(() -> CompletableFuture.completedFuture(unhandledResult));

    final var result = future.join();

    assertThat(result.isSuccess()).isTrue();
    //noinspection rawtypes
    assertThat(((Success) result).getResult()).isSameAs(unhandledResult);
  }
  //</editor-fold>

  //<editor-fold desc="context tests">
  @Test
  public void executingThePolicyFunctionShouldThrowWhenContextDataIsNull() {
    //noinspection ConstantConditions
    assertThatExceptionOfType(IllegalArgumentException.class)
      .isThrownBy(() -> AsyncRetryPolicy.builder()
        .handle(ArithmeticException.class)
        .retry()
        .executeAsync((Map<String, Object>) null, context -> CompletableFuture.completedFuture(2)))
      .withMessageContaining(NULL_PARAMETER);
  }

  @Test
  public void executingThePolicyFunctionShouldThrowWhenContextIsNull() {
    //noinspection ConstantConditions
    assertThatExceptionOfType(IllegalArgumentException.class)
      .isThrownBy(() -> AsyncRetryPolicy.builder()
        .handle(ArithmeticException.class)
        .retry()
        .executeAsync((Context) null, context -> CompletableFuture.completedFuture(2)))
      .withMessageContaining(NULL_PARAMETER);
  }

  @Test
  public void executingAndCapturingThePolicyFunctionShouldThrowWhenContextDataIsNull() {
    //noinspection ConstantConditions
    assertThatExceptionOfType(IllegalArgumentException.class)
      .isThrownBy(() -> AsyncRetryPolicy.builder()
        .handle(ArithmeticException.class)
        .retry()
        .executeAndCaptureAsync((Map<String, Object>) null, context -> CompletableFuture.completedFuture(2)))
      .withMessageContaining(NULL_PARAMETER);
  }

  @Test
  public void executingAndCapturingThePolicyFunctionShouldThrowWhenContextIsNull() {
    //noinspection ConstantConditions
    assertThatExceptionOfType(IllegalArgumentException.class)
      .isThrownBy(() -> AsyncRetryPolicy.builder()
        .handle(ArithmeticException.class)
        .retry()
        .executeAndCaptureAsync((Context) null, context -> CompletableFuture.completedFuture(2)))
      .withMessageContaining(NULL_PARAMETER);
  }

  @Test
  public void executingThePolicyFunctionShouldPassContextToExecutedDelegate() {
    final var operationKey = "SomeKey";
    final var executionContext = Context.builder()
      .operationKey(operationKey)
      .build();
    final var capturedContext = new AtomicReference<Context>();

    new AsyncNoOpPolicy<>()
      .executeAsync(executionContext, context -> {
        capturedContext.set(context);
        return CompletableFuture.completedFuture(null);
      })
      .join();

    assertThat(capturedContext.get().getOperationKey()).isEqualTo(operationKey);
  }

  @Test
  public void executingAndCapturingThePolicyFunctionShouldPassContextToExecutedDelegate() {
    final var operationKey = "SomeKey";
    final var executionContext = Context.builder()
      .operationKey(operationKey)
      .build();
    final var capturedContext = new AtomicReference<Context>();

    new AsyncNoOpPolicy<>()
      .executeAndCaptureAsync(executionContext, context -> {
        capturedContext.set(context);
        return CompletableFuture.completedFuture(null);
      })
      .join();

    assertThat(capturedContext.get().getOperationKey()).isEqualTo(operationKey);
  }

  @Test
  public void executingAndCapturingThePolicyFunctionShouldPassContextToPolicyResult() {
    final var operationKey = "SomeKey";
    final var executionContext = Context.builder()
      .operationKey(operationKey)
      .build();
    final var future = new AsyncNoOpPolicy<>()
      .executeAndCaptureAsync(executionContext, context -> null);

    final var result = future.join();

    assertThat(result.getContext().getOperationKey()).isEqualTo(operationKey);
  }
  //</editor-fold>
}
