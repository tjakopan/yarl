package hr.tjakopan.yarl.retry;

import hr.tjakopan.yarl.Context;
import hr.tjakopan.yarl.test.helpers.PolicyUtils;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static hr.tjakopan.yarl.Functions.fromConsumer;
import static hr.tjakopan.yarl.Functions.fromConsumer3;
import static org.assertj.core.api.Assertions.*;

public class RetryHandleExceptionTest {
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
      .isInstanceOf(NullPointerException.class)
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

  @Test
  public void shouldCallOnRetryWithAHandledCauseException() {
    final var passedToOnRetry = new AtomicReference<Throwable>();
    final var policy = RetryPolicy.<Void>builder()
      .handleCause(ArithmeticException.class)
      .retry(3, fromConsumer3(outcome -> (i, c) -> outcome.onFailure(fromConsumer(passedToOnRetry::set))));
    final var toRaiseAsInner = new ArithmeticException();
    final var withInner = new RuntimeException(toRaiseAsInner);

    PolicyUtils.raiseExceptions(policy, 1, i -> withInner);

    assertThat(passedToOnRetry.get()).isSameAs(toRaiseAsInner);
  }

  @Test
  public void shouldNotCallOnRetryWhenNoRetriesArePerformed() {
    final var retryCount = new AtomicInteger(0);
    final var policy = RetryPolicy.<Void>builder()
      .handle(ArithmeticException.class)
      .retry(fromConsumer3(d -> (i, c) -> retryCount.incrementAndGet()));

    assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(() ->
      PolicyUtils.raiseExceptions(policy, 1, i -> new IllegalArgumentException()));
    assertThat(retryCount.get()).isEqualTo(0);
  }

  @Test
  public void shouldCallOnRetryWithThePassedContext() {
    final var context = new AtomicReference<Context>();
    final var policy = RetryPolicy.<Void>builder()
      .handle(ArithmeticException.class)
      .retry(fromConsumer3(d -> (i, ctx) -> context.set(ctx)));

    PolicyUtils.raiseExceptions(
      policy,
      Context.of(new HashMap<>() {{
        put("key1", "value1");
        put("key2", "value2");
      }}),
      1,
      i -> new ArithmeticException());

    assertThat(context.get()).isNotNull();
    assertThat(context.get()).containsKeys("key1", "key2")
      .containsValues("value1", "value2");
  }

  @Test
  public void shouldCallOnRetryWithThePassedContextWhenExecuteAndCapture() {
    final var context = new AtomicReference<Context>();
    final var policy = RetryPolicy.<Void>builder()
      .handle(ArithmeticException.class)
      .retry(fromConsumer3(d -> (i, ctx) -> context.set(ctx)));

    PolicyUtils.raiseExceptionsOnExecuteAndCapture(
      policy,
      Context.of(new HashMap<>() {{
        put("key1", "value1");
        put("key2", "value2");
      }}),
      1,
      i -> new ArithmeticException());

    assertThat(context.get()).isNotNull();
    assertThat(context.get()).containsKeys("key1", "key2")
      .containsValues("value1", "value2");
  }

  @Test
  public void contextShouldBeEmptyIfExecuteNotCalledWithAnyData() {
    final var capturedContext = new AtomicReference<Context>();
    final var policy = RetryPolicy.<Void>builder()
      .handle(ArithmeticException.class)
      .retry(fromConsumer3(d -> (i, context) -> capturedContext.set(context)));

    PolicyUtils.raiseExceptions(policy, 1, i -> new ArithmeticException());

    assertThat(capturedContext.get()).isEmpty();
  }

  @Test
  public void shouldCreateNewContextForEachCallToExecute() {
    final var contextValue = new AtomicReference<String>();
    //noinspection ConstantConditions
    final var policy = RetryPolicy.<Void>builder()
      .handle(ArithmeticException.class)
      .retry(fromConsumer3(d -> (i, context) -> contextValue.set(context.get("key").toString())));

    PolicyUtils.raiseExceptions(
      policy,
      Context.of(new HashMap<>() {{
        put("key", "original_value");
      }}),
      1,
      i -> new ArithmeticException()
    );

    assertThat(contextValue.get()).isEqualTo("original_value");

    PolicyUtils.raiseExceptions(
      policy,
      Context.of(new HashMap<>() {{
        put("key", "new_value");
      }}),
      1,
      i -> new ArithmeticException()
    );

    assertThat(contextValue.get()).isEqualTo("new_value");
  }

  @Test
  public void shouldCreateNewContextForEachCallToExecuteAndCapture() {
    final var contextValue = new AtomicReference<String>();
    //noinspection ConstantConditions
    final var policy = RetryPolicy.<Void>builder()
      .handle(ArithmeticException.class)
      .retry(fromConsumer3(d -> (i, context) -> contextValue.set(context.get("key").toString())));

    PolicyUtils.raiseExceptionsOnExecuteAndCapture(
      policy,
      Context.of(new HashMap<>() {{
        put("key", "original_value");
      }}),
      1,
      i -> new ArithmeticException()
    );

    assertThat(contextValue.get()).isEqualTo("original_value");

    PolicyUtils.raiseExceptionsOnExecuteAndCapture(
      policy,
      Context.of(new HashMap<>() {{
        put("key", "new_value");
      }}),
      1,
      i -> new ArithmeticException()
    );

    assertThat(contextValue.get()).isEqualTo("new_value");
  }

  @Test
  public void shouldCreateNewStateForEachCallToPolicy() {
    final var policy = RetryPolicy.<Void>builder()
      .handle(ArithmeticException.class)
      .retry();

    PolicyUtils.raiseExceptions(policy, 1, i -> new ArithmeticException());
    PolicyUtils.raiseExceptions(policy, 1, i -> new ArithmeticException());
  }

  @Test
  public void shouldNotCallOnRetryWhenRetryCountIsZero() {
    final var retryInvoked = new AtomicBoolean(false);
    final var policy = RetryPolicy.<Void>builder()
      .handle(ArithmeticException.class)
      .retry(0, fromConsumer3(d -> (i, c) -> retryInvoked.set(true)));

    assertThatExceptionOfType(ArithmeticException.class).isThrownBy(() ->
      PolicyUtils.raiseExceptions(policy, 1, i -> new ArithmeticException()));
    assertThat(retryInvoked.get()).isFalse();
  }
}
