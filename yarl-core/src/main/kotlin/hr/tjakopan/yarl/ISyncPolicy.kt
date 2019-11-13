package hr.tjakopan.yarl

import arrow.core.Option

/**
 * An interface defining all executions available on a synchronous policy generic-typed for executions returning results
 * of type [R].
 *
 * @param R The type of the result of functions executed through the policy.
 */
interface ISyncPolicy<R> {
  /**
   * Executes the specified action within the policy and returns the result.
   *
   * @param action The action to perform.
   * @return The value returned by the action.
   */
  @JvmDefault
  fun execute(action: () -> Option<R>): Option<R> = execute(Context.none) { action() }

  /**
   * Executes the specified action within the policy and returns the result.
   *
   * @param contextData Arbitrary data that is passed to the exception policy.
   * @param action The action to perform.
   * @return The value returned by the action.
   */
  @JvmDefault
  fun execute(contextData: Map<String, Any>, action: (Context) -> Option<R>): Option<R> =
    execute(Context(contextData = contextData)) { action(it) }

  /**
   * Executes the specified action within the policy and returns the result.
   *
   * @param context Context data that is passed to the exception policy.
   * @param action The action to perform.
   * @return The value returned by the action.
   */
  fun execute(context: Context, action: (Context) -> Option<R>): Option<R>

  /**
   * Executes the specified action within the policy and returns the captured result.
   *
   * @param action The action to perform.
   * @return The captured result.
   */
  @JvmDefault
  fun executeAndCapture(action: () -> Option<R>): PolicyResult<R> =
    executeAndCapture(Context.none) { action() }

  /**
   * Executes the specified action within the policy and returns the captured result.
   *
   * @param contextData Arbitrary data that is passed to the exception policy.
   * @param action The action to perform.
   * @return The captured result.
   */
  @JvmDefault
  fun executeAndCapture(contextData: Map<String, Any>, action: (Context) -> Option<R>): PolicyResult<R> =
    executeAndCapture(Context(contextData = contextData)) { action(it) }

  /**
   * Executes the specified action within the policy and returns the captured result.
   *
   * @param context Context data that is passed to the exception policy.
   * @param action The action to perform.
   * @return The captured result.
   */
  fun executeAndCapture(context: Context, action: (Context) -> Option<R>): PolicyResult<R>
}
