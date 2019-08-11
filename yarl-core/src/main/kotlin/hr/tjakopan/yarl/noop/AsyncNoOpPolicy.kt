package hr.tjakopan.yarl.noop

import hr.tjakopan.yarl.AsyncPolicy
import hr.tjakopan.yarl.Context
import java.util.concurrent.CompletionStage
import java.util.concurrent.Executor

/**
 * A noop policy that can be applied to asynchronous delegates.
 */
class AsyncNoOpPolicy internal constructor() : AsyncPolicy(), INoOpPolicy {
  override fun implementationAsync(
    context: Context,
    action: (Context) -> CompletionStage<Unit>
  ): CompletionStage<Unit> =
    NoOpEngineAsync.implementationAsync(context, action)

  override fun implementationAsync(
    context: Context,
    executor: Executor,
    action: (Context, Executor) -> CompletionStage<Unit>
  ): CompletionStage<Unit> =
    NoOpEngineAsync.implementationAsync(context, executor, action)

  override fun <TResult> implementationAsyncGeneric(
    context: Context,
    action: (Context) -> CompletionStage<TResult?>
  ): CompletionStage<TResult?> =
    NoOpEngineAsync.implementationAsyncGeneric(context, action)

  override fun <TResult> implementationAsyncGeneric(
    context: Context,
    executor: Executor,
    action: (Context, Executor) -> CompletionStage<TResult?>
  ): CompletionStage<TResult?> =
    NoOpEngineAsync.implementationAsyncGeneric(context, executor, action)

  companion object {
    /**
     * Builds a NoOp [AsyncPolicy] that will execute without any custom behavior.
     *
     * @return The policy instance.
     */
    @JvmStatic
    fun noOpAsync() = AsyncNoOpPolicy()
  }
}
