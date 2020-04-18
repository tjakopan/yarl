//@file:JvmName("FallbackBuilder")
//@file:JvmMultifileClass
//
//package hr.tjakopan.yarl.fallback
//
//import hr.tjakopan.yarl.Context
//import hr.tjakopan.yarl.DelegateResult
//import hr.tjakopan.yarl.PolicyBuilder
//import hr.tjakopan.yarl.PolicyBuilderGeneric
//import java.util.concurrent.CompletableFuture
//import java.util.concurrent.CompletableFuture
//import java.util.concurrent.Executor
//
///**
// * Builds an [AsyncFallbackPolicyGeneric] which provides a fallback value if the main execution fails. Executes the
// * main delegate asynchronously, but if this throws a handled exception or raises a handled result, returns
// * [fallbackValue].
// *
// * @param fallbackValue The fallback [TResult] value to provide.
// * @return The policy instance.
// */
//fun <TResult> PolicyBuilderGeneric<TResult>.fallbackAsync(
//  fallbackValue: TResult?
//) =
//  this.fallbackAsync(
//    { _ -> CompletableFuture.completedStage(null) },
//    fallbackAction = { CompletableFuture.completedStage(fallbackValue) })
//
///**
// * Builds an [AsyncFallbackPolicyGeneric] which provides a fallback value if the main execution fails. Executes the
// * main delegate asynchronously, but if this throws a handled exception or raises a handled result, asynchronously
// * calls [fallbackAction] and returns its result.
// *
// * @param fallbackAction The fallback delegate.
// * @return The policy instance.
// */
//fun <TResult> PolicyBuilderGeneric<TResult>.fallbackAsync(fallbackAction: () -> CompletableFuture<TResult>) =
//  this.fallbackAsync({ CompletableFuture.completedStage(null) }, fallbackAction)
//
///**
// * Builds an [AsyncFallbackPolicyGeneric] which provides a fallback value if the main execution fails. Executes the
// * main delegate asynchronously, but if this throws a handled exception or raises a handled result, asynchronously
// * calls [fallbackAction] and returns its result.
// *
// * @param fallbackAction The fallback delegate.
// * @return The policy instance.
// */
//fun <TResult> PolicyBuilderGeneric<TResult>.fallbackAsyncEx(fallbackAction: (Executor) -> CompletableFuture<TResult>) =
//  this.fallbackAsyncEx({ _, _ -> CompletableFuture.completedStage(null) }, fallbackAction)
//
///**
// * Builds an [AsyncFallbackPolicyGeneric] which provides a fallback value if the main execution fails. Executes the
// * main delegate asynchronously, but if this throws a handled exception or raises a handled result, first
// * asynchronously calls [onFallbackAsync] with details of the handled exception or result and the execution context;
// * then returns [fallbackValue].
// *
// * @param onFallbackAsync The action to call asynchronously before invoking the fallback delegate.
// * @param fallbackValue The fallback [TResult] value to provide.
// * @return The policy instance.
// */
//fun <TResult> PolicyBuilderGeneric<TResult>.fallbackAsync(
//  onFallbackAsync: (DelegateResult<TResult>) -> CompletableFuture<Unit>,
//  fallbackValue: TResult?
//) =
//  this.fallbackAsync(
//    { outcome, _ -> onFallbackAsync(outcome) },
//    fallbackAction = { _, _ -> CompletableFuture.completedStage(fallbackValue) })
//
///**
// * Builds an [AsyncFallbackPolicyGeneric] which provides a fallback value if the main execution fails. Executes the
// * main delegate asynchronously, but if this throws a handled exception or raises a handled result, first
// * asynchronously calls [onFallbackAsync] with details of the handled exception or result and the execution context;
// * then returns [fallbackValue].
// *
// * @param onFallbackAsync The action to call asynchronously before invoking the fallback delegate.
// * @param fallbackValue The fallback [TResult] value to provide.
// * @return The policy instance.
// */
//fun <TResult> PolicyBuilderGeneric<TResult>.fallbackAsyncEx(
//  onFallbackAsync: (DelegateResult<TResult>, Executor) -> CompletableFuture<Unit>,
//  fallbackValue: TResult?
//) =
//  this.fallbackAsyncEx(
//    { outcome, _, exe -> onFallbackAsync(outcome, exe) },
//    fallbackAction = { _, _, _ -> CompletableFuture.completedStage(fallbackValue) })
//
///**
// * Builds an [AsyncFallbackPolicyGeneric] which provides a fallback value if the main execution fails. Executes the
// * main delegate asynchronously, but if this throws a handled exception or raises a handled result, first
// * asynchronously calls [onFallbackAsync] with details of the handled exception or result and the execution context;
// * then asynchronously calls [fallbackAction] and returns its result.
// *
// * @param onFallbackAsync The action to call asynchronously before invoking the fallback delegate.
// * @param fallbackAction The fallback delegate.
// * @return The policy instance.
// */
//fun <TResult> PolicyBuilderGeneric<TResult>.fallbackAsync(
//  onFallbackAsync: (DelegateResult<TResult>) -> CompletableFuture<Unit>,
//  fallbackAction: () -> CompletableFuture<TResult>
//) =
//  this.fallbackAsync({ outcome, _ -> onFallbackAsync(outcome) }, fallbackAction = { _, _ -> fallbackAction() })
//
///**
// * Builds an [AsyncFallbackPolicyGeneric] which provides a fallback value if the main execution fails. Executes the
// * main delegate asynchronously, but if this throws a handled exception or raises a handled result, first
// * asynchronously calls [onFallbackAsync] with details of the handled exception or result and the execution context;
// * then asynchronously calls [fallbackAction] and returns its result.
// *
// * @param onFallbackAsync The action to call asynchronously before invoking the fallback delegate.
// * @param fallbackAction The fallback delegate.
// * @return The policy instance.
// */
//fun <TResult> PolicyBuilderGeneric<TResult>.fallbackAsyncEx(
//  onFallbackAsync: (DelegateResult<TResult>, Executor) -> CompletableFuture<Unit>,
//  fallbackAction: (Executor) -> CompletableFuture<TResult>
//) =
//  this.fallbackAsyncEx(
//    { outcome, _, exe -> onFallbackAsync(outcome, exe) },
//    fallbackAction = { _, _, exe -> fallbackAction(exe) })
//
///**
// * Builds an [AsyncFallbackPolicyGeneric] which provides a fallback value if the main execution fails. Executes the
// * main delegate asynchronously, but if this throws a handled exception or raises a handled result, first
// * asynchronously calls [onFallbackAsync] with details of the handled exception or result and the execution context;
// * then returns [fallbackValue].
// *
// * @param onFallbackAsync The action to call asynchronously before invoking the fallback delegate.
// * @param fallbackValue The fallback [TResult] value to provide.
// * @return The policy instance.
// */
//fun <TResult> PolicyBuilderGeneric<TResult>.fallbackAsync(
//  onFallbackAsync: (DelegateResult<TResult>, Context) -> CompletableFuture<Unit>,
//  fallbackValue: TResult?
//) =
//  this.fallbackAsync(onFallbackAsync, fallbackAction = { _, _ -> CompletableFuture.completedStage(fallbackValue) })
//
///**
// * Builds an [AsyncFallbackPolicyGeneric] which provides a fallback value if the main execution fails. Executes the
// * main delegate asynchronously, but if this throws a handled exception or raises a handled result, first
// * asynchronously calls [onFallbackAsync] with details of the handled exception or result and the execution context;
// * then returns [fallbackValue].
// *
// * @param onFallbackAsync The action to call asynchronously before invoking the fallback delegate.
// * @param fallbackValue The fallback [TResult] value to provide.
// * @return The policy instance.
// */
//fun <TResult> PolicyBuilderGeneric<TResult>.fallbackAsyncEx(
//  onFallbackAsync: (DelegateResult<TResult>, Context, Executor) -> CompletableFuture<Unit>,
//  fallbackValue: TResult?
//) =
//  this.fallbackAsyncEx(onFallbackAsync, fallbackAction = { _, _, _ -> CompletableFuture.completedStage(fallbackValue) })
//
///**
// * Builds an [AsyncFallbackPolicyGeneric] which provides a fallback value if the main execution fails. Executes the
// * main delegate asynchronously, but if this throws a handled exception or raises a handled result, first
// * asynchronously calls [onFallbackAsync] with details of the handled exception or result and the execution context;
// * then asynchronously calls [fallbackAction] and returns its result.
// *
// * @param onFallbackAsync The action to call asynchronously before invoking the fallback delegate.
// * @param fallbackAction The fallback delegate.
// * @return The policy instance.
// */
//fun <TResult> PolicyBuilderGeneric<TResult>.fallbackAsync(
//  onFallbackAsync: (DelegateResult<TResult>, Context) -> CompletableFuture<Unit>,
//  fallbackAction: (Context) -> CompletableFuture<TResult>
//) =
//  this.fallbackAsync(onFallbackAsync, fallbackAction = { _, ctx -> fallbackAction(ctx) })
//
///**
// * Builds an [AsyncFallbackPolicyGeneric] which provides a fallback value if the main execution fails. Executes the
// * main delegate asynchronously, but if this throws a handled exception or raises a handled result, first
// * asynchronously calls [onFallbackAsync] with details of the handled exception or result and the execution context;
// * then asynchronously calls [fallbackAction] and returns its result.
// *
// * @param onFallbackAsync The action to call asynchronously before invoking the fallback delegate.
// * @param fallbackAction The fallback delegate.
// * @return The policy instance.
// */
//fun <TResult> PolicyBuilderGeneric<TResult>.fallbackAsyncEx(
//  onFallbackAsync: (DelegateResult<TResult>, Context, Executor) -> CompletableFuture<Unit>,
//  fallbackAction: (Context, Executor) -> CompletableFuture<TResult>
//) =
//  this.fallbackAsyncEx(onFallbackAsync, fallbackAction = { _, ctx, exe -> fallbackAction(ctx, exe) })
//
///**
// * Builds an [AsyncFallbackPolicyGeneric] which provides a fallback value if the main execution fails. Executes the
// * main delegate asynchronously, but if this throws a handled exception or raises a handled result, first
// * asynchronously calls [onFallbackAsync] with details of the handled exception or result and the execution context;
// * then asynchronously calls [fallbackAction] and returns its result.
// *
// * @param onFallbackAsync The action to call asynchronously before invoking the fallback delegate.
// * @param fallbackAction The fallback delegate.
// * @return The policy instance.
// */
//fun <TResult> PolicyBuilderGeneric<TResult>.fallbackAsync(
//  onFallbackAsync: (DelegateResult<TResult>, Context) -> CompletableFuture<Unit>,
//  fallbackAction: (DelegateResult<TResult>, Context) -> CompletableFuture<TResult>
//) =
//  AsyncFallbackPolicyGeneric(this, onFallbackAsync, fallbackAction)
//
///**
// * Builds an [AsyncFallbackPolicyGeneric] which provides a fallback value if the main execution fails. Executes the
// * main delegate asynchronously, but if this throws a handled exception or raises a handled result, first
// * asynchronously calls [onFallbackAsync] with details of the handled exception or result and the execution context;
// * then asynchronously calls [fallbackAction] and returns its result.
// *
// * @param onFallbackAsync The action to call asynchronously before invoking the fallback delegate.
// * @param fallbackAction The fallback delegate.
// * @return The policy instance.
// */
//fun <TResult> PolicyBuilderGeneric<TResult>.fallbackAsyncEx(
//  onFallbackAsync: (DelegateResult<TResult>, Context, Executor) -> CompletableFuture<Unit>,
//  fallbackAction: (DelegateResult<TResult>, Context, Executor) -> CompletableFuture<TResult>
//) =
//  AsyncFallbackPolicyGeneric(
//    this,
//    { outcome, ctx, exe -> onFallbackAsync(outcome, ctx, exe!!) },
//    { outcome, ctx, exe -> fallbackAction(outcome, ctx, exe!!) })
