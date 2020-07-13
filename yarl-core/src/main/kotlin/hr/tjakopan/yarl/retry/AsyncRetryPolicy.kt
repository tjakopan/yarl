package hr.tjakopan.yarl.retry

import hr.tjakopan.yarl.AsyncPolicy
import hr.tjakopan.yarl.Context
import hr.tjakopan.yarl.DelegateResult
import java.time.Duration

class AsyncRetryPolicy<R> internal constructor(policyBuilder: AsyncRetryPolicyBuilder<R>) :
  AsyncPolicy<R, AsyncRetryPolicyBuilder<R>>(policyBuilder), IRetryPolicy {
  companion object AsyncRetryPolicy {
    @JvmStatic
    fun <R> builder() = AsyncRetryPolicyBuilder<R>()
  }

  private val onRetry: suspend (DelegateResult<R>, Duration, Int, Context) -> Unit = policyBuilder.onRetry
  private val permittedRetryCount: Int = policyBuilder.permittedRetryCount
  private val sleepDurationsIterable: Iterable<Duration> = policyBuilder.sleepDurationsIterable
  private val sleepDurationProvider: ((Int, DelegateResult<R>, Context) -> Duration)? =
    policyBuilder.sleepDurationProvider

  @JvmSynthetic
  override suspend fun implementation(context: Context, action: suspend (Context) -> R): R =
    RetryEngine.implementation(
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
