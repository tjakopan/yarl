package hr.tjakopan.yarl.fallback

import hr.tjakopan.yarl.Context
import hr.tjakopan.yarl.DelegateResult
import hr.tjakopan.yarl.ExceptionPredicates
import hr.tjakopan.yarl.ResultPredicates

internal object FallbackEngine {
  @JvmSynthetic
  internal fun <TResult> implementation(
    context: Context,
    shouldHandleExceptionPredicates: ExceptionPredicates,
    shouldHandleResultPredicates: ResultPredicates<TResult>,
    onFallback: (DelegateResult<TResult>, Context) -> Unit,
    fallbackAction: (DelegateResult<TResult>, Context) -> TResult?,
    action: (Context) -> TResult?
  ): TResult? {
    var delegateOutcome: DelegateResult<TResult>
    try {
      val result = action(context)
      if (!shouldHandleResultPredicates.anyMatch(result)) {
        return result
      }
      delegateOutcome = DelegateResult.Result(result)
    } catch (ex: Throwable) {
      val handledException = shouldHandleExceptionPredicates.firstMatchOrNull(ex) ?: throw ex
      delegateOutcome = DelegateResult.Exception(handledException)
    }
    onFallback(delegateOutcome, context)
    return fallbackAction(delegateOutcome, context)
  }
}
