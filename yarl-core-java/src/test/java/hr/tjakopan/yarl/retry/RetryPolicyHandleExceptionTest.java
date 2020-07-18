package hr.tjakopan.yarl.retry;

import hr.tjakopan.yarl.test.helpers.PolicyUtils;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static hr.tjakopan.yarl.Functions.fromConsumer;
import static hr.tjakopan.yarl.Functions.fromConsumer3;
import static org.assertj.core.api.Assertions.*;

public class RetryPolicyHandleExceptionTest {
  @Test
  public void shouldThrowWhenRetryCountIsLessThanZero() {
    assertThatThrownBy(() ->
      RetryPolicy.<Void>builder()
        .handle(ArithmeticException.class)
        .retry(-1))
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessageContaining("Retry count");
  }

  @Test
  public void shouldThrowWhenOnRetryActionIsNull() {
    //noinspection ConstantConditions
    assertThatThrownBy(() ->
      RetryPolicy.<Void>builder()
        .handle(ArithmeticException.class)
        .retry(null))
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessageContaining("onRetry");
  }

  @Test
  public void shouldNotThrowWhenSpecifiedExceptionThrownSameNumberOfTimesAsRetryCount() {
    final var policy = RetryPolicy.<Void>builder()
      .handle(ArithmeticException.class)
      .retry(3);

    PolicyUtils.raiseExceptions(policy, 3, i -> new ArithmeticException());
  }

  @Test
  public void shouldNotThrowWhenOneOfTheSpecifiedExceptionsThrownSameNumberOfTimesAsRetryCount() {
    final var policy = RetryPolicy.<Void>builder()
      .handle(ArithmeticException.class)
      .handle(IllegalArgumentException.class)
      .retry(3);

    PolicyUtils.raiseExceptions(policy, 3, i -> new IllegalArgumentException());
  }

  @Test
  public void shouldNotThrowWhenSpecifiedExceptionThrownLessNumberOfTimesThanRetryCount() {
    final var policy = RetryPolicy.<Void>builder()
      .handle(ArithmeticException.class)
      .retry(3);

    PolicyUtils.raiseExceptions(policy, 1, i -> new ArithmeticException());
  }

  @Test
  public void shouldNotThrowWhenOneOfTheSpecifiedExceptionsThrownLessNumberOfTimesThanRetryCount() {
    final var policy = RetryPolicy.<Void>builder()
      .handle(ArithmeticException.class)
      .handle(IllegalArgumentException.class)
      .retry(3);

    PolicyUtils.raiseExceptions(policy, 1, i -> new IllegalArgumentException());
  }

  @Test
  public void shouldThrowWhenSpecifiedExceptionThrownMoreTimesThenRetryCount() {
    final var policy = RetryPolicy.<Void>builder()
      .handle(ArithmeticException.class)
      .retry(3);

    assertThatExceptionOfType(ArithmeticException.class).isThrownBy(() ->
      PolicyUtils.raiseExceptions(policy, 3 + 1, i -> new ArithmeticException()));
  }

  @Test
  public void shouldThrowWhenOneOfTheSpecifiedExceptionsAreThrownMoreTimesThenRetryCount() {
    final var policy = RetryPolicy.<Void>builder()
      .handle(ArithmeticException.class)
      .handle(IllegalArgumentException.class)
      .retry(3);

    assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(() ->
      PolicyUtils.raiseExceptions(policy, 3 + 1, i -> new IllegalArgumentException()));
  }

  @Test
  public void shouldThrowWhenExceptionThrownIsNotTheSpecifiedExceptionType() {
    final var policy = RetryPolicy.<Void>builder()
      .handle(ArithmeticException.class)
      .retry();

    assertThatExceptionOfType(NullPointerException.class).isThrownBy(() ->
      PolicyUtils.raiseExceptions(policy, 1, i -> new NullPointerException()));
  }

  @Test
  public void shouldThrowWhenExceptionThrownIsNotOneOfTheSpecifiedExceptionTypes() {
    final var policy = RetryPolicy.<Void>builder()
      .handle(ArithmeticException.class)
      .handle(IllegalArgumentException.class)
      .retry();

    assertThatExceptionOfType(NullPointerException.class).isThrownBy(() ->
      PolicyUtils.raiseExceptions(policy, 1, i -> new NullPointerException()));
  }

  @Test
  public void shouldThrowWhenSpecifiedExceptionPredicateIsNotSatisfied() {
    final var policy = RetryPolicy.<Void>builder()
      .handle(ArithmeticException.class, e -> false)
      .retry();

    assertThatExceptionOfType(ArithmeticException.class).isThrownBy(() ->
      PolicyUtils.raiseExceptions(policy, 1, i -> new ArithmeticException()));
  }

  @Test
  public void shouldThrowWhenNoneOfTheSpecifiedExceptionPredicatesAreSatisfied() {
    final var policy = RetryPolicy.<Void>builder()
      .handle(ArithmeticException.class, e -> false)
      .handle(IllegalArgumentException.class, e -> false)
      .retry();

    assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(() ->
      PolicyUtils.raiseExceptions(policy, 1, i -> new IllegalArgumentException()));
  }

  @Test
  public void shouldNotThrowWhenSpecifiedExceptionPredicateIsSatisfied() {
    final var policy = RetryPolicy.<Void>builder()
      .handle(ArithmeticException.class, e -> true)
      .retry();

    PolicyUtils.raiseExceptions(policy, 1, i -> new ArithmeticException());
  }

  @Test
  public void shouldNotThrowWhenOneOfTheSpecifiedExceptionPredicatesAreSatisfied() {
    final var policy = RetryPolicy.<Void>builder()
      .handle(ArithmeticException.class, e -> true)
      .handle(IllegalArgumentException.class, e -> true)
      .retry();

    PolicyUtils.raiseExceptions(policy, 1, i -> new IllegalArgumentException());
  }

  @Test
  public void shouldCallOnRetryOnEachRetryWithTheCurrentRetryCount() {
    final var expectedRetryCounts = List.of(1, 2, 3);
    final var retryCounts = new ArrayList<Integer>(3);
    final var policy = RetryPolicy.<Void>builder()
      .handle(ArithmeticException.class)
      .retry(3, fromConsumer3(r -> (retryCount, c) -> retryCounts.add(retryCount)));

    PolicyUtils.raiseExceptions(policy, 3, i -> new ArithmeticException());

    assertThat(retryCounts).containsExactlyElementsOf(expectedRetryCounts);
  }

  @Test
  public void shouldCallOnRetryOnEachRetryWithTheCurrentException() {
    final var expectedExceptions = List.of("Exception #1", "Exception #2", "Exception #3");
    final var retryExceptions = new ArrayList<Throwable>(3);
    final var policy = RetryPolicy.<Void>builder()
      .handle(ArithmeticException.class)
      .retry(3, fromConsumer3(outcome -> (i, c) -> outcome.onFailure(fromConsumer(retryExceptions::add))));

    PolicyUtils.raiseExceptions(policy, 3, i -> new ArithmeticException("Exception #" + i));

    assertThat(retryExceptions.stream()
      .map(Throwable::getMessage))
      .containsExactlyElementsOf(expectedExceptions);
  }
}
