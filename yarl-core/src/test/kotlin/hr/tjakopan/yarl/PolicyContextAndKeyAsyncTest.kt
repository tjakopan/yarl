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
  fun shouldBeAbleFluentlyToConfigurePolicyKey() {
    val policy = Policy.asyncRetry<Int>()
      .handleResult(0)
      .policyKey(UUID.randomUUID().toString())
      .retry()

    assertThat(policy).isInstanceOf(AsyncPolicy::class.java)
  }

  @Test
  fun policyKeyPropertyShouldBeTheFluentlyConfiguredPolicyKey() {
    val key = "SomePolicyKey"
    val policy = Policy.asyncRetry<Int>()
      .handleResult(0)
      .policyKey(key)
      .retry()

    assertThat(policy.policyKey).isEqualTo(key)
  }

  @Suppress("UsePropertyAccessSyntax")
  @Test
  fun policyKeyPropertyShouldBeNonNullOrEmptyIfNotExplicitlyConfigured() {
    val policy = Policy.asyncRetry<Int>()
      .handleResult(0)
      .retry()

    assertThat(policy.policyKey).isNotNull()
    assertThat(policy.policyKey).isNotEmpty()
  }

  @Test
  fun policyKeyPropertyShouldStartWithPolicyTypeIfNotExplicitlyConfigured() {
    val policy = Policy.asyncRetry<Int>()
      .handleResult(0)
      .retry()

    assertThat(policy.policyKey).startsWith("AsyncRetryPolicy")
  }

  @Test
  fun policyKeyPropertyShouldBeUniqueForDifferentInstancesIfNotExplicitlyConfigured() {
    val policy1 = Policy.asyncRetry<Int>()
      .handleResult(0)
      .retry()
    val policy2 = Policy.asyncRetry<Int>()
      .handleResult(0)
      .retry()

    assertThat(policy1.policyKey).isNotEqualTo(policy2.policyKey)
  }

  @Test
  fun policyKeyPropertyShouldReturnConsistentValueForSamePolicyInstanceIfNotExplicitlyConfigured() {
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
  fun shouldPassPolicyKeyToExecutionContext() = runBlockingTest {
    val policyKey = UUID.randomUUID().toString()
    var policyKeySetOnExecutionContext: String? = null
    val onRetry: suspend (Result<TestResult>, Int, Context) -> Unit =
      { _, _, context -> policyKeySetOnExecutionContext = context.policyKey }
    val policy = Policy.asyncRetry<TestResult>()
      .handleResult(TestResult.FAULT)
      .policyKey(policyKey)
      .retry(1, onRetry)

    policy.raiseResults(TestResult.FAULT, TestResult.GOOD)

    assertThat(policyKeySetOnExecutionContext).isEqualTo(policyKey)
  }

  @Test
  fun shouldPassOperationKeyToExecutionContext() = runBlockingTest {
    val operationKey = "SomeKey"
    var operationKeySetOnContext: String? = null
    val onRetry: suspend (Result<TestResult>, Int, Context) -> Unit =
      { _, _, context -> operationKeySetOnContext = context.operationKey }
    val policy = Policy.asyncRetry<TestResult>()
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
  //</editor-fold>
}
