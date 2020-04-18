package hr.tjakopan.yarl;

import hr.tjakopan.yarl.retry.AsyncRetryPolicy;
import hr.tjakopan.yarl.retry.AsyncRetryPolicyBuilder;
import kotlin.Unit;
import org.junit.Test;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.assertj.core.api.Assertions.assertThat;

public class AsyncPolicyTest {
  @Test
  public void executingThePolicyActionShouldExecuteTheSpecifiedAsyncAction() {
    final AtomicBoolean executed = new AtomicBoolean(false);
    final AsyncRetryPolicy<Unit> policy = new AsyncRetryPolicyBuilder<Unit>()
      .handle(ArithmeticException.class)
      .retryAsync((result, integer, context) -> Unit.INSTANCE);

    final CompletableFuture<Unit> future = policy.executeAsync(() -> Unit.INSTANCE)
      .whenComplete((unit, throwable) -> {
        if (throwable == null) {
          executed.set(true);
        }
      });
    future.join();

    //noinspection ConstantConditions
    assertThat(executed).isTrue();
  }

  @Test
  public void executingThePolicyFunctionShouldExecuteTheSpecifiedAsyncFunctionAndReturnTheResult() {
    final AsyncRetryPolicy<Integer> policy = new AsyncRetryPolicyBuilder<Integer>()
      .handle(ArithmeticException.class)
      .retryAsync((result, integer, context) -> Unit.INSTANCE);

    final CompletableFuture<Integer> future = policy.executeAsync(() -> 2);

    assertThat(future.join()).isEqualTo(2);
  }
}
