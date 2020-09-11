package hr.tjakopan.yarl.bulkhead

import hr.tjakopan.yarl.Context
import kotlinx.coroutines.future.await
import java.util.concurrent.CompletableFuture

class AsyncBulkheadPolicyBuilder<R> : BulkheadPolicyBuilderBase<R, AsyncBulkheadPolicyBuilder<R>>() {
  @JvmSynthetic
  internal var onBulkheadRejected: suspend (Context) -> Unit = { _ -> }

  @JvmSynthetic
  fun bulkhead(
    maxParallelization: Int,
    maxQueueingActions: Int = 0,
    onBulkheadRejected: suspend (Context) -> Unit = { _ -> }
  ): AsyncBulkheadPolicy<R> {
    if (maxParallelization <= 0) throw IllegalArgumentException("Max parallelization must be greater than zero.")
    if (maxQueueingActions < 0) throw IllegalArgumentException("Max queueing actions must be greater than or equal to zero.")
    this.maxParallelization = maxParallelization
    this.maxQueueingActions = maxQueueingActions
    this.onBulkheadRejected = onBulkheadRejected
    return AsyncBulkheadPolicy(this)
  }

  @JvmSynthetic
  fun bulkhead(maxParallelization: Int, onBulkheadRejected: suspend (Context) -> Unit): AsyncBulkheadPolicy<R> =
    bulkhead(maxParallelization, 0, onBulkheadRejected)

  @JvmName("bulkhead")
  @JvmOverloads
  fun bulkheadAsync(
    maxParallelization: Int,
    maxQueueingActions: Int = 0,
    onBulkheadRejected: (Context) -> CompletableFuture<Unit> = { _ -> CompletableFuture.completedFuture(Unit) }
  ): AsyncBulkheadPolicy<R> =
    bulkhead(maxParallelization, maxQueueingActions) { context -> onBulkheadRejected(context).await() }

  @JvmName("bulkhead")
  fun bulkheadAsync(
    maxParallelization: Int,
    onBulkheadRejected: (Context) -> CompletableFuture<Unit>
  ): AsyncBulkheadPolicy<R> =
    bulkheadAsync(maxParallelization, 0, onBulkheadRejected)

  override fun self(): AsyncBulkheadPolicyBuilder<R> = this
}
