package hr.tjakopan.yarl.bulkhead

import hr.tjakopan.yarl.Context
import hr.tjakopan.yarl.Policy
import kotlinx.coroutines.sync.Semaphore
import kotlin.math.min

class BulkheadPolicy<R> internal constructor(policyBuilder: BulkheadPolicyBuilder<R>) :
  Policy<R, BulkheadPolicyBuilder<R>>(policyBuilder), IBulkheadPolicy {
  companion object BulkheadPolicy {
    @JvmStatic
    fun <R> builder(): BulkheadPolicyBuilder<R> = BulkheadPolicyBuilder()
  }

  private val maxParallelizationSemaphore: Semaphore =
    BulkheadSemaphoreFactory.createMaxParallelizationSemaphore(policyBuilder.maxParallelization)
  private val maxQueuedActionsSemaphore: Semaphore = BulkheadSemaphoreFactory.createMaxQueuedActionsSemaphore(
    policyBuilder.maxParallelization,
    policyBuilder.maxQueueingActions
  )
  private val maxQueueingActions: Int = policyBuilder.maxQueueingActions
  private val onBulkheadRejected: (Context) -> Unit = policyBuilder.onBulkheadRejected

  override val bulkheadAvailableCount: Int
    get() = maxParallelizationSemaphore.availablePermits
  override val queueAvailableCount: Int
    get() = min(maxQueuedActionsSemaphore.availablePermits, maxQueueingActions)

  override fun implementation(context: Context, action: (Context) -> R): R = BulkheadEngine.implementation(
    action,
    context,
    onBulkheadRejected,
    maxParallelizationSemaphore,
    maxQueuedActionsSemaphore
  )
}
