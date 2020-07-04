package hr.tjakopan.yarl

import hr.tjakopan.yarl.test.helpers.TestResult
import hr.tjakopan.yarl.test.helpers.raiseResults
import org.assertj.core.api.Assertions.assertThat
import java.util.*
import kotlin.test.Test

class PolicyContextAndKeysTest {
  //<editor-fold desc="configuration">
  @Test
  fun shouldBeAbleFluentlyToConfigureThePolicyKey() {
    val policy = Policy.retry<Int>()
      .policyKey(UUID.randomUUID().toString())
      .handleResult(0)
      .retry()

    assertThat(policy).isInstanceOf(Policy::class.java)
  }

  @Test
  fun policyKeyPropertyShouldBeTheFluentlyConfiguredPolicyKey() {
    val key = "SomePolicyKey"
    val policy = Policy.retry<Int>()
      .handleResult(0)
      .policyKey(key)
      .retry()

    assertThat(policy.policyKey).isEqualTo(key)
  }

  @Suppress("UsePropertyAccessSyntax")
  @Test
  fun policyKeyPropertyShouldBeNonNullOrEmptyIfNotExplicitlyConfigured() {
    val policy = Policy.retry<Int>()
      .handleResult(0)
      .retry()

    assertThat(policy.policyKey).isNotNull()
    assertThat(policy.policyKey).isNotEmpty()
  }

  @Test
  fun policyKeyPropertyShouldStartWithPolicyTypeIfNotExplicitlyConfigured() {
    val policy = Policy.retry<Int>()
      .handleResult(0)
      .retry()

    assertThat(policy.policyKey).startsWith("RetryPolicy")
  }

  @Test
  fun policyKeyPropertyShouldBeUniqueForDifferentInstancesIfNotExplicitlyConfigured() {
    val policy1 = Policy.retry<Int>()
      .handleResult(0)
      .retry()
    val policy2 = Policy.retry<Int>()
      .handleResult(0)
      .retry()

    assertThat(policy1.policyKey).isNotEqualTo(policy2.policyKey)
  }

  @Test
  fun policyKeyPropertyShouldReturnConsistentValueForSamePolicyInstanceIfNotExplicitlyConfigured() {
    val policy = Policy.retry<Int>()
      .handleResult(0)
      .retry()

    val keyRetrievedFirst = policy.policyKey
    val keyRetrievedSecond = policy.policyKey

    assertThat(keyRetrievedSecond).isSameAs(keyRetrievedFirst)
  }
  //</editor-fold>

  @Test
  fun shouldPassPolicyKeyToExecutionContext() {
    val policyKey = UUID.randomUUID().toString()
    var policyKeySetOnExecutionContext: String? = null
    val onRetry: (Result<TestResult>, Int, Context) -> Unit =
      { _, _, context -> policyKeySetOnExecutionContext = context.policyKey }
    val policy = Policy.retry<TestResult>()
      .handleResult(TestResult.FAULT)
      .policyKey(policyKey)
      .retry(1, onRetry)

    policy.raiseResults(TestResult.FAULT, TestResult.GOOD)

    assertThat(policyKeySetOnExecutionContext).isEqualTo(policyKey)
  }

  @Test
  fun shouldPassOperationKeyToExecutionContext() {
    val operationKey = "SomeKey"
    var operationKeySetOnContext: String? = null
    val onRetry: (Result<TestResult>, Int, Context) -> Unit =
      { _, _, context -> operationKeySetOnContext = context.operationKey }
    val policy = Policy.retry<TestResult>()
      .handleResult(TestResult.FAULT)
      .retry(1, onRetry)

    var firstExecution: Boolean = true
    policy.execute(Context(operationKey = operationKey)) {
      if (firstExecution) {
        firstExecution = false
        return@execute TestResult.FAULT
      }
      return@execute TestResult.GOOD
    }

    assertThat(operationKeySetOnContext).isEqualTo(operationKey)
  }
}
