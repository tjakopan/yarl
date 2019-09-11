package hr.tjakopan.yarl.wrap

import hr.tjakopan.yarl.Context
import hr.tjakopan.yarl.IAsyncPolicy
import java.util.concurrent.CompletionStage
import java.util.concurrent.Executor

internal object AsyncPolicyWrapEngine {
  @JvmSynthetic
  internal fun <TResult> implementationAsync(
    context: Context,
    outerPolicy: IAsyncPolicy<TResult>,
    innerPolicy: IAsyncPolicy<TResult>,
    func: (Context) -> CompletionStage<TResult>
  ): CompletionStage<TResult> = outerPolicy.executeAsync(context) { innerPolicy.executeAsync(it, func) }

  @JvmSynthetic
  internal fun <TResult> implementationAsync(
    context: Context,
    executor: Executor,
    outerPolicy: IAsyncPolicy<TResult>,
    innerPolicy: IAsyncPolicy<TResult>,
    func: (Context, Executor) -> CompletionStage<TResult>
  ): CompletionStage<TResult> =
    outerPolicy.executeAsync(context, executor) { ctx, exe -> innerPolicy.executeAsync(ctx, exe, func) }
}
