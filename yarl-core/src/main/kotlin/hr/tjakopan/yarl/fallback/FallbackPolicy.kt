package hr.tjakopan.yarl.fallback

import hr.tjakopan.yarl.*

/**
 * A fallback policy that can be applied to delegates.
 */
class FallbackPolicy internal constructor(
  policyBuilder: PolicyBuilder,
  private val onFallback: (Throwable, Context) -> Unit,
  private val fallbackAction: (Throwable, Context) -> Unit
) : Policy(policyBuilder), IFallbackPolicy {
  override fun implementation(context: Context, action: (Context) -> Unit) =
    FallbackEngine.implementation(
      context,
      exceptionPredicates,
      ResultPredicates.none(),
      { outcome, ctx -> onFallback((outcome as DelegateResult.Exception).exception, ctx) },
      { outcome, ctx -> fallbackAction((outcome as DelegateResult.Exception).exception, ctx) },
      action
    )!!

  override fun <TResult> implementation(context: Context, action: (Context) -> TResult?): TResult? =
    throw UnsupportedOperationException(
      "You have executed the generic .execute method on a non-generic fallback policy. A non-generic fallback policy " +
        "only defines a fallback action which returns void; it can never return a substitute value. To use fallback " +
        "policy to provide fallback values you must define a generic fallback policy. "
    )
}

/**
 * A fallback policy that can be applied to delegates returning a value of type [TResult].
 */
class FallbackPolicyGeneric<TResult> internal constructor(
  policyBuilder: PolicyBuilderGeneric<TResult>,
  private val onFallback: (DelegateResult<TResult>, Context) -> Unit,
  private val fallbackAction: (DelegateResult<TResult>, Context) -> TResult?
) : PolicyGeneric<TResult>(policyBuilder), IFallbackPolicyGeneric<TResult> {
  override fun implementation(context: Context, action: (Context) -> TResult?): TResult? =
    FallbackEngine.implementation(
      context,
      exceptionPredicates,
      resultPredicates,
      onFallback,
      fallbackAction,
      action
    )
}
