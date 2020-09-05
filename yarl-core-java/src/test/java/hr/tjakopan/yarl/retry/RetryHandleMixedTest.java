package hr.tjakopan.yarl.retry;

import hr.tjakopan.yarl.test.helpers.PolicyUtils;
import hr.tjakopan.yarl.test.helpers.TestResult;
import hr.tjakopan.yarl.test.helpers.TestResultClass;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

public class RetryHandleMixedTest {
  @Test
  public void shouldHandleExceptionWhenHandlingExceptionsOnly() {
    final var policy = RetryPolicy.<TestResult>builder()
      .handle(ArithmeticException.class)
      .retry(1);

    final var result = PolicyUtils.raiseResultsAndOrExceptions(policy, TestResult.class,
      new ArithmeticException(), TestResult.GOOD);

    assertThat(result).isEqualTo(TestResult.GOOD);
  }

  @Test
  public void shouldThrowUnhandledExceptionWhenHandlingExceptionsOnly() {
    final var policy = RetryPolicy.<TestResult>builder()
      .handle(ArithmeticException.class)
      .retry(1);

    assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(() ->
      PolicyUtils.raiseResultsAndOrExceptions(policy, TestResult.class, new IllegalArgumentException(), TestResult.GOOD));
  }

  @Test
  public void shouldHandleBothExceptionAndSpecifiedResultIfRaisedSameNumberOfTimesAsRetryCountWhenConfiguringResultsBeforeExceptions() {
    final var policy = RetryPolicy.<TestResult>builder()
      .handleResult(TestResult.FAULT)
      .handle(ArithmeticException.class)
      .retry(2);

    final var result = PolicyUtils.raiseResultsAndOrExceptions(policy, TestResult.class,
      TestResult.FAULT, new ArithmeticException(), TestResult.GOOD);

    assertThat(result).isEqualTo(TestResult.GOOD);
  }

  @Test
  public void shouldHandleBothExceptionAndSpecifiedResultIfRaisedSameNumberOfTimesAsRetryCountWhenConfiguringExceptionBeforeResult() {
    final var policy = RetryPolicy.<TestResult>builder()
      .handle(ArithmeticException.class)
      .handleResult(TestResult.FAULT)
      .retry(2);

    final var result = PolicyUtils.raiseResultsAndOrExceptions(policy, TestResult.class,
      TestResult.FAULT, new ArithmeticException(), TestResult.GOOD);

    assertThat(result).isEqualTo(TestResult.GOOD);
  }

  @Test
  public void shouldHandleBothExceptionsAndSpecifiedResultsIfRaisedSameNumberOfTimesAsRetryCountMixingExceptionsAndResultsSpecifyingExceptionsFirst() {
    final var policy = RetryPolicy.<TestResult>builder()
      .handle(ArithmeticException.class)
      .handleResult(TestResult.FAULT)
      .handle(IllegalArgumentException.class)
      .handleResult(TestResult.FAULT_AGAIN)
      .retry(4);

    final var result = PolicyUtils.raiseResultsAndOrExceptions(policy, TestResult.class,
      TestResult.FAULT, new ArithmeticException(), new IllegalArgumentException(), TestResult.FAULT_AGAIN,
      TestResult.GOOD);

    assertThat(result).isEqualTo(TestResult.GOOD);
  }

  @Test
  public void shouldHandleBothExceptionsAndSpecifiedResultsIfRaisedSameNumberOfTimesAsRetryCountMixingExceptionsAndResultsSpecifyingResultsFirst() {
    final var policy = RetryPolicy.<TestResult>builder()
      .handleResult(TestResult.FAULT)
      .handle(ArithmeticException.class)
      .handleResult(TestResult.FAULT_AGAIN)
      .handle(IllegalArgumentException.class)
      .retry(4);

    final var result = PolicyUtils.raiseResultsAndOrExceptions(policy, TestResult.class,
      TestResult.FAULT, new ArithmeticException(), new IllegalArgumentException(), TestResult.FAULT_AGAIN,
      TestResult.GOOD);

    assertThat(result).isEqualTo(TestResult.GOOD);
  }

  @Test
  public void shouldReturnHandledResultWhenHandledResultReturnedNextAfterRetriesExhaustHandlingBothExceptionsAndSpecifiedResultsMixingExceptionsAndResultsSpecifyingResultsFirst() {
    final var policy = RetryPolicy.<TestResult>builder()
      .handleResult(TestResult.FAULT)
      .handle(ArithmeticException.class)
      .handleResult(TestResult.FAULT_AGAIN)
      .handle(IllegalArgumentException.class)
      .retry(3);

    final var result = PolicyUtils.raiseResultsAndOrExceptions(policy, TestResult.class,
      TestResult.FAULT, new ArithmeticException(), new IllegalArgumentException(), TestResult.FAULT_AGAIN,
      TestResult.GOOD);

    assertThat(result).isEqualTo(TestResult.FAULT_AGAIN);
  }

  @Test
  public void shouldThrowWhenExceptionThrownNextAfterRetriesExhaustHandlingBothExceptionsAndSpecifiedResultsMixingExceptionsAndResultsSpecifyingResultsFirst() {
    final var policy = RetryPolicy.<TestResult>builder()
      .handleResult(TestResult.FAULT)
      .handle(ArithmeticException.class)
      .handleResult(TestResult.FAULT_AGAIN)
      .handle(IllegalArgumentException.class)
      .retry(3);

    assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(() ->
      PolicyUtils.raiseResultsAndOrExceptions(policy, TestResult.class,
        TestResult.FAULT, new ArithmeticException(), TestResult.FAULT_AGAIN, new IllegalArgumentException(),
        TestResult.GOOD));
  }

  @Test
  public void shouldReturnHandledResultWhenHandledResultReturnedNextAfterRetriesExhaustHandlingBothExceptionsAndSpecifiedResultsMixingExceptionsAndResultsSpecifyingExceptionsFirst() {
    final var policy = RetryPolicy.<TestResult>builder()
      .handle(ArithmeticException.class)
      .handleResult(TestResult.FAULT)
      .handle(IllegalArgumentException.class)
      .handleResult(TestResult.FAULT_AGAIN)
      .retry(3);

    final var result = PolicyUtils.raiseResultsAndOrExceptions(policy, TestResult.class,
      TestResult.FAULT, new ArithmeticException(), new IllegalArgumentException(), TestResult.FAULT_AGAIN,
      TestResult.GOOD);

    assertThat(result).isEqualTo(TestResult.FAULT_AGAIN);
  }

  @Test
  public void shouldThrowWhenExceptionThrownNextAfterRetriesExhaustHandlingBothExceptionsAndSpecifiedResultsMixingExceptionsAndResultsSpecifyingExceptionsFirst() {
    final var policy = RetryPolicy.<TestResult>builder()
      .handle(ArithmeticException.class)
      .handleResult(TestResult.FAULT)
      .handle(IllegalArgumentException.class)
      .handleResult(TestResult.FAULT_AGAIN)
      .retry(3);

    assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(() ->
      PolicyUtils.raiseResultsAndOrExceptions(policy, TestResult.class,
        TestResult.FAULT, new ArithmeticException(), TestResult.FAULT_AGAIN, new IllegalArgumentException(),
        TestResult.GOOD));
  }

  @Test
  public void shouldReturnUnhandledResultIfNotOneOfResultsOrExceptionsSpecified() {
    final var policy = RetryPolicy.<TestResult>builder()
      .handleResult(TestResult.FAULT)
      .handle(ArithmeticException.class)
      .retry(2);

    final var result = PolicyUtils.raiseResults(policy, TestResult.FAULT_AGAIN);

    assertThat(result).isEqualTo(TestResult.FAULT_AGAIN);
  }

  @Test
  public void shouldThrowIfNotOneOfResultsOrExceptionsHandled() {
    final var policy = RetryPolicy.<TestResult>builder()
      .handle(ArithmeticException.class)
      .handleResult(TestResult.FAULT)
      .retry(2);

    assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(() ->
      PolicyUtils.raiseResultsAndOrExceptions(policy, TestResult.class, new IllegalArgumentException(), TestResult.GOOD));
  }

  @Test
  public void shouldHandleBothExceptionsAndSpecifiedResultsWithPredicates() {
    final var policy = RetryPolicy.<TestResultClass>builder()
      .handle(IllegalArgumentException.class, e -> e.getMessage().equals("key"))
      .handleResult(r -> r.getResultCode() == TestResult.FAULT)
      .retry(2);

    final var result = PolicyUtils.raiseResultsAndOrExceptions(policy, TestResultClass.class,
      new TestResultClass(TestResult.FAULT), new IllegalArgumentException("key"), new TestResultClass(TestResult.GOOD));

    assertThat(result.getResultCode()).isEqualTo(TestResult.GOOD);
  }

  @Test
  public void shouldThrowIfExceptionPredicateNotMatched() {
    final var policy = RetryPolicy.<TestResultClass>builder()
      .handle(IllegalArgumentException.class, e -> e.getMessage().equals("key"))
      .handleResult(r -> r.getResultCode() == TestResult.FAULT)
      .retry(2);

    assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(() ->
      PolicyUtils.raiseResultsAndOrExceptions(policy, TestResultClass.class,
        new TestResultClass(TestResult.FAULT), new IllegalArgumentException("value"),
        new TestResultClass(TestResult.GOOD)));
  }

  @Test
  public void shouldReturnUnhandledResultIfResultPredicateNotMatched() {
    final var policy = RetryPolicy.<TestResultClass>builder()
      .handle(IllegalArgumentException.class, e -> e.getMessage().equals("key"))
      .handleResult(r -> r.getResultCode() == TestResult.FAULT)
      .retry(2);

    final var result = PolicyUtils.raiseResultsAndOrExceptions(policy, TestResultClass.class,
      new IllegalArgumentException("key"), new TestResultClass(TestResult.FAULT_AGAIN),
      new TestResultClass(TestResult.GOOD));

    assertThat(result.getResultCode()).isEqualTo(TestResult.FAULT_AGAIN);
  }
}
