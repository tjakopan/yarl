package hr.tjakopan.yarl

import hr.tjakopan.yarl.noop.NoOpPolicyGeneric
import hr.tjakopan.yarl.wrap.PolicyWrapGeneric

open class Policy : PolicyBase(), ISyncPolicy {
  companion object {
    /**
     * Builds a NoOp [PolicyGeneric] that will execute without any custom behaviour.
     *
     * @param TResult The type of return values this policy will handle.
     * @return The policy instance.
     */
    fun <TResult> noOp() = NoOpPolicyGeneric<TResult>();
  }

  override fun withPolicyKey(policyKey: String): ISyncPolicy {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
  }

  override fun execute(action: () -> Unit) {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
  }

  override fun execute(contextData: Map<String, Any>, action: (Context) -> Unit) {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
  }

  override fun execute(context: Context, action: (Context) -> Unit) {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
  }

  override fun executeAndCapture(action: () -> Unit): PolicyResult {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
  }

  override fun executeAndCapture(contextData: Map<String, Any>, action: (Context) -> Unit): PolicyResult {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
  }

  override fun executeAndCapture(context: Context, action: (Context) -> Unit): PolicyResult {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
  }

  /**
   * Wraps the specified inner policy.
   *
   * @param TResult The return type of delegates which may be executed through the policy.
   * @param innerPolicy The inner policy.
   * @return The [PolicyWrapGeneric] instance.
   */
  fun <TResult> wrap(innerPolicy: ISyncPolicyGeneric<TResult>) = PolicyWrapGeneric(this, innerPolicy)
}
