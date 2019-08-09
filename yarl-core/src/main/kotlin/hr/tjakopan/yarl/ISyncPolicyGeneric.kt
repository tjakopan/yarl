package hr.tjakopan.yarl

/**
 * An interface defining all executions available on a synchronous policy generic-typed for executions returning results
 * of type [TResult].
 *
 * @param TResult The type of the result of functions executed through the policy.
 */
interface ISyncPolicyGeneric<TResult> : IsPolicy {
  /**
   * Sets the PolicyKey for this [PolicyGeneric] instance.
   *
   * Must be called before the policy is first used. Can only be set once.
   *
   * @param policyKey The unique, used-definable key to assign to this [PolicyGeneric] instance.
   */
  fun withPolicyKey(policyKey: String): ISyncPolicyGeneric<TResult>

  /**
   * Executes the specified action within the policy and returns the result.
   *
   * @param action The action to perform.
   * @return The value returned by the action.
   */
  fun execute(action: () -> TResult?): TResult?

  /**
   * Executes the specified action within the policy and returns the result.
   *
   * @param contextData Arbitrary data that is passed to the exception policy.
   * @param action The action to perform.
   * @return The value returned by the action.
   */
  fun execute(contextData: Map<String, Any>, action: (Context) -> TResult?): TResult?

  /**
   * Executes the specified action within the policy and returns the result.
   *
   * @param context Context data that is passed to the exception policy.
   * @param action The action to perform.
   * @return The value returned by the action.
   */
  fun execute(context: Context, action: (Context) -> TResult?): TResult?

  /**
   * Executes the specified action within the policy and returns the captured result.
   *
   * @param action The action to perform.
   * @return The captured result.
   */
  fun executeAndCapture(action: () -> TResult?): PolicyResultGeneric<TResult>

  /**
   * Executes the specified action within the policy and returns the captured result.
   *
   * @param contextData Arbitrary data that is passed to the exception policy.
   * @param action The action to perform.
   * @return The captured result.
   */
  fun executeAndCapture(contextData: Map<String, Any>, action: (Context) -> TResult?): PolicyResultGeneric<TResult>

  /**
   * Executes the specified action within the policy and returns the captured result.
   *
   * @param context Context data that is passed to the exception policy.
   * @param action The action to perform.
   * @return The captured result.
   */
  fun executeAndCapture(context: Context, action: (Context) -> TResult?): PolicyResultGeneric<TResult>
}
