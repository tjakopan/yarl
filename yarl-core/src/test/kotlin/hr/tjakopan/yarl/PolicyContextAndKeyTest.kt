package hr.tjakopan.yarl

import hr.tjakopan.yarl.retry.retry
import hr.tjakopan.yarl.test.helpers.TestResult
import hr.tjakopan.yarl.test.helpers.raiseResults
import org.assertj.core.api.Assertions.assertThat
import java.util.*
import kotlin.test.Test

class PolicyContextAndKeyTest {
  //<editor-fold desc="configuration">
  @Test
  fun `should be able fluently to configure the policy key`() {
    val policy = Policy.retry<Int>()
      .policyKey(UUID.randomUUID().toString())
      .handleResult(0)
      .retry()

    assertThat(policy).isInstanceOf(Policy::class.java)
  }

  @Test
  fun `policy key property should be the fluently configured policy key`() {
    val key = "SomePolicyKey"
    val policy = Policy.retry<Int>()
      .handleResult(0)
      .policyKey(key)
      .retry()

    assertThat(policy.policyKey).isEqualTo(key)
  }

  @Suppress("UsePropertyAccessSyntax")
  @Test
  fun `policy key property should be non null or empty if not explicitly configured`() {
    val policy = Policy.retry<Int>()
      .handleResult(0)
      .retry()

    assertThat(policy.policyKey).isNotNull()
    assertThat(policy.policyKey).isNotEmpty()
  }

  @Test
  fun `policy key property should start with policy type if not explicitly configured`() {
    val policy = Policy.retry<Int>()
      .handleResult(0)
      .retry()

    assertThat(policy.policyKey).startsWith("RetryPolicy")
  }

  @Test
  fun `policy key property should be unique for different instances if not explicitly configured`() {
    val policy1 = Policy.retry<Int>()
      .handleResult(0)
      .retry()
    val policy2 = Policy.retry<Int>()
      .handleResult(0)
      .retry()

    assertThat(policy1.policyKey).isNotEqualTo(policy2.policyKey)
  }

  @Test
  fun `policy key property should return consistent value for same policy instance if not explicitly configured`() {
    val policy = Policy.retry<Int>()
      .handleResult(0)
      .retry()

    val keyRetrievedFirst = policy.policyKey
    val keyRetrievedSecond = policy.policyKey

    assertThat(keyRetrievedSecond).isSameAs(keyRetrievedFirst)
  }
  //</editor-fold>

  @Test
  fun `should pass policy key to execution context`() {
    val policyKey = UUID.randomUUID().toString()
    var policyKeySetOnExecutionContext: String? = null
    val onRetry: (DelegateResult<TestResult>, Int, Context) -> Unit =
      { _, _, context -> policyKeySetOnExecutionContext = context.policyKey }
    val policy = Policy.retry<TestResult>()
      .handleResult(TestResult.FAULT)
      .policyKey(policyKey)
      .retry(1, onRetry)

    policy.raiseResults(TestResult.FAULT, TestResult.GOOD)

    assertThat(policyKeySetOnExecutionContext).isEqualTo(policyKey)
  }

  @Test
  fun `should pass operation key to execution context`() {
    val operationKey = "SomeKey"
    var operationKeySetOnContext: String? = null
    val onRetry: (DelegateResult<TestResult>, Int, Context) -> Unit =
      { _, _, context -> operationKeySetOnContext = context.operationKey }
    val policy = Policy.retry<TestResult>()
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
}
