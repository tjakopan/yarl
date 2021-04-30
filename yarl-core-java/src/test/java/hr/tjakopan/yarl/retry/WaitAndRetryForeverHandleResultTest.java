package hr.tjakopan.yarl.retry;

import hr.tjakopan.yarl.test.helpers.TestResult;
import org.junit.Test;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;

import static hr.tjakopan.yarl.Functions.fromConsumer4;
import static org.assertj.core.api.Assertions.assertThat;

public class WaitAndRetryForeverHandleResultTest {
  @Test
  public void shouldBeAbleToCalculateRetryDurationsBasedOnTheHandledFault() {
    final var expectedRetryWaits = new HashMap<TestResult, Duration>() {{
      put(TestResult.FAULT, Duration.ofMillis(2));
      put(TestResult.FAULT_AGAIN, Duration.ofMillis(4));
    }};
    final var actualRetryWaits = new ArrayList<Duration>(2);
    final var policy = RetryPolicy.<TestResult>builder()
      .handleResult(TestResult.FAULT)
      .handleResult(TestResult.FAULT_AGAIN)
      .waitAndRetryForever((i, outcome, c) -> outcome.fold(expectedRetryWaits::get, e -> Duration.ZERO),
        fromConsumer4(d -> duration -> (i, c) -> actualRetryWaits.add(duration)));

    final var iterator = expectedRetryWaits.keySet().iterator();
    policy.execute(() -> {
      if (iterator.hasNext()) {
        return iterator.next();
      } else {
        return TestResult.UNDEFINED;
      }
    });

    assertThat(actualRetryWaits).containsExactlyElementsOf(expectedRetryWaits.values());
  }
}
