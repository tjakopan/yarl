package hr.tjakopan.yarl

/**
 * An interface defining all executions available on a synchronous policy generic-typed for executions returning results
 * of type [TResult].
 *
 * @param TResult The type of the result of functions executed through the policy.
 */
interface ISyncPolicy<TResult> : IsPolicy {
  /**
   * Sets the policy key for this [Policy] instance.
   *
   * Must be called before the policy is first used. Can only be set once.
   *
   * @param policyKey The unique, user-definable key to assign to this [Policy] instance.
   */
  fun withPolicyKey(policyKey: String): ISyncPolicy<TResult>;

  @JvmDefault
  fun execute(action: () -> TResult): TResult = execute(Context()) { action() }

  @JvmDefault
  fun execute(contextData: MutableMap<String, Any>, action: (Context) -> TResult): TResult =
    execute(Context(contextData)) { action(it) }

  fun execute(context: Context, action: (Context) -> TResult): TResult

  @JvmDefault
  fun executeAndCapture(action: () -> TResult): PolicyResult<TResult> = executeAndCapture(Context()) { action() }

  @JvmDefault
  fun executeAndCapture(contextData: MutableMap<String, Any>, action: (Context) -> TResult): PolicyResult<TResult> =
    executeAndCapture(Context(contextData)) { action(it) }

  fun executeAndCapture(context: Context, action: (Context) -> TResult): PolicyResult<TResult>
}
