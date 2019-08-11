package hr.tjakopan.yarl.noop

import hr.tjakopan.yarl.AsyncPolicy
import hr.tjakopan.yarl.Context
import java.util.concurrent.CompletionStage
import java.util.concurrent.Executor

/**
 * A noop policy that can be applied to asynchronous delegates returning a value of type [TResult].
 */
class AsyncNoOpPolicy<TResult> internal constructor() : AsyncPolicy<TResult>(), INoOpPolicy {
  override fun implementationAsync(
    context: Context,
    action: (Context) -> CompletionStage<TResult?>
  ): CompletionStage<TResult?> =
    NoOpEngineAsync.implementationAsync(context, action)

  override fun implementationAsync(
    context: Context,
    executor: Executor,
    action: (Context, Executor) -> CompletionStage<TResult?>
  ): CompletionStage<TResult?> =
    NoOpEngineAsync.implementationAsync(context, executor, action)

  companion object {
    /**
     * Builds a NoOp [AsyncPolicy] that will execute without any custom behavior.
     *
     * @param TResult The type of return values this policy will handle.
     * @return The policy instance.
     */
    @JvmStatic
    fun <TResult> noOpAsync() = AsyncNoOpPolicy<TResult>()
  }
}
