package hr.tjakopan.yarl.fallback

import hr.tjakopan.yarl.Context
import hr.tjakopan.yarl.DelegateResult
import hr.tjakopan.yarl.PolicyBuilder
import hr.tjakopan.yarl.PolicyBuilderGeneric
import java.util.concurrent.CompletableFuture
import java.util.concurrent.CompletionStage
import java.util.concurrent.Executor

/**
 * Builds an [AsyncFallbackPolicy] which provides a fallback action if the main execution fails. Executes the main
 * delegate asynchronously, but if this throws a handled exception, asynchronously calls [fallbackAction].
 *
 * @param fallbackAction The fallback delegate.
 * @return The policy instance.
 */
fun PolicyBuilder.fallbackAsync(fallbackAction: () -> CompletionStage<Unit>) =
  this.fallbackAsync({ _ -> CompletableFuture.completedStage(null) }, fallbackAction)

/**
 * Builds an [AsyncFallbackPolicy] which provides a fallback action if the main execution fails. Executes the main
 * delegate asynchronously, but if this throws a handled exception, asynchronously calls [fallbackAction].
 *
 * @param fallbackAction The fallback delegate.
 * @return The policy instance.
 */
fun PolicyBuilder.fallbackAsyncEx(fallbackAction: (Executor) -> CompletionStage<Unit>) =
  this.fallbackAsyncEx({ _, _ -> CompletableFuture.completedStage(null) }, fallbackAction)

/**
 * Builds an [AsyncFallbackPolicy] which provides a fallback action if the main execution fails. Executes the main
 * delegate asynchronously, but if this throws a handled exception, first asynchronously calls [onFallbackAsync] with
 * details of the handled exception and execution context; then asynchronously calls [fallbackAction].
 *
 * @param onFallbackAsync The action to call asynchronously before invoking the fallback delegate.
 * @param fallbackAction The fallback delegate.
 * @return The policy instance.
 */
fun PolicyBuilder.fallbackAsync(
  onFallbackAsync: (Throwable) -> CompletionStage<Unit>,
  fallbackAction: () -> CompletionStage<Unit>
) =
  this.fallbackAsync({ ex, _ -> onFallbackAsync(ex) }, { _, _ -> fallbackAction() })

/**
 * Builds an [AsyncFallbackPolicy] which provides a fallback action if the main execution fails. Executes the main
 * delegate asynchronously, but if this throws a handled exception, first asynchronously calls [onFallbackAsync] with
 * details of the handled exception and execution context; then asynchronously calls [fallbackAction].
 *
 * @param onFallbackAsync The action to call asynchronously before invoking the fallback delegate.
 * @param fallbackAction The fallback delegate.
 * @return The policy instance.
 */
fun PolicyBuilder.fallbackAsyncEx(
  onFallbackAsync: (Throwable, Executor) -> CompletionStage<Unit>,
  fallbackAction: (Executor) -> CompletionStage<Unit>
) =
  this.fallbackAsyncEx({ ex, _, exe -> onFallbackAsync(ex, exe) }, { _, _, exe -> fallbackAction(exe) })

/**
 * Builds an [AsyncFallbackPolicy] which provides a fallback action if the main execution fails. Executes the main
 * delegate asynchronously, but if this throws a handled exception, first asynchronously calls [onFallbackAsync] with
 * details of the handled exception and execution context; then asynchronously calls [fallbackAction].
 *
 * @param onFallbackAsync The action to call asynchronously before invoking the fallback delegate.
 * @param fallbackAction The fallback delegate.
 * @return The policy instance.
 */
fun PolicyBuilder.fallbackAsync(
  onFallbackAsync: (Throwable, Context) -> CompletionStage<Unit>,
  fallbackAction: (Context) -> CompletionStage<Unit>
) =
  this.fallbackAsync(onFallbackAsync, { _, ctx -> fallbackAction(ctx) })

/**
 * Builds an [AsyncFallbackPolicy] which provides a fallback action if the main execution fails. Executes the main
 * delegate asynchronously, but if this throws a handled exception, first asynchronously calls [onFallbackAsync] with
 * details of the handled exception and execution context; then asynchronously calls [fallbackAction].
 *
 * @param onFallbackAsync The action to call asynchronously before invoking the fallback delegate.
 * @param fallbackAction The fallback delegate.
 * @return The policy instance.
 */
fun PolicyBuilder.fallbackAsyncEx(
  onFallbackAsync: (Throwable, Context, Executor) -> CompletionStage<Unit>,
  fallbackAction: (Context, Executor) -> CompletionStage<Unit>
) =
  this.fallbackAsyncEx(onFallbackAsync, { _, ctx, exe -> fallbackAction(ctx, exe) })

/**
 * Builds an [AsyncFallbackPolicy] which provides a fallback action if the main execution fails. Executes the main
 * delegate asynchronously, but if this throws a handled exception, first asynchronously calls [onFallbackAsync] with
 * details of the handled exception and execution context; then asynchronously calls [fallbackAction].
 *
 * @param onFallbackAsync The action to call asynchronously before invoking the fallback delegate.
 * @param fallbackAction The fallback delegate.
 * @return The policy instance.
 */
fun PolicyBuilder.fallbackAsync(
  onFallbackAsync: (Throwable, Context) -> CompletionStage<Unit>,
  fallbackAction: (Throwable, Context) -> CompletionStage<Unit>
) =
  AsyncFallbackPolicy(this, onFallbackAsync, fallbackAction)

/**
 * Builds an [AsyncFallbackPolicy] which provides a fallback action if the main execution fails. Executes the main
 * delegate asynchronously, but if this throws a handled exception, first asynchronously calls [onFallbackAsync] with
 * details of the handled exception and execution context; then asynchronously calls [fallbackAction].
 *
 * @param onFallbackAsync The action to call asynchronously before invoking the fallback delegate.
 * @param fallbackAction The fallback delegate.
 * @return The policy instance.
 */
fun PolicyBuilder.fallbackAsyncEx(
  onFallbackAsync: (Throwable, Context, Executor) -> CompletionStage<Unit>,
  fallbackAction: (Throwable, Context, Executor) -> CompletionStage<Unit>
) =
  AsyncFallbackPolicy(
    this,
    { ex, ctx, exe -> onFallbackAsync(ex, ctx, exe!!) },
    { ex, ctx, exe -> fallbackAction(ex, ctx, exe!!) })

/**
 * Builds an [AsyncFallbackPolicyGeneric] which provides a fallback value if the main execution fails. Executes the
 * main delegate asynchronously, but if this throws a handled exception or raises a handled result, returns
 * [fallbackValue].
 *
 * @param fallbackValue The fallback [TResult] value to provide.
 * @return The policy instance.
 */
fun <TResult> PolicyBuilderGeneric<TResult>.fallbackAsync(
  fallbackValue: TResult?
) =
  this.fallbackAsync(
    { _ -> CompletableFuture.completedStage(null) },
    fallbackAction = { CompletableFuture.completedStage(fallbackValue) })

/**
 * Builds an [AsyncFallbackPolicyGeneric] which provides a fallback value if the main execution fails. Executes the
 * main delegate asynchronously, but if this throws a handled exception or raises a handled result, asynchronously
 * calls [fallbackAction] and returns its result.
 *
 * @param fallbackAction The fallback delegate.
 * @return The policy instance.
 */
fun <TResult> PolicyBuilderGeneric<TResult>.fallbackAsync(fallbackAction: () -> CompletionStage<TResult>) =
  this.fallbackAsync({ CompletableFuture.completedStage(null) }, fallbackAction)

/**
 * Builds an [AsyncFallbackPolicyGeneric] which provides a fallback value if the main execution fails. Executes the
 * main delegate asynchronously, but if this throws a handled exception or raises a handled result, asynchronously
 * calls [fallbackAction] and returns its result.
 *
 * @param fallbackAction The fallback delegate.
 * @return The policy instance.
 */
fun <TResult> PolicyBuilderGeneric<TResult>.fallbackAsyncEx(fallbackAction: (Executor) -> CompletionStage<TResult>) =
  this.fallbackAsyncEx({ _, _ -> CompletableFuture.completedStage(null) }, fallbackAction)

/**
 * Builds an [AsyncFallbackPolicyGeneric] which provides a fallback value if the main execution fails. Executes the
 * main delegate asynchronously, but if this throws a handled exception or raises a handled result, first
 * asynchronously calls [onFallbackAsync] with details of the handled exception or result and the execution context;
 * then returns [fallbackValue].
 *
 * @param onFallbackAsync The action to call asynchronously before invoking the fallback delegate.
 * @param fallbackValue The fallback [TResult] value to provide.
 * @return The policy instance.
 */
fun <TResult> PolicyBuilderGeneric<TResult>.fallbackAsync(
  onFallbackAsync: (DelegateResult<TResult>) -> CompletionStage<Unit>,
  fallbackValue: TResult?
) =
  this.fallbackAsync(
    { outcome, _ -> onFallbackAsync(outcome) },
    fallbackAction = { _, _ -> CompletableFuture.completedStage(fallbackValue) })

/**
 * Builds an [AsyncFallbackPolicyGeneric] which provides a fallback value if the main execution fails. Executes the
 * main delegate asynchronously, but if this throws a handled exception or raises a handled result, first
 * asynchronously calls [onFallbackAsync] with details of the handled exception or result and the execution context;
 * then returns [fallbackValue].
 *
 * @param onFallbackAsync The action to call asynchronously before invoking the fallback delegate.
 * @param fallbackValue The fallback [TResult] value to provide.
 * @return The policy instance.
 */
fun <TResult> PolicyBuilderGeneric<TResult>.fallbackAsyncEx(
  onFallbackAsync: (DelegateResult<TResult>, Executor) -> CompletionStage<Unit>,
  fallbackValue: TResult?
) =
  this.fallbackAsyncEx(
    { outcome, _, exe -> onFallbackAsync(outcome, exe) },
    fallbackAction = { _, _, _ -> CompletableFuture.completedStage(fallbackValue) })

/**
 * Builds an [AsyncFallbackPolicyGeneric] which provides a fallback value if the main execution fails. Executes the
 * main delegate asynchronously, but if this throws a handled exception or raises a handled result, first
 * asynchronously calls [onFallbackAsync] with details of the handled exception or result and the execution context;
 * then asynchronously calls [fallbackAction] and returns its result.
 *
 * @param onFallbackAsync The action to call asynchronously before invoking the fallback delegate.
 * @param fallbackAction The fallback delegate.
 * @return The policy instance.
 */
fun <TResult> PolicyBuilderGeneric<TResult>.fallbackAsync(
  onFallbackAsync: (DelegateResult<TResult>) -> CompletionStage<Unit>,
  fallbackAction: () -> CompletionStage<TResult>
) =
  this.fallbackAsync({ outcome, _ -> onFallbackAsync(outcome) }, fallbackAction = { _, _ -> fallbackAction() })

/**
 * Builds an [AsyncFallbackPolicyGeneric] which provides a fallback value if the main execution fails. Executes the
 * main delegate asynchronously, but if this throws a handled exception or raises a handled result, first
 * asynchronously calls [onFallbackAsync] with details of the handled exception or result and the execution context;
 * then asynchronously calls [fallbackAction] and returns its result.
 *
 * @param onFallbackAsync The action to call asynchronously before invoking the fallback delegate.
 * @param fallbackAction The fallback delegate.
 * @return The policy instance.
 */
fun <TResult> PolicyBuilderGeneric<TResult>.fallbackAsyncEx(
  onFallbackAsync: (DelegateResult<TResult>, Executor) -> CompletionStage<Unit>,
  fallbackAction: (Executor) -> CompletionStage<TResult>
) =
  this.fallbackAsyncEx(
    { outcome, _, exe -> onFallbackAsync(outcome, exe) },
    fallbackAction = { _, _, exe -> fallbackAction(exe) })

/**
 * Builds an [AsyncFallbackPolicyGeneric] which provides a fallback value if the main execution fails. Executes the
 * main delegate asynchronously, but if this throws a handled exception or raises a handled result, first
 * asynchronously calls [onFallbackAsync] with details of the handled exception or result and the execution context;
 * then returns [fallbackValue].
 *
 * @param onFallbackAsync The action to call asynchronously before invoking the fallback delegate.
 * @param fallbackValue The fallback [TResult] value to provide.
 * @return The policy instance.
 */
fun <TResult> PolicyBuilderGeneric<TResult>.fallbackAsync(
  onFallbackAsync: (DelegateResult<TResult>, Context) -> CompletionStage<Unit>,
  fallbackValue: TResult?
) =
  this.fallbackAsync(onFallbackAsync, fallbackAction = { _, _ -> CompletableFuture.completedStage(fallbackValue) })

/**
 * Builds an [AsyncFallbackPolicyGeneric] which provides a fallback value if the main execution fails. Executes the
 * main delegate asynchronously, but if this throws a handled exception or raises a handled result, first
 * asynchronously calls [onFallbackAsync] with details of the handled exception or result and the execution context;
 * then returns [fallbackValue].
 *
 * @param onFallbackAsync The action to call asynchronously before invoking the fallback delegate.
 * @param fallbackValue The fallback [TResult] value to provide.
 * @return The policy instance.
 */
fun <TResult> PolicyBuilderGeneric<TResult>.fallbackAsyncEx(
  onFallbackAsync: (DelegateResult<TResult>, Context, Executor) -> CompletionStage<Unit>,
  fallbackValue: TResult?
) =
  this.fallbackAsyncEx(onFallbackAsync, fallbackAction = { _, _, _ -> CompletableFuture.completedStage(fallbackValue) })

/**
 * Builds an [AsyncFallbackPolicyGeneric] which provides a fallback value if the main execution fails. Executes the
 * main delegate asynchronously, but if this throws a handled exception or raises a handled result, first
 * asynchronously calls [onFallbackAsync] with details of the handled exception or result and the execution context;
 * then asynchronously calls [fallbackAction] and returns its result.
 *
 * @param onFallbackAsync The action to call asynchronously before invoking the fallback delegate.
 * @param fallbackAction The fallback delegate.
 * @return The policy instance.
 */
fun <TResult> PolicyBuilderGeneric<TResult>.fallbackAsync(
  onFallbackAsync: (DelegateResult<TResult>, Context) -> CompletionStage<Unit>,
  fallbackAction: (Context) -> CompletionStage<TResult>
) =
  this.fallbackAsync(onFallbackAsync, fallbackAction = { _, ctx -> fallbackAction(ctx) })

/**
 * Builds an [AsyncFallbackPolicyGeneric] which provides a fallback value if the main execution fails. Executes the
 * main delegate asynchronously, but if this throws a handled exception or raises a handled result, first
 * asynchronously calls [onFallbackAsync] with details of the handled exception or result and the execution context;
 * then asynchronously calls [fallbackAction] and returns its result.
 *
 * @param onFallbackAsync The action to call asynchronously before invoking the fallback delegate.
 * @param fallbackAction The fallback delegate.
 * @return The policy instance.
 */
fun <TResult> PolicyBuilderGeneric<TResult>.fallbackAsyncEx(
  onFallbackAsync: (DelegateResult<TResult>, Context, Executor) -> CompletionStage<Unit>,
  fallbackAction: (Context, Executor) -> CompletionStage<TResult>
) =
  this.fallbackAsyncEx(onFallbackAsync, fallbackAction = { _, ctx, exe -> fallbackAction(ctx, exe) })

/**
 * Builds an [AsyncFallbackPolicyGeneric] which provides a fallback value if the main execution fails. Executes the
 * main delegate asynchronously, but if this throws a handled exception or raises a handled result, first
 * asynchronously calls [onFallbackAsync] with details of the handled exception or result and the execution context;
 * then asynchronously calls [fallbackAction] and returns its result.
 *
 * @param onFallbackAsync The action to call asynchronously before invoking the fallback delegate.
 * @param fallbackAction The fallback delegate.
 * @return The policy instance.
 */
fun <TResult> PolicyBuilderGeneric<TResult>.fallbackAsync(
  onFallbackAsync: (DelegateResult<TResult>, Context) -> CompletionStage<Unit>,
  fallbackAction: (DelegateResult<TResult>, Context) -> CompletionStage<TResult>
) =
  AsyncFallbackPolicyGeneric(this, onFallbackAsync, fallbackAction)

/**
 * Builds an [AsyncFallbackPolicyGeneric] which provides a fallback value if the main execution fails. Executes the
 * main delegate asynchronously, but if this throws a handled exception or raises a handled result, first
 * asynchronously calls [onFallbackAsync] with details of the handled exception or result and the execution context;
 * then asynchronously calls [fallbackAction] and returns its result.
 *
 * @param onFallbackAsync The action to call asynchronously before invoking the fallback delegate.
 * @param fallbackAction The fallback delegate.
 * @return The policy instance.
 */
fun <TResult> PolicyBuilderGeneric<TResult>.fallbackAsyncEx(
  onFallbackAsync: (DelegateResult<TResult>, Context, Executor) -> CompletionStage<Unit>,
  fallbackAction: (DelegateResult<TResult>, Context, Executor) -> CompletionStage<TResult>
) =
  AsyncFallbackPolicyGeneric(
    this,
    { outcome, ctx, exe -> onFallbackAsync(outcome, ctx, exe!!) },
    { outcome, ctx, exe -> fallbackAction(outcome, ctx, exe!!) })
