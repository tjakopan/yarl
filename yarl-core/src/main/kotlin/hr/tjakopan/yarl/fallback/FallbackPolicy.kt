package hr.tjakopan.yarl.fallback

import hr.tjakopan.yarl.Context
import hr.tjakopan.yarl.DelegateResult
import hr.tjakopan.yarl.Policy

/**
 * A fallback policy that can be applied to delegates returning a value of type [TResult].
 */
class FallbackPolicy<TResult> private constructor(policyBuilder: Builder<TResult>) :
  Policy<TResult, FallbackPolicy.Builder<TResult>>(policyBuilder), IFallbackPolicy {
  private val onFallback = policyBuilder.onFallback
  private val fallbackAction = policyBuilder.fallbackAction

  override fun implementation(context: Context, action: (Context) -> TResult?): TResult? =
    FallbackEngine.implementation(
      context,
      exceptionPredicates,
      resultPredicates,
      onFallback,
      fallbackAction,
      action
    )

  class Builder<TResult>(@JvmSynthetic internal val fallbackAction: (DelegateResult<TResult>, Context) -> TResult?) :
    Policy.Builder<TResult, Builder<TResult>>() {
    @JvmSynthetic
    internal var onFallback: (DelegateResult<TResult>, Context) -> Unit = { _, _ -> }

    constructor(fallbackValue: TResult?) : this({ _, _ -> fallbackValue })

    override fun `this$`(): Builder<TResult> = this

    fun build() = FallbackPolicy(this)
  }
}
