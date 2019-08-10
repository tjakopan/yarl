package hr.tjakopan.yarl

import java.util.concurrent.CompletionStage
import java.util.concurrent.Executor

/**
 * An interface defining all executions available on a non-generic, asynchronous policy.
 */
interface IAsyncPolicy : IsPolicy {
  /**
   * Sets the PolicyKey for this [IAsyncPolicy] instance.
   *
   * Must be called before the policy is first used. Can only be set once.
   *
   * @param policyKey The unique, used-definable key to assign to this [IAsyncPolicy] instance.
   */
  fun withPolicyKey(policyKey: String): IAsyncPolicy

  /**
   * Executes the specified asynchronous action within the policy.
   *
   * @param action The action to perform.
   */
  fun executeAsync(action: () -> CompletionStage<Unit>): CompletionStage<Unit>

  /**
   * Executes the specified asynchronous action within the policy.
   *
   * @param executor The executor to use for asynchronous execution.
   * @param action The action to perform.
   */
  fun executeAsync(executor: Executor, action: () -> CompletionStage<Unit>): CompletionStage<Unit>

  /**
   * Executes the specified asynchronous action within the policy.
   *
   * @param contextData Arbitrary data that is passed to the exception policy.
   * @param action The action to perform.
   */
  fun executeAsync(contextData: Map<String, Any>, action: (Context) -> CompletionStage<Unit>): CompletionStage<Unit>

  /**
   * Executes the specified asynchronous action within the policy.
   *
   * @param contextData Arbitrary data that is passed to the exception policy.
   * @param executor The executor to use for asynchronous execution.
   * @param action The action to perform.
   */
  fun executeAsync(
    contextData: Map<String, Any>,
    executor: Executor,
    action: (Context) -> CompletionStage<Unit>
  ): CompletionStage<Unit>

  /**
   * Executes the specified asynchronous action within the policy.
   *
   * @param context Context data that is passed to the exception policy.
   * @param action The action to perform.
   */
  fun executeAsync(context: Context, action: (Context) -> CompletionStage<Unit>): CompletionStage<Unit>

  /**
   * Executes the specified asynchronous action within the policy.
   *
   * @param context Context data that is passed to the exception policy.
   * @param executor The executor to use for asynchronous execution.
   * @param action The action to perform.
   */
  fun executeAsync(
    context: Context,
    executor: Executor,
    action: (Context) -> CompletionStage<Unit>
  ): CompletionStage<Unit>

  /**
   * Executes the specified asynchronous action within the policy and returns the result.
   *
   * @param TResult the type of result.
   * @param action The action to perform.
   * @return The value returned by the action.
   */
  fun <TResult> executeAsyncGeneric(action: () -> CompletionStage<TResult?>): CompletionStage<TResult?>

  /**
   * Executes the specified asynchronous action within the policy and returns the result.
   *
   * @param TResult the type of result.
   * @param executor The executor to use for asynchronous execution.
   * @param action The action to perform.
   * @return The value returned by the action.
   */
  fun <TResult> executeAsyncGeneric(
    executor: Executor,
    action: () -> CompletionStage<TResult?>
  ): CompletionStage<TResult?>

  /**
   * Executes the specified asynchronous action within the policy and returns the result.
   *
   * @param TResult the type of result.
   * @param contextData Arbitrary data that is passed to the exception policy.
   * @param action The action to perform.
   * @return The value returned by the action.
   */
  fun <TResult> executeAsyncGeneric(
    contextData: Map<String, Any>,
    action: (Context) -> CompletionStage<TResult?>
  ): CompletionStage<TResult?>

  /**
   * Executes the specified asynchronous action within the policy and returns the result.
   *
   * @param TResult the type of result.
   * @param contextData Arbitrary data that is passed to the exception policy.
   * @param executor The executor to use for asynchronous execution.
   * @param action The action to perform.
   * @return The value returned by the action.
   */
  fun <TResult> executeAsyncGeneric(
    contextData: Map<String, Any>,
    executor: Executor,
    action: (Context) -> CompletionStage<TResult?>
  ): CompletionStage<TResult?>

  /**
   * Executes the specified asynchronous action within the policy and returns the result.
   *
   * @param TResult the type of result.
   * @param context Context data that is passed to the exception policy.
   * @param action The action to perform.
   * @return The value returned by the action.
   */
  fun <TResult> executeAsyncGeneric(
    context: Context,
    action: (Context) -> CompletionStage<TResult?>
  ): CompletionStage<TResult?>

  /**
   * Executes the specified asynchronous action within the policy and returns the result.
   *
   * @param TResult the type of result.
   * @param context Context data that is passed to the exception policy.
   * @param executor The executor to use for asynchronous execution.
   * @param action The action to perform.
   * @return The value returned by the action.
   */
  fun <TResult> executeAsyncGeneric(
    context: Context,
    executor: Executor,
    action: (Context) -> CompletionStage<TResult?>
  ): CompletionStage<TResult?>

  /**
   * Executes the specified asynchronous action within the policy and returns the captured result.
   *
   * @param action The action to perform.
   * @return The captured result.
   */
  fun executeAndCaptureAsync(action: () -> CompletionStage<Unit>): CompletionStage<PolicyResult>

  /**
   * Executes the specified asynchronous action within the policy and returns the captured result.
   *
   * @param executor The executor to use for asynchronous execution.
   * @param action The action to perform.
   * @return The captured result.
   */
  fun executeAndCaptureAsync(executor: Executor, action: () -> CompletionStage<Unit>): CompletionStage<PolicyResult>

  /**
   * Executes the specified asynchronous action within the policy and returns the captured result.
   *
   * @param contextData Arbitrary data that is passed to the exception policy.
   * @param action The action to perform.
   * @return The captured result.
   */
  fun executeAndCaptureAsync(
    contextData: Map<String, Any>,
    action: (Context) -> CompletionStage<Unit>
  ): CompletionStage<PolicyResult>

  /**
   * Executes the specified asynchronous action within the policy and returns the captured result.
   *
   * @param contextData Arbitrary data that is passed to the exception policy.
   * @param executor The executor to use for asynchronous execution.
   * @param action The action to perform.
   * @return The captured result.
   */
  fun executeAndCaptureAsync(
    contextData: Map<String, Any>,
    executor: Executor,
    action: (Context) -> CompletionStage<Unit>
  ): CompletionStage<PolicyResult>

  /**
   * Executes the specified asynchronous action within the policy and returns the captured result.
   *
   * @param context Context data that is passed to the exception policy.
   * @param action The action to perform.
   * @return The captured result.
   */
  fun executeAndCaptureAsync(
    context: Context,
    action: (Context) -> CompletionStage<Unit>
  ): CompletionStage<PolicyResult>

  /**
   * Executes the specified asynchronous action within the policy and returns the captured result.
   *
   * @param context Context data that is passed to the exception policy.
   * @param executor The executor to use for asynchronous execution.
   * @param action The action to perform.
   * @return The captured result.
   */
  fun executeAndCaptureAsync(
    context: Context,
    executor: Executor,
    action: (Context) -> CompletionStage<Unit>
  ): CompletionStage<PolicyResult>

  /**
   * Executes the specified asynchronous action within the policy and returns the result.
   *
   * @param TResult The type of the result.
   * @param action The action to perform.
   * @return The captured result.
   */
  fun <TResult> executeAndCaptureAsyncGeneric(action: () -> CompletionStage<TResult?>): CompletionStage<PolicyResultGeneric<TResult?>>

  /**
   * Executes the specified asynchronous action within the policy and returns the result.
   *
   * @param TResult The type of the result.
   * @param executor The executor to use for asynchronous execution.
   * @param action The action to perform.
   * @return The captured result.
   */
  fun <TResult> executeAndCaptureAsyncGeneric(
    executor: Executor,
    action: () -> CompletionStage<TResult?>
  ): CompletionStage<PolicyResultGeneric<TResult?>>

  /**
   * Executes the specified asynchronous action within the policy and returns the result.
   *
   * @param TResult The type of the result.
   * @param contextData Arbitrary data that is passed to the exception policy.
   * @param action The action to perform.
   * @return The captured result.
   */
  fun <TResult> executeAndCaptureAsyncGeneric(
    contextData: Map<String, Any>,
    action: (Context) -> CompletionStage<TResult?>
  ): CompletionStage<PolicyResultGeneric<TResult?>>

  /**
   * Executes the specified asynchronous action within the policy and returns the result.
   *
   * @param TResult The type of the result.
   * @param contextData Arbitrary data that is passed to the exception policy.
   * @param executor The executor to use for asynchronous execution.
   * @param action The action to perform.
   * @return The captured result.
   */
  fun <TResult> executeAndCaptureAsyncGeneric(
    contextData: Map<String, Any>,
    executor: Executor,
    action: (Context) -> CompletionStage<TResult?>
  ): CompletionStage<PolicyResultGeneric<TResult?>>

  /**
   * Executes the specified asynchronous action within the policy and returns the result.
   *
   * @param TResult The type of the result.
   * @param context Context data that is passed to the exception policy.
   * @param action The action to perform.
   * @return The captured result.
   */
  fun <TResult> executeAndCaptureAsyncGeneric(
    context: Context,
    action: (Context) -> CompletionStage<TResult?>
  ): CompletionStage<PolicyResultGeneric<TResult?>>

  /**
   * Executes the specified asynchronous action within the policy and returns the result.
   *
   * @param TResult The type of the result.
   * @param context Context data that is passed to the exception policy.
   * @param executor The executor to use for asynchronous execution.
   * @param action The action to perform.
   * @return The captured result.
   */
  fun <TResult> executeAndCaptureAsyncGeneric(
    context: Context,
    executor: Executor,
    action: (Context) -> CompletionStage<TResult?>
  ): CompletionStage<PolicyResultGeneric<TResult?>>
}
