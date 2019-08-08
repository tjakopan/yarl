package hr.tjakopan.yarl

/**
 * An interface defining all executions available on a synchronous policy generic-typed for executions returning results
 * of type [TResult].
 *
 * @param TResult The type of the result of functions executed through the policy.
 */
internal interface ISyncPolicy<TResult> : IsPolicy {
  /**
   * Executes the specified action within the policy and returns the result.
   *
   * @param action The action to perform.
   * @return The value returned by the action.
   */
  @JvmDefault
  fun execute(action: () -> TResult?): TResult? = execute(Context()) { action() }

  /**
   * Executes the specified action within the policy and returns the result.
   *
   * @param contextData Arbitrary data that is passed to the exception policy.
   * @param action The action to perform.
   * @return The value returned by the action.
   */
  @JvmDefault
  fun execute(contextData: MutableMap<String, Any>, action: (Context) -> TResult?): TResult? =
    execute(Context(contextData)) { action(it) }

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
  @JvmDefault
  fun executeAndCapture(action: () -> TResult?): PolicyResult<TResult> = executeAndCapture(Context()) { action() }

  /**
   * Executes the specified action within the policy and returns the captured result.
   *
   * @param contextData Arbitrary data that is passed to the exception policy.
   * @param action The action to perform.
   * @return The captured result.
   */
  @JvmDefault
  fun executeAndCapture(contextData: MutableMap<String, Any>, action: (Context) -> TResult?): PolicyResult<TResult> =
    executeAndCapture(Context(contextData)) { action(it) }

  /**
   * Executes the specified action within the policy and returns the captured result.
   *
   * @param context Context data that is passed to the exception policy.
   * @param action The action to perform.
   * @return The captured result.
   */
  fun executeAndCapture(context: Context, action: (Context) -> TResult?): PolicyResult<TResult>
}
