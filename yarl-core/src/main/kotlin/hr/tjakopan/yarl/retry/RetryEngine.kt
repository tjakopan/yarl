package hr.tjakopan.yarl.retry

import hr.tjakopan.yarl.Context
import hr.tjakopan.yarl.ExceptionPredicates
import hr.tjakopan.yarl.ResultPredicates
import kotlinx.coroutines.delay
import java.time.Duration

internal object RetryEngine {
  @JvmSynthetic
  fun canRetry(
    tryCount: Int,
    permittedRetryCount: Int,
    sleepDurationsIterator: Iterator<Duration>,
    sleepDurationsCount: Int
  ): Boolean =
    tryCount < permittedRetryCount && (sleepDurationsIterator.hasNext() || sleepDurationsCount == 0)

  @JvmSynthetic
  fun <R> shouldHandleResult(result: R, shouldRetryResultPredicates: ResultPredicates<R>): Boolean =
    shouldRetryResultPredicates.anyMatch(result)

  @JvmSynthetic
  fun shouldHandleException(e: Throwable, shouldRetryExceptionPredicates: ExceptionPredicates): Boolean =
    shouldRetryExceptionPredicates.firstMatchOrNull(e) != null

  @JvmSynthetic
  fun <R> implementation(
    action: (Context) -> R,
    context: Context,
    shouldRetryExceptionPredicates: ExceptionPredicates,
    shouldRetryResultPredicates: ResultPredicates<R>,
    onRetry: (Result<R>, Duration, Int, Context) -> Unit,
    permittedRetryCount: Int = Int.MAX_VALUE,
    sleepDurationsIterable: Iterable<Duration> = listOf(),
    sleepDurationProvider: ((Int, Result<R>, Context) -> Duration)? = null
  ): R {
    var tryCount = 0
    val sleepDurationsIterator = sleepDurationsIterable.iterator()

    while (true) {
      val outcome = Result.runCatching { action(context) }
      val canRetry = canRetry(tryCount, permittedRetryCount, sleepDurationsIterator, sleepDurationsIterable.count())

      if (!canRetry) {
        return outcome.getOrThrow()
      }

      outcome.onSuccess { result ->
        if (!shouldHandleResult(result, shouldRetryResultPredicates)) {
          return result
        }
      }
        .onFailure { e ->
          if (!shouldHandleException(e, shouldRetryExceptionPredicates)) {
            throw e
          }
        }

      if (tryCount < Int.MAX_VALUE) {
        tryCount++
      }

      val waitDuration = when {
        sleepDurationsIterator.hasNext() -> sleepDurationsIterator.next()
        else -> sleepDurationProvider?.invoke(tryCount, outcome, context) ?: Duration.ZERO
      }
      onRetry(outcome, waitDuration, tryCount, context)
      if (waitDuration > Duration.ZERO) {
        Thread.sleep(waitDuration.toMillis())
      }
    }
  }

  @JvmSynthetic
  suspend fun <R> implementation(
    action: suspend (Context) -> R,
    context: Context,
    shouldRetryExceptionPredicates: ExceptionPredicates,
    shouldRetryResultPredicates: ResultPredicates<R>,
    onRetry: suspend (Result<R>, Duration, Int, Context) -> Unit,
    permittedRetryCount: Int = Int.MAX_VALUE,
    sleepDurationsIterable: Iterable<Duration> = listOf(),
    sleepDurationProvider: (suspend (Int, Result<R>, Context) -> Duration)? = null
  ): R {
    var tryCount = 0
    val sleepDurationsIterator = sleepDurationsIterable.iterator()

    while (true) {
      val outcome = Result.runCatching { action(context) }
      val canRetry = canRetry(tryCount, permittedRetryCount, sleepDurationsIterator, sleepDurationsIterable.count())

      if (!canRetry) {
        return outcome.getOrThrow()
      }

      outcome.onSuccess { result ->
        if (!shouldHandleResult(result, shouldRetryResultPredicates)) {
          return result
        }
      }
        .onFailure { e ->
          if (!shouldHandleException(e, shouldRetryExceptionPredicates)) {
            throw e
          }
        }

      if (tryCount < Int.MAX_VALUE) {
        tryCount++
      }

      val waitDuration = when {
        sleepDurationsIterator.hasNext() -> sleepDurationsIterator.next()
        else -> sleepDurationProvider?.invoke(tryCount, outcome, context) ?: Duration.ZERO
      }
      onRetry(outcome, waitDuration, tryCount, context)
      if (waitDuration > Duration.ZERO) {
        delay(waitDuration.toMillis())
      }
    }
  }
}
