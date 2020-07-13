package hr.tjakopan.yarl.retry

import hr.tjakopan.yarl.Context
import hr.tjakopan.yarl.DelegateResult
import kotlinx.coroutines.future.await
import java.time.Duration
import java.util.concurrent.CompletableFuture

@JvmSuppressWildcards
class AsyncRetryPolicyBuilder<R> : RetryPolicyBuilderBase<R, AsyncRetryPolicyBuilder<R>>() {
  @JvmSynthetic
  internal var onRetry: suspend (DelegateResult<R>, Duration, Int, Context) -> Unit = { _, _, _, _ -> Unit }

  @JvmSynthetic
  fun retry(retryCount: Int, onRetry: suspend (DelegateResult<R>, Int, Context) -> Unit): AsyncRetryPolicy<R> {
    if (retryCount < 0) throw IllegalArgumentException("Retry count must be greater than or equal to zero.")
    this.onRetry = { outcome, _, i, ctx -> onRetry(outcome, i, ctx) }
    this.permittedRetryCount = retryCount
    return AsyncRetryPolicy(this)
  }

  fun retry(retryCount: Int): AsyncRetryPolicy<R> {
    val doNothing: suspend (DelegateResult<R>, Int, Context) -> Unit = { _, _, _ -> Unit }
    return retry(retryCount, doNothing)
  }

  @JvmSynthetic
  fun retry(onRetry: suspend (DelegateResult<R>, Int, Context) -> Unit): AsyncRetryPolicy<R> =
    retry(1, onRetry)

  fun retry(): AsyncRetryPolicy<R> = retry(1)

  @JvmName("retry")
  fun retryAsync(
    retryCount: Int,
    onRetry: (DelegateResult<R>, Int, Context) -> CompletableFuture<Unit>
  ): AsyncRetryPolicy<R> {
    if (retryCount < 0) throw IllegalArgumentException("Retry count must be greater than or equal to zero.")
    this.onRetry = { outcome, _, i, ctx -> onRetry(outcome, i, ctx).await() }
    this.permittedRetryCount = retryCount
    return AsyncRetryPolicy(this)
  }

  @JvmName("retry")
  fun retryAsync(onRetry: (DelegateResult<R>, Int, Context) -> CompletableFuture<Unit>): AsyncRetryPolicy<R> =
    retryAsync(1, onRetry)

  @JvmSynthetic
  fun retryForever(onRetry: suspend (DelegateResult<R>, Int, Context) -> Unit): AsyncRetryPolicy<R> {
    this.onRetry = { outcome, _, i, ctx -> onRetry(outcome, i, ctx) }
    return AsyncRetryPolicy(this)
  }

  fun retryForever(): AsyncRetryPolicy<R> {
    val doNothing: suspend (DelegateResult<R>, Int, Context) -> Unit = { _, _, _ -> Unit }
    return retryForever(doNothing)
  }

  fun retryForeverAsync(onRetry: (DelegateResult<R>, Int, Context) -> CompletableFuture<Unit>): AsyncRetryPolicy<R> {
    this.onRetry = { outcome, _, i, ctx -> onRetry(outcome, i, ctx).await() }
    return AsyncRetryPolicy(this)
  }

  @JvmSynthetic
  fun waitAndRetry(
    retryCount: Int,
    sleepDurationProvider: (Int, DelegateResult<R>, Context) -> Duration,
    onRetry: suspend (DelegateResult<R>, Duration, Int, Context) -> Unit
  ): AsyncRetryPolicy<R> {
    if (retryCount < 0) throw IllegalArgumentException("Retry count must be greater than or equal to zero.")
    this.permittedRetryCount = retryCount
    this.sleepDurationProvider = sleepDurationProvider
    this.onRetry = onRetry
    return AsyncRetryPolicy(this)
  }

  fun waitAndRetry(
    retryCount: Int,
    sleepDurationProvider: (Int, DelegateResult<R>, Context) -> Duration
  ): AsyncRetryPolicy<R> {
    val doNothing: suspend (DelegateResult<R>, Duration, Int, Context) -> Unit = { _, _, _, _ -> Unit }
    return waitAndRetry(retryCount, sleepDurationProvider, doNothing)
  }

  @JvmSynthetic
  fun waitAndRetry(
    sleepDurations: Iterable<Duration>,
    onRetry: suspend (DelegateResult<R>, Duration, Int, Context) -> Unit
  ): AsyncRetryPolicy<R> {
    this.sleepDurationsIterable = sleepDurations
    this.onRetry = onRetry
    return AsyncRetryPolicy(this)
  }

  fun waitAndRetry(sleepDurations: Iterable<Duration>): AsyncRetryPolicy<R> {
    val doNothing: suspend (DelegateResult<R>, Duration, Int, Context) -> Unit = { _, _, _, _ -> Unit }
    return waitAndRetry(sleepDurations, doNothing)
  }

  fun waitAndRetryAsync(
    retryCount: Int,
    sleepDurationProvider: (Int, DelegateResult<R>, Context) -> Duration,
    onRetry: (DelegateResult<R>, Duration, Int, Context) -> CompletableFuture<Unit>
  ): AsyncRetryPolicy<R> {
    if (retryCount < 0) throw IllegalArgumentException("Retry count must be greater than or equal to zero.")
    this.permittedRetryCount = retryCount
    this.sleepDurationProvider = { i, result, context -> sleepDurationProvider(i, result, context) }
    this.onRetry = { result, duration, i, context -> onRetry(result, duration, i, context).await() }
    return AsyncRetryPolicy(this)
  }

  fun waitAndRetryAsync(
    sleepDurations: Iterable<Duration>,
    onRetry: (DelegateResult<R>, Duration, Int, Context) -> CompletableFuture<Unit>
  ): AsyncRetryPolicy<R> {
    this.sleepDurationsIterable = sleepDurations
    this.onRetry = { result, duration, i, context -> onRetry(result, duration, i, context).await() }
    return AsyncRetryPolicy(this)
  }

  @JvmSynthetic
  fun waitAndRetryForever(
    sleepDurationProvider: (Int, DelegateResult<R>, Context) -> Duration,
    onRetry: suspend (DelegateResult<R>, Duration, Int, Context) -> Unit
  ): AsyncRetryPolicy<R> {
    this.sleepDurationProvider = sleepDurationProvider
    this.onRetry = onRetry
    return AsyncRetryPolicy(this)
  }

  fun waitAndRetryForever(sleepDurationProvider: (Int, DelegateResult<R>, Context) -> Duration): AsyncRetryPolicy<R> {
    val doNothing: suspend (DelegateResult<R>, Duration, Int, Context) -> Unit = { _, _, _, _ -> Unit }
    return waitAndRetryForever(sleepDurationProvider, doNothing)
  }

  fun waitAndRetryForeverAsync(
    sleepDurationProvider: (Int, DelegateResult<R>, Context) -> Duration,
    onRetry: (DelegateResult<R>, Duration, Int, Context) -> CompletableFuture<Unit>
  ): AsyncRetryPolicy<R> {
    this.sleepDurationProvider = { i, result, context -> sleepDurationProvider(i, result, context) }
    this.onRetry = { result, duration, i, context -> onRetry(result, duration, i, context).await() }
    return AsyncRetryPolicy(this)
  }

  override fun self(): AsyncRetryPolicyBuilder<R> = this
}
