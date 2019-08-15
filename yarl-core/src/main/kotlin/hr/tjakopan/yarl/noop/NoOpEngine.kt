package hr.tjakopan.yarl.noop

import hr.tjakopan.yarl.Context
import java.util.concurrent.CompletionStage
import java.util.concurrent.Executor

internal object NoOpEngine {
  @JvmSynthetic
  fun <TResult> implementation(context: Context, action: (Context) -> TResult?): TResult? = action(context)

  @JvmSynthetic
  fun <TResult> implementationAsync(
    context: Context,
    action: (Context) -> CompletionStage<TResult>
  ): CompletionStage<TResult> = action(context)

  @JvmSynthetic
  fun <TResult> implementationAsync(
    context: Context,
    executor: Executor,
    action: (Context, Executor) -> CompletionStage<TResult>
  ): CompletionStage<TResult> = action(context, executor)
}
