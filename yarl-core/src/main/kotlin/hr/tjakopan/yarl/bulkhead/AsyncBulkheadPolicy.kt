package hr.tjakopan.yarl.bulkhead

import hr.tjakopan.yarl.AsyncPolicy
import hr.tjakopan.yarl.Context
import kotlinx.coroutines.sync.Semaphore
import kotlin.math.min

class AsyncBulkheadPolicy<R> internal constructor(policyBuilder: AsyncBulkheadPolicyBuilder<R>) :
  AsyncPolicy<R, AsyncBulkheadPolicyBuilder<R>>(policyBuilder), IBulkheadPolicy {
  private val maxParallelizationSemaphore: Semaphore =
    BulkheadSemaphoreFactory.createMaxParallelizationSemaphore(policyBuilder.maxParallelization)
  private val maxQueuedActionsSemaphore: Semaphore = BulkheadSemaphoreFactory.createMaxQueuedActionsSemaphore(
    policyBuilder.maxParallelization,
    policyBuilder.maxQueueingActions
  )
  private val maxQueueingActions: Int = policyBuilder.maxQueueingActions
  private val onBulkheadRejected: suspend (Context) -> Unit = policyBuilder.onBulkheadRejected

  override val bulkheadAvailableCount: Int
    get() = maxParallelizationSemaphore.availablePermits
  override val queueAvailableCount: Int
    get() = min(maxQueuedActionsSemaphore.availablePermits, maxQueueingActions)

  override suspend fun implementation(context: Context, action: suspend (Context) -> R): R =
    BulkheadEngine.implementation(
      action,
      context,
      onBulkheadRejected,
      maxParallelizationSemaphore,
      maxQueuedActionsSemaphore
    )
}
