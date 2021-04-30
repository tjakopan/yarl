//package hr.tjakopan.yarl.fallback
//
//import hr.tjakopan.yarl.Context
//import hr.tjakopan.yarl.DelegateResult
//import hr.tjakopan.yarl.ExceptionPredicates
//import hr.tjakopan.yarl.ResultPredicates
//import java.util.concurrent.CompletableFuture
//import java.util.concurrent.CompletableFuture
//import java.util.concurrent.Executor
//import java.util.function.BiConsumer
//import java.util.function.Function
//
//internal object AsyncFallbackEngine {
//  @JvmSynthetic
//  internal fun <TResult> implementationAsync(
//    context: Context,
//    shouldHandleExceptionPredicates: ExceptionPredicates,
//    shouldHandleResultPredicates: ResultPredicates<TResult>,
//    onFallbackAsync: (DelegateResult<TResult>, Context) -> CompletableFuture<Unit>,
//    fallbackAction: (DelegateResult<TResult>, Context) -> CompletableFuture<TResult>,
//    action: (Context) -> CompletableFuture<TResult>
//  ): CompletableFuture<TResult> {
//    var CompletableFuture: CompletableFuture<TResult>? = null
//    var delegateOutcome: DelegateResult<TResult>? = null
//    action(context)
//      .whenCompleteAsync { result, ex ->
//        if (ex != null) {
//          val handledException = shouldHandleExceptionPredicates.firstMatchOrNull(ex)
//          if (handledException == null) {
//            CompletableFuture = CompletableFuture.failedStage(ex)
//          }
//          delegateOutcome = DelegateResult.Exception(handledException!!)
//        } else {
//          if (!shouldHandleResultPredicates.anyMatch(result)) {
//            CompletableFuture = CompletableFuture.completedStage(result)
//          }
//          delegateOutcome = DelegateResult.Result(result)
//        }
//      }
//
//    return if (CompletableFuture != null) {
//      CompletableFuture!!
//    } else {
//      onFallbackAsync(delegateOutcome!!, context)
//        .thenComposeAsync { fallbackAction(delegateOutcome!!, context) }
//    }
//  }
//
//  @JvmSynthetic
//  internal fun <TResult> implementationAsync(
//    context: Context,
//    executor: Executor,
//    shouldHandleExceptionPredicates: ExceptionPredicates,
//    shouldHandleResultPredicates: ResultPredicates<TResult>,
//    onFallbackAsync: (DelegateResult<TResult>, Context, Executor) -> CompletableFuture<Unit>,
//    fallbackAction: (DelegateResult<TResult>, Context, Executor) -> CompletableFuture<TResult>,
//    action: (Context, Executor) -> CompletableFuture<TResult>
//  ): CompletableFuture<TResult> {
//    var CompletableFuture: CompletableFuture<TResult>? = null
//    var delegateOutcome: DelegateResult<TResult>? = null
//    action(context, executor)
//      .whenCompleteAsync(BiConsumer { result, ex ->
//        if (ex != null) {
//          val handledException = shouldHandleExceptionPredicates.firstMatchOrNull(ex)
//          if (handledException == null) {
//            CompletableFuture = CompletableFuture.failedStage(ex)
//          }
//          delegateOutcome = DelegateResult.Exception(handledException!!)
//        } else {
//          if (!shouldHandleResultPredicates.anyMatch(result)) {
//            CompletableFuture = CompletableFuture.completedStage(result)
//          }
//          delegateOutcome = DelegateResult.Result(result)
//        }
//      }, executor)
//
//    return if (CompletableFuture != null) {
//      CompletableFuture!!
//    } else {
//      onFallbackAsync(delegateOutcome!!, context, executor)
//        .thenComposeAsync(Function { fallbackAction(delegateOutcome!!, context, executor) }, executor)
//    }
//  }
//}
