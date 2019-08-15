package hr.tjakopan.yarl.fallback

import hr.tjakopan.yarl.Context
import hr.tjakopan.yarl.DelegateResult
import hr.tjakopan.yarl.ExceptionPredicates
import hr.tjakopan.yarl.ResultPredicates
import java.util.concurrent.CompletableFuture
import java.util.concurrent.CompletionStage
import java.util.concurrent.Executor
import java.util.function.BiConsumer
import java.util.function.Function

internal object AsyncFallbackEngine {
  @JvmSynthetic
  internal fun <TResult> implementationAsync(
    context: Context,
    shouldHandleExceptionPredicates: ExceptionPredicates,
    shouldHandleResultPredicates: ResultPredicates<TResult>,
    onFallbackAsync: (DelegateResult<TResult>, Context) -> CompletionStage<Unit>,
    fallbackAction: (DelegateResult<TResult>, Context) -> CompletionStage<TResult>,
    action: (Context) -> CompletionStage<TResult>
  ): CompletionStage<TResult> {
    var completionStage: CompletionStage<TResult>? = null
    var delegateOutcome: DelegateResult<TResult>? = null
    action(context)
      .whenCompleteAsync { result, ex ->
        if (ex != null) {
          val handledException = shouldHandleExceptionPredicates.firstMatchOrNull(ex)
          if (handledException == null) {
            completionStage = CompletableFuture.failedStage(ex)
          }
          delegateOutcome = DelegateResult.Exception(handledException!!)
        } else {
          if (!shouldHandleResultPredicates.anyMatch(result)) {
            completionStage = CompletableFuture.completedStage(result)
          }
          delegateOutcome = DelegateResult.Result(result)
        }
      }

    return if (completionStage != null) {
      completionStage!!
    } else {
      onFallbackAsync(delegateOutcome!!, context)
        .thenComposeAsync { fallbackAction(delegateOutcome!!, context) }
    }
  }

  @JvmSynthetic
  internal fun <TResult> implementationAsync(
    context: Context,
    executor: Executor,
    shouldHandleExceptionPredicates: ExceptionPredicates,
    shouldHandleResultPredicates: ResultPredicates<TResult>,
    onFallbackAsync: (DelegateResult<TResult>, Context, Executor) -> CompletionStage<Unit>,
    fallbackAction: (DelegateResult<TResult>, Context, Executor) -> CompletionStage<TResult>,
    action: (Context, Executor) -> CompletionStage<TResult>
  ): CompletionStage<TResult> {
    var completionStage: CompletionStage<TResult>? = null
    var delegateOutcome: DelegateResult<TResult>? = null
    action(context, executor)
      .whenCompleteAsync(BiConsumer { result, ex ->
        if (ex != null) {
          val handledException = shouldHandleExceptionPredicates.firstMatchOrNull(ex)
          if (handledException == null) {
            completionStage = CompletableFuture.failedStage(ex)
          }
          delegateOutcome = DelegateResult.Exception(handledException!!)
        } else {
          if (!shouldHandleResultPredicates.anyMatch(result)) {
            completionStage = CompletableFuture.completedStage(result)
          }
          delegateOutcome = DelegateResult.Result(result)
        }
      }, executor)

    return if (completionStage != null) {
      completionStage!!
    } else {
      onFallbackAsync(delegateOutcome!!, context, executor)
        .thenComposeAsync(Function { fallbackAction(delegateOutcome!!, context, executor) }, executor)
    }
  }
}
