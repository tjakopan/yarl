package hr.tjakopan.yarl.bulkhead

import kotlinx.coroutines.sync.Semaphore

internal object BulkheadSemaphoreFactory {
  @JvmSynthetic
  fun createMaxParallelizationSemaphore(maxParallelization: Int): Semaphore = Semaphore(maxParallelization)

  @JvmSynthetic
  fun createMaxQueuedActionsSemaphore(maxParallelization: Int, maxQueueingActions: Int): Semaphore {
    val maxQueueingCompounded = when (maxQueueingActions <= Int.MAX_VALUE - maxParallelization) {
      true -> maxQueueingActions + maxParallelization
      else -> Int.MAX_VALUE
    }
    return Semaphore(maxQueueingCompounded)
  }
}
