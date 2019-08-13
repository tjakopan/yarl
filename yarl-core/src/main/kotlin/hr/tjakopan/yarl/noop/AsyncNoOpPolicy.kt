package hr.tjakopan.yarl.noop

import hr.tjakopan.yarl.AsyncPolicy
import hr.tjakopan.yarl.AsyncPolicyGeneric
import hr.tjakopan.yarl.Context
import java.util.concurrent.CompletionStage
import java.util.concurrent.Executor

/**
 * A noop policy that can be applied to asynchronous delegates.
 */
class AsyncNoOpPolicy internal constructor() : AsyncPolicy(), INoOpPolicy {
  override fun <TResult> implementationAsyncGeneric(
    context: Context,
    action: (Context) -> CompletionStage<TResult>
  ): CompletionStage<TResult> =
    NoOpEngine.implementationAsync(context, action)

  override fun <TResult> implementationAsyncGeneric(
    context: Context,
    executor: Executor,
    action: (Context, Executor) -> CompletionStage<TResult>
  ): CompletionStage<TResult> =
    NoOpEngine.implementationAsync(context, executor, action)
}

/**
 * A noop policy that can be applied to asynchronous delegates returning a value of type [TResult].
 */
class AsyncNoOpPolicyGeneric<TResult> internal constructor() : AsyncPolicyGeneric<TResult>(),
  INoOpPolicyGeneric<TResult> {
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
}
