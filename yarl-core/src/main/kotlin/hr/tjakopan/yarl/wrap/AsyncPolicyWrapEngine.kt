package hr.tjakopan.yarl.wrap

import hr.tjakopan.yarl.Context
import hr.tjakopan.yarl.IAsyncPolicy
import hr.tjakopan.yarl.IAsyncPolicyGeneric
import java.util.concurrent.CompletionStage
import java.util.concurrent.Executor

internal object AsyncPolicyWrapEngine {
  internal fun <TResult> implementationAsyncGeneric(
    context: Context,
    outerPolicy: IAsyncPolicyGeneric<TResult>,
    innerPolicy: IAsyncPolicyGeneric<TResult>,
    func: (Context) -> CompletionStage<TResult>
  ): CompletionStage<TResult> = outerPolicy.executeAsync(context) { innerPolicy.executeAsync(it, func) }

  internal fun <TResult> implementationAsyncGeneric(
    context: Context,
    executor: Executor,
    outerPolicy: IAsyncPolicyGeneric<TResult>,
    innerPolicy: IAsyncPolicyGeneric<TResult>,
    func: (Context, Executor) -> CompletionStage<TResult>
  ): CompletionStage<TResult> =
    outerPolicy.executeAsync(context, executor) { ctx, exe -> innerPolicy.executeAsync(ctx, exe, func) }

  internal fun <TResult> implementationAsyncGeneric(
    context: Context,
    outerPolicy: IAsyncPolicyGeneric<TResult>,
    innerPolicy: IAsyncPolicy,
    func: (Context) -> CompletionStage<TResult>
  ): CompletionStage<TResult> = outerPolicy.executeAsync(context) { innerPolicy.executeAsyncGeneric(it, func) }

  internal fun <TResult> implementationAsyncGeneric(
    context: Context,
    executor: Executor,
    outerPolicy: IAsyncPolicyGeneric<TResult>,
    innerPolicy: IAsyncPolicy,
    func: (Context, Executor) -> CompletionStage<TResult>
  ): CompletionStage<TResult> =
    outerPolicy.executeAsync(context, executor) { ctx, exe -> innerPolicy.executeAsyncGeneric(ctx, exe, func) }

  internal fun <TResult> implementationAsyncGeneric(
    context: Context,
    outerPolicy: IAsyncPolicy,
    innerPolicy: IAsyncPolicyGeneric<TResult>,
    func: (Context) -> CompletionStage<TResult>
  ): CompletionStage<TResult> = outerPolicy.executeAsyncGeneric(context) { innerPolicy.executeAsync(it, func) }

  internal fun <TResult> implementationAsyncGeneric(
    context: Context,
    executor: Executor,
    outerPolicy: IAsyncPolicy,
    innerPolicy: IAsyncPolicyGeneric<TResult>,
    func: (Context, Executor) -> CompletionStage<TResult>
  ): CompletionStage<TResult> =
    outerPolicy.executeAsyncGeneric(context, executor) { ctx, exe -> innerPolicy.executeAsync(ctx, exe, func) }

  internal fun <TResult> implementationAsyncGeneric(
    context: Context,
    outerPolicy: IAsyncPolicy,
    innerPolicy: IAsyncPolicy,
    func: (Context) -> CompletionStage<TResult>
  ): CompletionStage<TResult> =
    outerPolicy.executeAsyncGeneric(context) { innerPolicy.executeAsyncGeneric(it, func) }

  internal fun <TResult> implementationAsyncGeneric(
    context: Context,
    executor: Executor,
    outerPolicy: IAsyncPolicy,
    innerPolicy: IAsyncPolicy,
    func: (Context, Executor) -> CompletionStage<TResult>
  ): CompletionStage<TResult> =
    outerPolicy.executeAsyncGeneric(context, executor) { ctx, exe -> innerPolicy.executeAsyncGeneric(ctx, exe, func) }

  internal fun implementationAsync(
    context: Context,
    outerPolicy: IAsyncPolicy,
    innerPolicy: IAsyncPolicy,
    action: (Context) -> CompletionStage<Unit>
  ): CompletionStage<Unit> = outerPolicy.executeAsync(context) { innerPolicy.executeAsync(it, action) }

  internal fun implementationAsync(
    context: Context,
    executor: Executor,
    outerPolicy: IAsyncPolicy,
    innerPolicy: IAsyncPolicy,
    action: (Context, Executor) -> CompletionStage<Unit>
  ): CompletionStage<Unit> =
    outerPolicy.executeAsync(context, executor) { ctx, exe -> innerPolicy.executeAsync(ctx, exe, action) }
}
