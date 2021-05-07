package hr.tjakopan.yarl

import hr.tjakopan.yarl.retry.asyncRetry
import hr.tjakopan.yarl.test.helpers.TestResult
import hr.tjakopan.yarl.test.helpers.raiseResults
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.assertj.core.api.Assertions.assertThat
import java.util.*
import kotlin.test.Test

@ExperimentalCoroutinesApi
class PolicyContextAndKeyAsyncTest {
  //<editor-fold desc="configuration">
  @Test
  fun `should be able fluently to configure policy key`() {
    val policy = Policy.asyncRetry<Int>()
      .handleResult(0)
      .policyKey(UUID.randomUUID().toString())
      .retry()

    assertThat(policy).isInstanceOf(AsyncPolicy::class.java)
  }

  @Test
  fun `policy key property should be the fluently configured policy key`() {
    val key = "SomePolicyKey"
    val policy = Policy.asyncRetry<Int>()
      .handleResult(0)
      .policyKey(key)
      .retry()

    assertThat(policy.policyKey).isEqualTo(key)
  }

  @Suppress("UsePropertyAccessSyntax")
  @Test
  fun `policy key property should be non null or empty if not explicitly configured`() {
    val policy = Policy.asyncRetry<Int>()
      .handleResult(0)
      .retry()

    assertThat(policy.policyKey).isNotNull()
    assertThat(policy.policyKey).isNotEmpty()
  }

  @Test
  fun `policy key property should start with policy type if not explicitly configured`() {
    val policy = Policy.asyncRetry<Int>()
      .handleResult(0)
      .retry()

    assertThat(policy.policyKey).startsWith("AsyncRetryPolicy")
  }

  @Test
  fun `policy key property should be unique for different instances if not explicitly configured`() {
    val policy1 = Policy.asyncRetry<Int>()
      .handleResult(0)
      .retry()
    val policy2 = Policy.asyncRetry<Int>()
      .handleResult(0)
      .retry()

    assertThat(policy1.policyKey).isNotEqualTo(policy2.policyKey)
  }

  @Test
  fun `policy key property should return consistent value for same policy instance if not explicitly configured`() {
    val policy = Policy.asyncRetry<Int>()
      .handleResult(0)
      .retry()

    val keyRetrievedFirst = policy.policyKey
    val keyRetrievedSecond = policy.policyKey

    assertThat(keyRetrievedSecond).isSameAs(keyRetrievedFirst)
  }
  //</editor-fold>

  //<editor-fold desc="policyKey and execution contexts tests">
  @Test
  fun `should pass policy key to execution context`() = runBlockingTest {
    val policyKey = UUID.randomUUID().toString()
    var policyKeySetOnExecutionContext: String? = null
    val onRetry: suspend (DelegateResult<TestResult>, Int, Context) -> Unit =
      { _, _, context -> policyKeySetOnExecutionContext = context.policyKey }
    val policy = Policy.asyncRetry<TestResult>()
      .handleResult(TestResult.FAULT)
      .policyKey(policyKey)
      .retry(1, onRetry)

    policy.raiseResults(TestResult.FAULT, TestResult.GOOD)

    assertThat(policyKeySetOnExecutionContext).isEqualTo(policyKey)
  }

  @Test
  fun `should pass operation key to execution context`() = runBlockingTest {
    val operationKey = "SomeKey"
    var operationKeySetOnContext: String? = null
    val onRetry: suspend (DelegateResult<TestResult>, Int, Context) -> Unit =
      { _, _, context -> operationKeySetOnContext = context.operationKey }
    val policy = Policy.asyncRetry<TestResult>()
      .handleResult(TestResult.FAULT)
      .retry(1, onRetry)

    var firstExecution: Boolean = true
    policy.execute(Context(operationKey)) {
      if (firstExecution) {
        firstExecution = false
        return@execute TestResult.FAULT
      }
      return@execute TestResult.GOOD
    }

    assertThat(operationKeySetOnContext).isEqualTo(operationKey)
  }
  //</editor-fold>
}
