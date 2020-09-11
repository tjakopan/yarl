package hr.tjakopan.yarl.bulkhead

import hr.tjakopan.yarl.Context

@JvmSuppressWildcards
class BulkheadPolicyBuilder<R> : BulkheadPolicyBuilderBase<R, BulkheadPolicyBuilder<R>>() {
  @JvmSynthetic
  internal var onBulkheadRejected: (Context) -> Unit = { _ -> }

  @JvmOverloads
  fun bulkhead(
    maxParallelization: Int,
    maxQueueingActions: Int = 0,
    onBulkheadRejected: (Context) -> Unit = {_ ->}
  ): BulkheadPolicy<R> {
    if (maxParallelization <= 0) throw IllegalArgumentException("Max parallelization must be greater than zero.")
    if (maxQueueingActions < 0) throw IllegalArgumentException("Max queueing actions must be greater than or equal to zero.")
    this.maxParallelization = maxParallelization
    this.maxQueueingActions = maxQueueingActions
    this.onBulkheadRejected = onBulkheadRejected
    return BulkheadPolicy(this)
  }

  fun bulkhead(maxParallelization: Int, onBulkheadRejected: (Context) -> Unit): BulkheadPolicy<R> =
    bulkhead(maxParallelization, 0, onBulkheadRejected)

//  fun bulkhead(maxParallelization: Int, maxQueueingActions: Int): BulkheadPolicy<R> =
//    bulkhead(maxParallelization, maxQueueingActions) { _ -> }
//
//  fun bulkhead(maxParallelization: Int, onBulkheadRejected: (Context) -> Unit): BulkheadPolicy<R> =
//    bulkhead(maxParallelization, 0, onBulkheadRejected)
//
//  fun bulkhead(maxParallelization: Int): BulkheadPolicy<R> = bulkhead(maxParallelization, 0) { _ -> }

  override fun self(): BulkheadPolicyBuilder<R> = this
}
