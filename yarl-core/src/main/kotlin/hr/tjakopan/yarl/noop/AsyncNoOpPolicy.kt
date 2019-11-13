package hr.tjakopan.yarl.noop

import hr.tjakopan.yarl.AsyncPolicy
import hr.tjakopan.yarl.Context
import hr.tjakopan.yarl.Policy
import java.util.concurrent.CompletionStage
import java.util.concurrent.Executor

/**
 * A noop policy that can be applied to asynchronous delegates returning a value of type [TResult].
 */
class AsyncNoOpPolicy<TResult> private constructor(policyBuilder: Builder<TResult>) :
  AsyncPolicy<TResult, AsyncNoOpPolicy.Builder<TResult>>(policyBuilder), INoOpPolicy {
  override fun implementationAsync(
    context: Context,
    action: (Context) -> CompletionStage<TResult>
  ): CompletionStage<TResult> =
    NoOpEngine.implementationAsync(context, action)

  override fun implementationAsync(
    context: Context,
    executor: Executor,
    action: (Context, Executor) -> CompletionStage<TResult>
  ): CompletionStage<TResult> =
    NoOpEngine.implementationAsync(context, executor, action)

  class Builder<TResult> : Policy.Builder<TResult, Builder<TResult>>() {
    override fun self(): Builder<TResult> = this

    fun build() = AsyncNoOpPolicy(this)
  }
}
