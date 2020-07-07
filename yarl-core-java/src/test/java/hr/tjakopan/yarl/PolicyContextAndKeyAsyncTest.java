package hr.tjakopan.yarl;

import hr.tjakopan.yarl.retry.AsyncRetryPolicy;
import hr.tjakopan.yarl.test.helpers.AsyncPolicyUtils;
import hr.tjakopan.yarl.test.helpers.TestResult;
import kotlin.Result;
import kotlin.Unit;
import kotlin.jvm.functions.Function3;
import org.junit.Test;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

public class PolicyContextAndKeyAsyncTest {
  //<editor-fold desc="configuration">
  @Test
  public void shouldBeAbleFluentlyToConfigurePolicyKey() {
    final var policy = AsyncRetryPolicy.<Integer>builder()
      .handleResult(0)
      .policyKey(UUID.randomUUID().toString())
      .retry();

    assertThat(policy).isInstanceOf(AsyncPolicy.class);
  }

  @Test
  public void policyKeyPropertyShouldBeTheFluentlyConfiguredPolicyKey() {
    final var key = "SomePolicyKey";
    final var policy = AsyncRetryPolicy.<Integer>builder()
      .handleResult(0)
      .policyKey(key)
      .retry();

    assertThat(policy.getPolicyKey()).isEqualTo(key);
  }

  @Test
  public void policyKeyPropertyShouldBeNonNullOrEmptyIfNotExplicitlyConfigured() {
    final var policy = AsyncRetryPolicy.<Integer>builder()
      .handleResult(0)
      .retry();

    assertThat(policy.getPolicyKey()).isNotNull();
    assertThat(policy.getPolicyKey()).isNotEmpty();
  }

  @Test
  public void policyKeyPropertyShouldStartWithPolicyTypeIfNotExplicitlyConfigured() {
    final var policy = AsyncRetryPolicy.<Integer>builder()
      .handleResult(0)
      .retry();

    assertThat(policy.getPolicyKey()).startsWith("AsyncRetryPolicy");
  }

  @Test
  public void policyKeyPropertyShouldBeUniqueForDifferentInstancesIfNotExplicitlyConfigured() {
    final var policy1 = AsyncRetryPolicy.<Integer>builder()
      .handleResult(0)
      .retry();
    final var policy2 = AsyncRetryPolicy.<Integer>builder()
      .handleResult(0)
      .retry();

    assertThat(policy1.getPolicyKey()).isNotEqualTo(policy2.getPolicyKey());
  }

  @Test
  public void policyKeyPropertyShouldReturnConsistentValueForSamePolicyInstanceIfNotExplicitlyConfigured() {
    final var policy = AsyncRetryPolicy.<Integer>builder()
      .handleResult(0)
      .retry();

    final var keyRetrievedFirst = policy.getPolicyKey();
    final var keyRetrievedSecond = policy.getPolicyKey();

    assertThat(keyRetrievedSecond).isSameAs(keyRetrievedFirst);
  }
  //</editor-fold>

  //<editor-fold desc="policyKey and execution contexts tests">
  @Test
  public void shouldPassPolicyKeyToExecutionContext() {
    final var policyKey = UUID.randomUUID().toString();
    final AtomicReference<String> policyKeySetOnExecutionContext = new AtomicReference<>();
    final Function3<Result<TestResult>, Integer, Context, Unit> onRetry = (result, retryCount, context) -> {
      policyKeySetOnExecutionContext.set(context.getPolicyKey());
      return Unit.INSTANCE;
    };
    final var policy = AsyncRetryPolicy.<TestResult>builder()
      .handleResult(TestResult.FAULT)
      .policyKey(policyKey)
      .retry(1, onRetry);

    AsyncPolicyUtils.raiseResults(policy, TestResult.FAULT, TestResult.GOOD);

    assertThat(policyKeySetOnExecutionContext.get()).isEqualTo(policyKey);
  }

  @Test
  public void shouldPassOperationKeyToExecutionContext() {
    final var operationKey = "SomeKey";
    final AtomicReference<String> operationKeySetOnContext = new AtomicReference<>();
    final Function3<Result<TestResult>, Integer, Context, Unit> onRetry = (result, retryCount, context) -> {
      operationKeySetOnContext.set(context.getOperationKey());
      return Unit.INSTANCE;
    };
    final var policy = AsyncRetryPolicy.<TestResult>builder()
      .handleResult(TestResult.FAULT)
      .retry(1, onRetry);

    final AtomicBoolean firstExecution = new AtomicBoolean(true);
    final var context = Context.builder()
      .operationKey(operationKey)
      .build();
    policy.executeAsync(context, ctx -> {
      if (firstExecution.get()) {
        firstExecution.set(false);
        return TestResult.FAULT;
      }
      return TestResult.GOOD;
    })
      .join();

    assertThat(operationKeySetOnContext.get()).isEqualTo(operationKey);
  }
  //</editor-fold>
}
