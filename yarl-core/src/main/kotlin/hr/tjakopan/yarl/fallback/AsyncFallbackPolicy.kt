package hr.tjakopan.yarl.fallback

import hr.tjakopan.yarl.*
import java.util.concurrent.CompletionStage
import java.util.concurrent.Executor

/**
 * A fallback policy that can be applied to asynchronous delegates.
 */
class AsyncFallbackPolicy internal constructor(
  policyBuilder: PolicyBuilder,
  private val onFallbackAsync: (Throwable, Context, Executor?) -> CompletionStage<Unit>,
  private val fallbackAction: (Throwable, Context, Executor?) -> CompletionStage<Unit>
) : AsyncPolicy(policyBuilder), IFallbackPolicy {
  internal constructor(
    policyBuilder: PolicyBuilder,
    onFallbackAsync: (Throwable, Context) -> CompletionStage<Unit>,
    fallbackAction: (Throwable, Context) -> CompletionStage<Unit>
  ) : this(policyBuilder, { ex, ctx, _ -> onFallbackAsync(ex, ctx) }, { ex, ctx, _ -> fallbackAction(ex, ctx) })

  override fun implementationAsync(
    context: Context,
    action: (Context) -> CompletionStage<Unit>
  ): CompletionStage<Unit> =
    AsyncFallbackEngine.implementationAsync(
      context,
      exceptionPredicates,
      ResultPredicates.none(),
      { outcome, ctx -> onFallbackAsync((outcome as DelegateResult.Exception).exception, ctx, null) },
      { outcome, ctx -> fallbackAction((outcome as DelegateResult.Exception).exception, ctx, null) },
      action
    )

  override fun implementationAsync(
    context: Context,
    executor: Executor,
    action: (Context, Executor) -> CompletionStage<Unit>
  ): CompletionStage<Unit> =
    AsyncFallbackEngine.implementationAsync(
      context,
      executor,
      exceptionPredicates,
      ResultPredicates.none(),
      { outcome, ctx, exe -> onFallbackAsync((outcome as DelegateResult.Exception).exception, ctx, exe) },
      { outcome, ctx, exe -> fallbackAction((outcome as DelegateResult.Exception).exception, ctx, exe) },
      action
    )

  override fun <TResult> implementationAsyncGeneric(
    context: Context,
    action: (Context) -> CompletionStage<TResult>
  ): CompletionStage<TResult> =
    throw UnsupportedOperationException(
      "You have executed the generic .execute method on a non-generic fallback policy. A non-generic fallback policy " +
        "only defines a fallback action which returns void; it can never return a substitute value. To use fallback " +
        "policy to provide fallback values you must define a generic fallback policy. "
    )

  override fun <TResult> implementationAsyncGeneric(
    context: Context,
    executor: Executor,
    action: (Context, Executor) -> CompletionStage<TResult>
  ): CompletionStage<TResult> =
    throw UnsupportedOperationException(
      "You have executed the generic .execute method on a non-generic fallback policy. A non-generic fallback policy " +
        "only defines a fallback action which returns void; it can never return a substitute value. To use fallback " +
        "policy to provide fallback values you must define a generic fallback policy. "
    )

}

/**
 * A fallback policy that can be applied to asynchronous delegates.
 *
 * @param TResult The return type of delegates which may be executed through the policy.
 */
class AsyncFallbackPolicyGeneric<TResult> internal constructor(
  policyBuilder: PolicyBuilderGeneric<TResult>,
  private val onFallbackAsync: (DelegateResult<TResult>, Context, Executor?) -> CompletionStage<Unit>,
  private val fallbackAction: (DelegateResult<TResult>, Context, Executor?) -> CompletionStage<TResult>
) : AsyncPolicyGeneric<TResult>(policyBuilder), IFallbackPolicyGeneric<TResult> {
  internal constructor(
    policyBuilder: PolicyBuilderGeneric<TResult>,
    onFallbackAsync: (DelegateResult<TResult>, Context) -> CompletionStage<Unit>,
    fallbackAction: (DelegateResult<TResult>, Context) -> CompletionStage<TResult>
  ) : this(policyBuilder, { dr, ctx, _ -> onFallbackAsync(dr, ctx) }, { dr, ctx, _ -> fallbackAction(dr, ctx) })

  override fun implementationAsync(
    context: Context,
    action: (Context) -> CompletionStage<TResult>
  ): CompletionStage<TResult> =
    AsyncFallbackEngine.implementationAsync(
      context,
      exceptionPredicates,
      resultPredicates,
      { dr, ctx -> onFallbackAsync(dr, ctx, null) },
      { dr, ctx -> fallbackAction(dr, ctx, null) },
      action
    )

  override fun implementationAsync(
    context: Context,
    executor: Executor,
    action: (Context, Executor) -> CompletionStage<TResult>
  ): CompletionStage<TResult> =
    AsyncFallbackEngine.implementationAsync(
      context,
      executor,
      exceptionPredicates,
      resultPredicates,
      { dr, ctx, exe -> onFallbackAsync(dr, ctx, exe) },
      { dr, ctx, exe -> fallbackAction(dr, ctx, exe) },
      action
    )
}
