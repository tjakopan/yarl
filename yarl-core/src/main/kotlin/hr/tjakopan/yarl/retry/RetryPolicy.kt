package hr.tjakopan.yarl.retry

import hr.tjakopan.yarl.Context
import hr.tjakopan.yarl.DelegateResult
import hr.tjakopan.yarl.Policy
import java.time.Duration

class RetryPolicy<R> internal constructor(policyBuilder: RetryPolicyBuilder<R>) :
  Policy<R, RetryPolicyBuilder<R>>(policyBuilder), IRetryPolicy {
  companion object RetryPolicy {
    @JvmStatic
    fun <R> builder(): RetryPolicyBuilder<R> = RetryPolicyBuilder()
  }

  private val onRetry: (DelegateResult<R>, Duration, Int, Context) -> Unit = policyBuilder.onRetry
  private val permittedRetryCount: Int = policyBuilder.permittedRetryCount
  private val sleepDurationsIterable: Iterable<Duration> = policyBuilder.sleepDurationsIterable
  private val sleepDurationProvider: ((Int, DelegateResult<R>, Context) -> Duration)? =
    policyBuilder.sleepDurationProvider

  override fun implementation(context: Context, action: (Context) -> R): R = RetryEngine.implementation(
    action,
    context,
    exceptionPredicates,
    resultPredicates,
    onRetry,
    permittedRetryCount,
    sleepDurationsIterable,
    sleepDurationProvider
  )
}
