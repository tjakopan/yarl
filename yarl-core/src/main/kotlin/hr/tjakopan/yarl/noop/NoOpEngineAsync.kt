package hr.tjakopan.yarl.noop

import hr.tjakopan.yarl.Context
import java.util.concurrent.CompletionStage
import java.util.concurrent.Executor

internal object NoOpEngineAsync {
  fun <TResult> implementationAsync(
    context: Context,
    action: (Context) -> CompletionStage<TResult?>
  ): CompletionStage<TResult?> = action(context)

  fun <TResult> implementationAsync(
    context: Context,
    executor: Executor,
    action: (Context, Executor) -> CompletionStage<TResult?>
  ): CompletionStage<TResult?> = action(context, executor)
}
