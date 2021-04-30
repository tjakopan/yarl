package hr.tjakopan.yarl;

import hr.tjakopan.yarl.retry.RetryPolicy;
import hr.tjakopan.yarl.test.helpers.PolicyUtils;
import hr.tjakopan.yarl.test.helpers.TestResult;
import kotlin.Unit;
import kotlin.jvm.functions.Function3;
import org.junit.Test;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static hr.tjakopan.yarl.Functions.fromConsumer3;
import static org.assertj.core.api.Assertions.assertThat;

public class PolicyContextAndKeyTest {
  //<editor-fold desc="configuration">
  @Test
  public void shouldBeAbleFluentlyToConfigureThePolicyKey() {
    final var policy = RetryPolicy.<Integer>builder()
      .policyKey(UUID.randomUUID().toString())
      .handleResult(0)
      .retry();

    assertThat(policy).isInstanceOf(Policy.class);
  }

  @Test
  public void policyKeyPropertyShouldBeTheFluentlyConfiguredPolicyKey() {
    final var key = "SomePolicyKey";
    final var policy = RetryPolicy.<Integer>builder()
      .handleResult(0)
      .policyKey(key)
      .retry();

    assertThat(policy.getPolicyKey()).isEqualTo(key);
  }

  @Test
  public void policyKeyPropertyShouldBeNonNullOrEmptyIfNotExplicitlyConfigured() {
    final var policy = RetryPolicy.<Integer>builder()
      .handleResult(0)
      .retry();

    assertThat(policy.getPolicyKey()).isNotNull();
    assertThat(policy.getPolicyKey()).isNotEmpty();
  }

  @Test
  public void policyKeyPropertyShouldStartWithPolicyTypeIfNotExplicitlyConfigured() {
    final var policy = RetryPolicy.<Integer>builder()
      .handleResult(0)
      .retry();

    assertThat(policy.getPolicyKey()).startsWith("RetryPolicy");
  }

  @Test
  public void policyKeyPropertyShouldBeUniqueForDifferentInstancesIfNotExplicitlyConfigured() {
    final var policy1 = RetryPolicy.<Integer>builder()
      .handleResult(0)
      .retry();
    final var policy2 = RetryPolicy.<Integer>builder()
      .handleResult(0)
      .retry();

    assertThat(policy1.getPolicyKey()).isNotEqualTo(policy2.getPolicyKey());
  }

  @Test
  public void policyKeyPropertyShouldReturnConsistentValueForSamePolicyInstanceIfNotExplicitlyConfigured() {
    final var policy = RetryPolicy.<Integer>builder()
      .handleResult(0)
      .retry();

    final var keyRetrievedFirst = policy.getPolicyKey();
    final var keyRetrievedSecond = policy.getPolicyKey();

    assertThat(keyRetrievedSecond).isSameAs(keyRetrievedFirst);
  }

  @Test
  public void shouldPassPolicyKeyToExecutionContext() {
    final var policyKey = UUID.randomUUID().toString();
    final AtomicReference<String> policyKeySetOnExecutionContext = new AtomicReference<>();
    final Function3<DelegateResult<TestResult>, Integer, Context, Unit> onRetry =
      fromConsumer3(result -> (retryCount, context) -> policyKeySetOnExecutionContext.set(context.getPolicyKey()));
    final var policy = RetryPolicy.<TestResult>builder()
      .handleResult(TestResult.FAULT)
      .policyKey(policyKey)
      .retry(1, onRetry);

    PolicyUtils.raiseResults(policy, TestResult.FAULT, TestResult.GOOD);

    assertThat(policyKeySetOnExecutionContext.get()).isEqualTo(policyKey);
  }
  //</editor-fold>

  @Test
  public void shouldPassOperationKeyToExecutionContext() {
    final var operationKey = "SomeKey";
    final AtomicReference<String> operationKeySetOnContext = new AtomicReference<>();
    final Function3<DelegateResult<TestResult>, Integer, Context, Unit> onRetry =
      fromConsumer3(result -> (retryCount, context) -> operationKeySetOnContext.set(context.getOperationKey()));
    final var policy = RetryPolicy.<TestResult>builder()
      .handleResult(TestResult.FAULT)
      .retry(1, onRetry);

    final AtomicBoolean firstExecution = new AtomicBoolean(true);
    final var context = Context.builder()
      .operationKey(operationKey)
      .build();
    policy.execute(context, ctx -> {
      if (firstExecution.get()) {
        firstExecution.set(false);
        return TestResult.FAULT;
      }
      return TestResult.GOOD;
    });

    assertThat(operationKeySetOnContext.get()).isEqualTo(operationKey);
  }
}
