package hr.tjakopan.yarl.noop

import hr.tjakopan.yarl.Context
import hr.tjakopan.yarl.Policy

/**
 * A no op policy that can be applied to delegates returning a value of type [TResult].
 *
 * @param TResult The type of return values this policy will handle.
 */
class NoOpPolicy<TResult> internal constructor() : Policy<TResult>(), INoOpPolicy {
  override fun implementation(context: Context, action: (Context) -> TResult?): TResult? =
    NoOpEngine.implementation(context, action)

  companion object {
    /**
     * Builds a NoOp [Policy] that will execute without any custom behavior.
     *
     * @param TResult The type of return values this policy will handle.
     * @return The policy instance.
     */
    @JvmStatic
    fun <TResult> noOp() = NoOpPolicy<TResult>()
  }
}
