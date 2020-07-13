package hr.tjakopan.yarl.retry

import hr.tjakopan.yarl.Context
import hr.tjakopan.yarl.DelegateResult
import java.time.Duration

@JvmSuppressWildcards
class RetryPolicyBuilder<R> : RetryPolicyBuilderBase<R, RetryPolicyBuilder<R>>() {
  @JvmSynthetic
  internal var onRetry: (DelegateResult<R>, Duration, Int, Context) -> Unit = { _, _, _, _ -> Unit }

  fun retry(retryCount: Int, onRetry: (DelegateResult<R>, Int, Context) -> Unit): RetryPolicy<R> {
    if (retryCount < 0) throw IllegalArgumentException("Retry count must be greater than or equal to zero.")
    this.onRetry = { outcome, _, i, ctx -> onRetry(outcome, i, ctx) }
    this.permittedRetryCount = retryCount
    return RetryPolicy(this)
  }

  fun retry(retryCount: Int): RetryPolicy<R> {
    val doNothing: (DelegateResult<R>, Int, Context) -> Unit = { _, _, _ -> Unit }
    return retry(retryCount, doNothing)
  }

  fun retry(onRetry: (DelegateResult<R>, Int, Context) -> Unit): RetryPolicy<R> = retry(1, onRetry)

  fun retry(): RetryPolicy<R> = retry(1)

  fun retryForever(onRetry: (DelegateResult<R>, Int, Context) -> Unit): RetryPolicy<R> {
    this.onRetry = { outcome, _, i, ctx -> onRetry(outcome, i, ctx) }
    return RetryPolicy(this)
  }

  fun retryForever(): RetryPolicy<R> {
    val doNothing: (DelegateResult<R>, Int, Context) -> Unit = { _, _, _ -> Unit }
    return retryForever(doNothing)
  }

  fun waitAndRetry(
    retryCount: Int,
    sleepDurationProvider: (Int, DelegateResult<R>, Context) -> Duration,
    onRetry: (DelegateResult<R>, Duration, Int, Context) -> Unit
  ): RetryPolicy<R> {
    if (retryCount < 0) throw IllegalArgumentException("Retry count must be greater than or equal to zero.")
    this.permittedRetryCount = retryCount
    this.sleepDurationProvider = sleepDurationProvider
    this.onRetry = onRetry
    return RetryPolicy(this)
  }

  fun waitAndRetry(retryCount: Int, sleepDurationProvider: (Int, DelegateResult<R>, Context) -> Duration): RetryPolicy<R> {
    val doNothing: (DelegateResult<R>, Duration, Int, Context) -> Unit = { _, _, _, _ -> Unit }
    return waitAndRetry(retryCount, sleepDurationProvider, doNothing)
  }

  fun waitAndRetry(
    sleepDurations: Iterable<Duration>,
    onRetry: (DelegateResult<R>, Duration, Int, Context) -> Unit
  ): RetryPolicy<R> {
    this.sleepDurationsIterable = sleepDurations
    this.onRetry = onRetry
    return RetryPolicy(this)
  }

  fun waitAndRetry(sleepDurations: Iterable<Duration>): RetryPolicy<R> {
    val doNothing: (DelegateResult<R>, Duration, Int, Context) -> Unit = { _, _, _, _ -> Unit }
    return waitAndRetry(sleepDurations, doNothing)
  }

  fun waitAndRetryForever(
    sleepDurationProvider: (Int, DelegateResult<R>, Context) -> Duration,
    onRetry: (DelegateResult<R>, Duration, Int, Context) -> Unit
  ): RetryPolicy<R> {
    this.sleepDurationProvider = sleepDurationProvider
    this.onRetry = onRetry
    return RetryPolicy(this)
  }

  fun waitAndRetryForever(sleepDurationProvider: (Int, DelegateResult<R>, Context) -> Duration): RetryPolicy<R> {
    val doNothing: (DelegateResult<R>, Duration, Int, Context) -> Unit = { _, _, _, _ -> Unit }
    return waitAndRetryForever(sleepDurationProvider, doNothing)
  }

  override fun self(): RetryPolicyBuilder<R> = this
}
