package hr.tjakopan.yarl.noop

import hr.tjakopan.yarl.Context
import hr.tjakopan.yarl.Policy
import hr.tjakopan.yarl.PolicyGeneric

/**
 * A no op policy that can be applied to delegates.
 */
class NoOpPolicy internal constructor() : Policy(), INoOpPolicy {
  override fun <TResult> implementation(context: Context, action: (Context) -> TResult?): TResult? =
    NoOpEngine.implementation(context, action)
}

/**
 * A no op policy that can be applied to delegates returning a value of type [TResult].
 *
 * @param TResult The type of return values this policy will handle.
 */
class NoOpPolicyGeneric<TResult> internal constructor() : PolicyGeneric<TResult>(), INoOpPolicyGeneric<TResult> {
  override fun implementation(context: Context, action: (Context) -> TResult?): TResult? =
    NoOpEngine.implementation(context, action)
}
