package hr.tjakopan.yarl.retry;

import hr.tjakopan.yarl.Context;
import hr.tjakopan.yarl.DelegateResult;
import hr.tjakopan.yarl.test.helpers.TestResult;
import org.junit.Test;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static hr.tjakopan.yarl.Functions.fromConsumer4Async;
import static org.assertj.core.api.Assertions.assertThat;

public class AsyncWaitAndRetryHandleResultTest {
  @Test
  public void shouldBeAbleToCalculateRetryDurationsBasedOnTheHandledFault() {
    final var expectedRetryWaits = Map.of(TestResult.FAULT, Duration.ofMillis(2),
      TestResult.FAULT_AGAIN, Duration.ofMillis(4));
    final var actualRetryWaits = new ArrayList<Duration>(2);
    final var policy = AsyncRetryPolicy.<TestResult>builder()
      .handleResult(TestResult.FAULT)
      .handleResult(TestResult.FAULT_AGAIN)
      .waitAndRetry(2,
        (Integer i, DelegateResult<TestResult> outcome, Context c) ->
          outcome.fold(expectedRetryWaits::get, e -> Duration.ZERO),
        fromConsumer4Async(dr -> duration -> (i, c) -> actualRetryWaits.add(duration)));

    final var iterator = expectedRetryWaits.keySet().iterator();
    policy.executeAsync(() -> {
      if (iterator.hasNext()) {
        return CompletableFuture.completedFuture(iterator.next());
      } else {
        return CompletableFuture.completedFuture(TestResult.UNDEFINED);
      }
    })
      .join();

    assertThat(actualRetryWaits).containsExactlyElementsOf(expectedRetryWaits.values());
  }
}
