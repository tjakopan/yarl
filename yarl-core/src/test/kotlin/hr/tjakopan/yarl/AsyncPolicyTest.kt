package hr.tjakopan.yarl

import hr.tjakopan.yarl.retry.AsyncRetryPolicyBuilder
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

@ExperimentalCoroutinesApi
class AsyncPolicyTest {
  @Test
  fun executingThePolicyActionShouldExecuteTheSpecifiedAsyncAction() = runBlockingTest {
    var executed = false
    val policy = AsyncRetryPolicyBuilder<Unit>()
      .handle(ArithmeticException::class.java)
      .retry { _, _, _ -> }

    policy.execute {
      executed = true
    }

    assertThat(executed).isTrue()
  }

  @Test
  fun executingThePolicyFunctionShouldExecuteTheSpecifiedAsyncFunctionAndReturnTheResult() = runBlockingTest {
    val policy = AsyncRetryPolicyBuilder<Int>()
      .handle(ArithmeticException::class.java)
      .retry { _, _, _ ->  }

    val result: Int = policy.execute { 2 }

    assertThat(result).isEqualTo(2)
  }
}
