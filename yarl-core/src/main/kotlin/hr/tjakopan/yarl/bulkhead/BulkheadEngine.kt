package hr.tjakopan.yarl.bulkhead

import hr.tjakopan.yarl.Context
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit

internal object BulkheadEngine {
  @JvmSynthetic
  fun <R> implementation(
    action: (Context) -> R,
    context: Context,
    onBulkheadRejected: (Context) -> Unit,
    maxParallelizationSemaphore: Semaphore,
    maxQueuedActionsSemaphore: Semaphore
  ): R {
    if (!maxQueuedActionsSemaphore.tryAcquire()) {
      onBulkheadRejected(context)
      throw BulkheadRejectedException()
    }
    try {
      return runBlocking {
        maxParallelizationSemaphore.withPermit { action(context) }
      }
    } finally {
      maxQueuedActionsSemaphore.release()
    }
  }

  @JvmSynthetic
  suspend fun <R> implementation(
    action: suspend (Context) -> R,
    context: Context,
    onBulkheadRejected: suspend (Context) -> Unit,
    maxParallelizationSemaphore: Semaphore,
    maxQueuedActionsSemaphore: Semaphore
  ): R {
    if (!maxQueuedActionsSemaphore.tryAcquire()) {
      onBulkheadRejected(context)
      throw BulkheadRejectedException()
    }
    try {
      return maxParallelizationSemaphore.withPermit { action(context) }
    } finally {
      maxQueuedActionsSemaphore.release()
    }
  }
}
