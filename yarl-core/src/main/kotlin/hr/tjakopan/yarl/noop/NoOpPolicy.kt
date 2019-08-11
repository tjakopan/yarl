package hr.tjakopan.yarl.noop

import hr.tjakopan.yarl.Context
import hr.tjakopan.yarl.Policy

/**
 * A no op policy that can be applied to delegates.
 */
class NoOpPolicy internal constructor() : Policy(), INoOpPolicy {
  override fun implementation(context: Context, action: (Context) -> Unit) =
    NoOpEngine.implementation(context, action)

  override fun <TResult> implementation(context: Context, action: (Context) -> TResult?): TResult? =
    NoOpEngine.implementation(context, action)

  companion object {
    /**
     * Builds a NoOp [Policy] that will execute without any custom behavior.
     *
     * @return The policy instance.
     */
    @JvmStatic
    fun noOp() = NoOpPolicy()
  }
}
