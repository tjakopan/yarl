package hr.tjakopan.yarl.noop

import hr.tjakopan.yarl.Context
import java.util.concurrent.CompletionStage
import java.util.concurrent.Executor

internal object NoOpEngineAsync {
  fun implementationAsync(
    context: Context,
    action: (Context) -> CompletionStage<Unit>
  ): CompletionStage<Unit> = action(context)

  fun implementationAsync(
    context: Context,
    executor: Executor,
    action: (Context, Executor) -> CompletionStage<Unit>
  ): CompletionStage<Unit> = action(context, executor)

  fun <TResult> implementationAsyncGeneric(
    context: Context,
    action: (Context) -> CompletionStage<TResult?>
  ): CompletionStage<TResult?> = action(context)

  fun <TResult> implementationAsyncGeneric(
    context: Context,
    executor: Executor,
    action: (Context, Executor) -> CompletionStage<TResult?>
  ): CompletionStage<TResult?> = action(context, executor)
}
