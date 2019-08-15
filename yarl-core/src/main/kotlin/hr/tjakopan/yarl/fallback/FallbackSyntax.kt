package hr.tjakopan.yarl.fallback

import hr.tjakopan.yarl.Context
import hr.tjakopan.yarl.DelegateResult
import hr.tjakopan.yarl.PolicyBuilder
import hr.tjakopan.yarl.PolicyBuilderGeneric

/**
 * Builds a [FallbackPolicy] which provides a fallback action if the main execution fails. Executes the main delegate,
 * but if this throws a handled exception, first calls [onFallback] with details of the handled exception and the
 * execution context; then calls [fallbackAction].
 *
 * @param fallbackAction The fallback action.
 * @return The policy instance.
 */
fun PolicyBuilder.fallback(fallbackAction: () -> Unit) = this.fallback({}, fallbackAction)

/**
 * Builds a [FallbackPolicy] which provides a fallback action if the main execution fails. Executes the main delegate,
 * but if this throws a handled exception, first calls [onFallback] with details of the handled exception and the
 * execution context; then calls [fallbackAction].
 *
 * @param onFallback The action to call before invoking the fallback delegate.
 * @param fallbackAction The fallback action.
 * @return The policy instance.
 */
fun PolicyBuilder.fallback(onFallback: (Throwable) -> Unit, fallbackAction: () -> Unit) =
  this.fallback({ exception, _ -> onFallback(exception) }) { _, _ -> fallbackAction() }

/**
 * Builds a [FallbackPolicy] which provides a fallback action if the main execution fails. Executes the main delegate,
 * but if this throws a handled exception, first calls [onFallback] with details of the handled exception and the
 * execution context; then calls [fallbackAction].
 *
 * @param onFallback The action to call before invoking the fallback delegate.
 * @param fallbackAction The fallback action.
 * @return The policy instance.
 */
fun PolicyBuilder.fallback(onFallback: (Throwable, Context) -> Unit, fallbackAction: (Context) -> Unit) =
  this.fallback(onFallback) { _, ctx -> fallbackAction(ctx) }

/**
 * Builds a [FallbackPolicy] which provides a fallback action if the main execution fails. Executes the main delegate,
 * but if this throws a handled exception, first calls [onFallback] with details of the handled exception and the
 * execution context; then calls [fallbackAction].
 *
 * @param onFallback The action to call before invoking the fallback delegate.
 * @param fallbackAction The fallback action.
 * @return The policy instance.
 */
fun PolicyBuilder.fallback(onFallback: (Throwable, Context) -> Unit, fallbackAction: (Throwable, Context) -> Unit) =
  FallbackPolicy(this, onFallback, fallbackAction)

/**
 * Builds a [FallbackPolicyGeneric] which provides a fallback value if the main execution fails. Executes the main
 * delegate, but if this throws a handled exception or raises a handled result, returns [fallbackValue] instead.
 *
 * @param fallbackValue The fallback [TResult] value to provide.
 * @return The policy instance.
 */
fun <TResult> PolicyBuilderGeneric<TResult>.fallback(fallbackValue: TResult?) =
  this.fallback({ _ -> }, fallbackAction = { fallbackValue })

/**
 * Builds a [FallbackPolicyGeneric] which provides a fallback value if the main execution fails. Executes the main
 * delegate, but if this throws a handled exception or raises a handled result, calls [fallbackAction] and returns its
 * result.
 *
 * @param fallbackAction The fallback action.
 * @return The policy instance.
 */
fun <TResult> PolicyBuilderGeneric<TResult>.fallback(fallbackAction: () -> TResult?) =
  this.fallback({}, fallbackAction)

/**
 * Builds a [FallbackPolicyGeneric] which provides a fallback value if the main execution fails. Executes the main
 * delegate, but if this throws a handled exception or raises a handled result, first calls [onFallback] with details
 * of the handled exception or result and the execution context; then returns [fallbackValue].
 *
 * @param onFallback The action to call before invoking the fallback delegate.
 * @param fallbackValue The fallback [TResult] value to provide.
 * @return The policy instance.
 */
fun <TResult> PolicyBuilderGeneric<TResult>.fallback(
  onFallback: (DelegateResult<TResult>) -> Unit,
  fallbackValue: TResult?
) = this.fallback({ outcome, _ -> onFallback(outcome) }, fallbackAction = { _, _ -> fallbackValue })

/**
 * Builds a [FallbackPolicyGeneric] which provides a fallback value if the main execution fails. Executes the main
 * delegate, but if this throws a handled exception or raises a handled result, first calls [onFallback] with details
 * of the handled exception or result and the execution context; then calls [fallbackAction] and returns its result.
 *
 * @param onFallback The action to call before invoking the fallback delegate.
 * @param fallbackAction The fallback action.
 * @return The policy instance.
 */
fun <TResult> PolicyBuilderGeneric<TResult>.fallback(
  onFallback: (DelegateResult<TResult>) -> Unit,
  fallbackAction: () -> TResult?
) = this.fallback({ outcome, _ -> onFallback(outcome) }, fallbackAction = { _, _ -> fallbackAction() })

/**
 * Builds a [FallbackPolicyGeneric] which provides a fallback value if the main execution fails. Executes the main
 * delegate, but if this throws a handled exception or raises a handled result, first calls [onFallback] with details
 * of the handled exception or result and the execution context; then returns [fallbackValue].
 *
 * @param onFallback The action to call before invoking the fallback delegate.
 * @param fallbackValue The fallback [TResult] value to provide.
 * @return The policy instance.
 */
fun <TResult> PolicyBuilderGeneric<TResult>.fallback(
  onFallback: (DelegateResult<TResult>, Context) -> Unit,
  fallbackValue: TResult?
) = this.fallback(onFallback, fallbackAction = { _, _ -> fallbackValue })

/**
 * Builds a [FallbackPolicyGeneric] which provides a fallback value if the main execution fails. Executes the main
 * delegate, but if this throws a handled exception or raises a handled result, first calls [onFallback] with details
 * of the handled exception or result and the execution context; then calls [fallbackAction] and returns its result.
 *
 * @param onFallback The action to call before invoking the fallback delegate.
 * @param fallbackAction The fallback action.
 * @return The policy instance.
 */
fun <TResult> PolicyBuilderGeneric<TResult>.fallback(
  onFallback: (DelegateResult<TResult>, Context) -> Unit,
  fallbackAction: (Context) -> TResult?
) = this.fallback(onFallback, fallbackAction = { _, ctx -> fallbackAction(ctx) })

/**
 * Builds a [FallbackPolicyGeneric] which provides a fallback value if the main execution fails. Executes the main
 * delegate, but if this throws a handled exception or raises a handled result, first calls [onFallback] with details
 * of the handled exception or result and the execution context; then calls [fallbackAction] and returns its result.
 *
 * @param onFallback The action to call before invoking the fallback delegate.
 * @param fallbackAction The fallback action.
 * @return The policy instance.
 */
fun <TResult> PolicyBuilderGeneric<TResult>.fallback(
  onFallback: (DelegateResult<TResult>, Context) -> Unit,
  fallbackAction: (DelegateResult<TResult>, Context) -> TResult?
) =
  FallbackPolicyGeneric(this, onFallback, fallbackAction)
