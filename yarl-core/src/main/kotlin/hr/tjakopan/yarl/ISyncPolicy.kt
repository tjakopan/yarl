package hr.tjakopan.yarl

/**
 * An interface defining all executions available on a non-generic, synchronous policy.
 */
interface ISyncPolicy : IsPolicy {
  /**
   * Sets the PolicyKey for this [Policy] instance.
   *
   * Must be called before the policy is first used. Can only be set once.
   *
   * @param policyKey The unique, used-definable key to assign to this [PolicyGeneric] instance.
   */
  fun withPolicyKey(policyKey: String): ISyncPolicy

  /**
   * Executes the specified action within the policy.
   *
   * @param action The action to perform.
   */
  @JvmDefault
  fun execute(action: () -> Unit) = execute(Context()) { action() }

  /**
   * Executes the specified action within the policy.
   *
   * @param contextData Arbitrary data that is passed to the exception policy.
   * @param action The action to perform.
   */
  @JvmDefault
  fun execute(contextData: Map<String, Any>, action: (Context) -> Unit) =
    execute(Context(contextData.toMutableMap())) { action(it) }

  /**
   * Executes the specified action within the policy.
   *
   * @param context Context data that is passed to the exception policy.
   * @param action The action to perform.
   */
  fun execute(context: Context, action: (Context) -> Unit)

  /**
   * Executes the specified action within the policy and returns the result.
   *
   * @param TResult The type of the result.
   * @param action The action to perform.
   * @return The value returned by the action.
   */
  @JvmDefault
  fun <TResult> execute(action: () -> TResult): TResult? = execute<TResult>(Context()) { action() }

  /**
   * Executes the specified action within the policy and returns the result.
   *
   * @param TResult The type of the result.
   * @param contextData Arbitrary data that is passed to the exception policy.
   * @param action The action to perform.
   * @return The value returned by the action.
   */
  @JvmDefault
  fun <TResult> execute(contextData: Map<String, Any>, action: (Context) -> TResult?): TResult? =
    execute<TResult>(Context(contextData.toMutableMap())) { action(it) }

  /**
   * Executes the specified action within the policy and returns the result.
   *
   * @param TResult The type of the result.
   * @param context Context data that is passed to the exception policy.
   * @param action The action to perform.
   * @return The value returned by the action.
   */
  fun <TResult> execute(context: Context, action: (Context) -> TResult?): TResult?

  /**
   * Executes the specified action within the policy and returns the captured result.
   *
   * @param action The action to perform.
   * @return The captured result.
   */
  @JvmDefault
  fun executeAndCapture(action: () -> Unit): PolicyResult = executeAndCapture(Context()) { action() }

  /**
   * Executes the specified action within the policy and returns the captured result.
   *
   * @param contextData Arbitrary data that is passed to the exception policy.
   * @param action The action to perform.
   * @return The captured result.
   */
  @JvmDefault
  fun executeAndCapture(contextData: Map<String, Any>, action: (Context) -> Unit): PolicyResult =
    executeAndCapture(Context(contextData.toMutableMap())) { action(it) }

  /**
   * Executes the specified action within the policy and returns the captured result.
   *
   * @param context Context data that is passed to the exception policy.
   * @param action The action to perform.
   * @return The captured result.
   */
  fun executeAndCapture(context: Context, action: (Context) -> Unit): PolicyResult

  /**
   * Executes the specified action within the policy and returns the captured result.
   *
   * @param action The action to perform.
   * @return The captured result.
   */
  @JvmDefault
  fun <TResult> executeAndCapture(action: () -> TResult?): PolicyResultGeneric<TResult> =
    executeAndCapture<TResult>(Context()) { action() }

  /**
   * Executes the specified action within the policy and returns the captured result.
   *
   * @param contextData Arbitrary data that is passed to the exception policy.
   * @param action The action to perform.
   * @return The captured result.
   */
  @JvmDefault
  fun <TResult> executeAndCapture(
    contextData: Map<String, Any>,
    action: (Context) -> TResult?
  ): PolicyResultGeneric<TResult> =
    executeAndCapture<TResult>(Context(contextData.toMutableMap())) { action(it) }

  /**
   * Executes the specified action within the policy and returns the captured result.
   *
   * @param context Context data that is passed to the exception policy.
   * @param action The action to perform.
   * @return The captured result.
   */
  fun <TResult> executeAndCapture(context: Context, action: (Context) -> TResult?): PolicyResultGeneric<TResult>
}
