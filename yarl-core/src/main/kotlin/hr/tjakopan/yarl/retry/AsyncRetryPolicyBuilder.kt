package hr.tjakopan.yarl.retry

import hr.tjakopan.yarl.Context
import java.time.Duration

@JvmSuppressWildcards
class AsyncRetryPolicyBuilder<R> : RetryPolicyBuilderBase<R, AsyncRetryPolicyBuilder<R>>() {
  @JvmSynthetic
  internal var sleepDurationProvider: (suspend (Int, Result<R>, Context) -> Duration)? = null

  @JvmSynthetic
  internal var onRetry: suspend (Result<R>, Duration, Int, Context) -> Unit = { _, _, _, _ -> Unit }

  fun retry(retryCount: Int, onRetry: suspend (Result<R>, Int, Context) -> Unit): AsyncRetryPolicy<R> {
    if (retryCount < 0) throw IllegalArgumentException("Retry count must be greater than or equal to zero.")
    this.onRetry = { outcome, _, i, ctx -> onRetry(outcome, i, ctx) }
    this.permittedRetryCount = retryCount
    return AsyncRetryPolicy(this)
  }

  fun retry(retryCount: Int): AsyncRetryPolicy<R> {
    val doNothing: suspend (Result<R>, Int, Context) -> Unit = { _, _, _ -> Unit }
    return retry(retryCount, doNothing)
  }

  fun retry(onRetry: suspend (Result<R>, Int, Context) -> Unit): AsyncRetryPolicy<R> =
    retry(1, onRetry)

  fun retry(): AsyncRetryPolicy<R> = retry(1)

  fun retryForever(onRetry: suspend (Result<R>, Int, Context) -> Unit): AsyncRetryPolicy<R> {
    this.onRetry = { outcome, _, i, ctx -> onRetry(outcome, i, ctx) }
    return AsyncRetryPolicy(this)
  }

  fun retryForever(): AsyncRetryPolicy<R> {
    val doNothing: suspend (Result<R>, Int, Context) -> Unit = { _, _, _ -> Unit }
    return retryForever(doNothing)
  }

  fun waitAndRetry(
    retryCount: Int,
    sleepDurationProvider: suspend (Int, Result<R>, Context) -> Duration,
    onRetry: suspend (Result<R>, Duration, Int, Context) -> Unit
  ): AsyncRetryPolicy<R> {
    if (retryCount < 0) throw IllegalArgumentException("Retry count must be greater than or equal to zero.")
    this.permittedRetryCount = retryCount
    this.sleepDurationProvider = sleepDurationProvider
    this.onRetry = onRetry
    return AsyncRetryPolicy(this)
  }

  fun waitAndRetry(
    retryCount: Int,
    sleepDurationProvider: suspend (Int, Result<R>, Context) -> Duration
  ): AsyncRetryPolicy<R> {
    val doNothing: suspend (Result<R>, Duration, Int, Context) -> Unit = { _, _, _, _ -> Unit }
    return waitAndRetry(retryCount, sleepDurationProvider, doNothing)
  }

  fun waitAndRetry(
    sleepDurations: Iterable<Duration>,
    onRetry: suspend (Result<R>, Duration, Int, Context) -> Unit
  ): AsyncRetryPolicy<R> {
    this.sleepDurationsIterable = sleepDurations
    this.onRetry = onRetry
    return AsyncRetryPolicy(this)
  }

  fun waitAndRetry(sleepDurations: Iterable<Duration>): AsyncRetryPolicy<R> {
    val doNothing: suspend (Result<R>, Duration, Int, Context) -> Unit = { _, _, _, _ -> Unit }
    return waitAndRetry(sleepDurations, doNothing)
  }

  fun waitAndRetryForever(
    sleepDurationProvider: suspend (Int, Result<R>, Context) -> Duration,
    onRetry: suspend (Result<R>, Duration, Int, Context) -> Unit
  ): AsyncRetryPolicy<R> {
    this.sleepDurationProvider = sleepDurationProvider
    this.onRetry = onRetry
    return AsyncRetryPolicy(this)
  }

  fun waitAndRetryForever(sleepDurationProvider: suspend (Int, Result<R>, Context) -> Duration): AsyncRetryPolicy<R> {
    val doNothing: suspend (Result<R>, Duration, Int, Context) -> Unit = { _, _, _, _ -> Unit }
    return waitAndRetryForever(sleepDurationProvider, doNothing)
  }

  fun retry(retryCount: Int, onRetry: (Result<R>, Int, Context) -> Unit): AsyncRetryPolicy<R> {
    if (retryCount < 0) throw IllegalArgumentException("Retry count must be greater than or equal to zero.")
    this.onRetry = { outcome, _, i, ctx -> onRetry(outcome, i, ctx) }
    this.permittedRetryCount = retryCount
    return AsyncRetryPolicy(this)
  }

  fun retry(onRetry: (Result<R>, Int, Context) -> Unit): AsyncRetryPolicy<R> =
    retry(1, onRetry)

  fun retryForever(onRetry: (Result<R>, Int, Context) -> Unit): AsyncRetryPolicy<R> {
    this.onRetry = { outcome, _, i, ctx -> onRetry(outcome, i, ctx) }
    return AsyncRetryPolicy(this)
  }

  fun waitAndRetry(
    retryCount: Int,
    sleepDurationProvider: (Int, Result<R>, Context) -> Duration,
    onRetry: (Result<R>, Duration, Int, Context) -> Unit
  ): AsyncRetryPolicy<R> {
    if (retryCount < 0) throw IllegalArgumentException("Retry count must be greater than or equal to zero.")
    this.permittedRetryCount = retryCount
    this.sleepDurationProvider = { i, result, context -> sleepDurationProvider(i, result, context) }
    this.onRetry = { result, duration, i, context -> onRetry(result, duration, i, context) }
    return AsyncRetryPolicy(this)
  }

  fun waitAndRetry(
    retryCount: Int,
    sleepDurationProvider: (Int, Result<R>, Context) -> Duration
  ): AsyncRetryPolicy<R> {
    val doNothing: (Result<R>, Duration, Int, Context) -> Unit = { _, _, _, _ -> Unit }
    return waitAndRetry(retryCount, sleepDurationProvider, doNothing)
  }

  fun waitAndRetry(
    sleepDurations: Iterable<Duration>,
    onRetry: (Result<R>, Duration, Int, Context) -> Unit
  ): AsyncRetryPolicy<R> {
    this.sleepDurationsIterable = sleepDurations
    this.onRetry = { result, duration, i, context -> onRetry(result, duration, i, context) }
    return AsyncRetryPolicy(this)
  }

  fun waitAndRetryForever(
    sleepDurationProvider: (Int, Result<R>, Context) -> Duration,
    onRetry: (Result<R>, Duration, Int, Context) -> Unit
  ): AsyncRetryPolicy<R> {
    this.sleepDurationProvider = { i, result, context -> sleepDurationProvider(i, result, context) }
    this.onRetry = { result, duration, i, context -> onRetry(result, duration, i, context) }
    return AsyncRetryPolicy(this)
  }

  fun waitAndRetryForever(sleepDurationProvider: (Int, Result<R>, Context) -> Duration): AsyncRetryPolicy<R> {
    val doNothing: (Result<R>, Duration, Int, Context) -> Unit = { _, _, _, _ -> Unit }
    return waitAndRetryForever(sleepDurationProvider, doNothing)
  }

  override fun self(): AsyncRetryPolicyBuilder<R> = this
}
