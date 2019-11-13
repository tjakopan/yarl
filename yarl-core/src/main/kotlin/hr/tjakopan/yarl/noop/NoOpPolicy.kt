package hr.tjakopan.yarl.noop

import hr.tjakopan.yarl.Context
import hr.tjakopan.yarl.Policy

/**
 * A no op policy that can be applied to delegates returning a value of type [TResult].
 *
 * @param TResult The type of return values this policy will handle.
 */
class NoOpPolicy<TResult> private constructor(policyBuilder: Builder<TResult>) :
  Policy<TResult, NoOpPolicy.Builder<TResult>>(policyBuilder), INoOpPolicy {
  override fun implementation(context: Context, action: (Context) -> TResult?): TResult? =
    NoOpEngine.implementation(context, action)

  class Builder<TResult> : Policy.Builder<TResult, Builder<TResult>>() {
    override fun self(): Builder<TResult> = this

    fun build() = NoOpPolicy(this)
  }
}
