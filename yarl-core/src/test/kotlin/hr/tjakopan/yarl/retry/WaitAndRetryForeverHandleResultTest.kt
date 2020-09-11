package hr.tjakopan.yarl.retry

import hr.tjakopan.yarl.Policy
import hr.tjakopan.yarl.test.helpers.TestResult
import org.assertj.core.api.Assertions.assertThat
import java.time.Duration
import kotlin.test.Test

class WaitAndRetryForeverHandleResultTest {
  @Test
  fun shouldBeAbleToCalculateRetryDurationsBasedOnTheHandledFault() {
    val expectedRetryWaits =
      mapOf(TestResult.FAULT to Duration.ofMillis(2), TestResult.FAULT_AGAIN to Duration.ofMillis(4))
    val actualRetryWaits = mutableListOf<Duration>()
    val policy = Policy.retry<TestResult>()
      .handleResult(TestResult.FAULT)
      .handleResult(TestResult.FAULT_AGAIN)
      .waitAndRetryForever({ _, outcome, _ ->
        outcome.fold({ expectedRetryWaits[it] }, { Duration.ZERO }) ?: Duration.ZERO
      }) { _, duration, _, _ -> actualRetryWaits.add(duration) }

    val iterator = expectedRetryWaits.iterator()
    policy.execute {
      if (iterator.hasNext()) {
        return@execute iterator.next().key
      } else {
        return@execute TestResult.UNDEFINED
      }
    }

    assertThat(actualRetryWaits).containsExactlyElementsOf(expectedRetryWaits.values)
  }
}
