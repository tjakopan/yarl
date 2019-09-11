package hr.tjakopan.yarl

import java.util.concurrent.CompletionStage
import java.util.concurrent.Executor

/**
 * An interface defining all executions available on an asynchronous policy generic-typed for executions returning
 * results of type [TResult].
 *
 * @param TResult The type of the result of functions executed through the policy.
 */
interface IAsyncPolicy<TResult> : IsPolicy {
  /**
   * Executes the specified asynchronous action within the policy and returns the result.
   *
   * @param action The action to perform.
   * @return The value returned by the action.
   */
  @JvmDefault
  fun executeAsync(action: () -> CompletionStage<TResult>): CompletionStage<TResult> =
    executeAsync(Context()) { action() }

  /**
   * Executes the specified asynchronous action within the policy and returns the result.
   *
   * @param executor The executor to use for asynchronous execution.
   * @param action The action to perform.
   * @return The value returned by the action.
   */
  @JvmDefault
  fun executeAsync(executor: Executor, action: (Executor) -> CompletionStage<TResult>): CompletionStage<TResult> =
    executeAsync(Context(), executor) { _, ex -> action(ex) }

  /**
   * Executes the specified asynchronous action within the policy and returns the result.
   *
   * @param contextData Arbitrary data that is passed to the exception policy.
   * @param action The action to perform.
   * @return The value returned by the action.
   */
  @JvmDefault
  fun executeAsync(
    contextData: Map<String, Any>,
    action: (Context) -> CompletionStage<TResult>
  ): CompletionStage<TResult> =
    executeAsync(Context(contextData.toMutableMap())) { action(it) }

  /**
   * Executes the specified asynchronous action within the policy and returns the result.
   *
   * @param contextData Arbitrary data that is passed to the exception policy.
   * @param executor The executor to use for asynchronous execution.
   * @param action The action to perform.
   * @return The value returned by the action.
   */
  @JvmDefault
  fun executeAsync(
    contextData: Map<String, Any>,
    executor: Executor,
    action: (Context, Executor) -> CompletionStage<TResult>
  ): CompletionStage<TResult> =
    executeAsync(Context(contextData.toMutableMap()), executor) { ctx, ex -> action(ctx, ex) }

  /**
   * Executes the specified asynchronous action within the policy and returns the result.
   *
   * @param context Context data that is passed to the exception policy.
   * @param action The action to perform.
   * @return The value returned by the action.
   */
  fun executeAsync(context: Context, action: (Context) -> CompletionStage<TResult>): CompletionStage<TResult>

  /**
   * Executes the specified asynchronous action within the policy and returns the result.
   *
   * @param context Context data that is passed to the exception policy.
   * @param executor The executor to use for asynchronous execution.
   * @param action The action to perform.
   * @return The value returned by the action.
   */
  fun executeAsync(
    context: Context,
    executor: Executor,
    action: (Context, Executor) -> CompletionStage<TResult>
  ): CompletionStage<TResult>

  /**
   * Executes the specified asynchronous action within the policy and returns the result.
   *
   * @param action The action to perform.
   * @return The captured result.
   */
  @JvmDefault
  fun executeAndCaptureAsync(action: () -> CompletionStage<TResult>): CompletionStage<PolicyResult<TResult>> =
    executeAndCaptureAsync(Context()) { action() }

  /**
   * Executes the specified asynchronous action within the policy and returns the result.
   *
   * @param executor The executor to use for asynchronous execution.
   * @param action The action to perform.
   * @return The captured result.
   */
  @JvmDefault
  fun executeAndCaptureAsync(
    executor: Executor,
    action: (Executor) -> CompletionStage<TResult>
  ): CompletionStage<PolicyResult<TResult>> =
    executeAndCaptureAsync(Context(), executor) { _, ex -> action(ex) }

  /**
   * Executes the specified asynchronous action within the policy and returns the result.
   *
   * @param contextData Arbitrary data that is passed to the exception policy.
   * @param action The action to perform.
   * @return The captured result.
   */
  @JvmDefault
  fun executeAndCaptureAsync(
    contextData: Map<String, Any>,
    action: (Context) -> CompletionStage<TResult>
  ): CompletionStage<PolicyResult<TResult>> =
    executeAndCaptureAsync(Context(contextData.toMutableMap())) { action(it) }

  /**
   * Executes the specified asynchronous action within the policy and returns the result.
   *
   * @param contextData Arbitrary data that is passed to the exception policy.
   * @param executor The executor to use for asynchronous execution.
   * @param action The action to perform.
   * @return The captured result.
   */
  @JvmDefault
  fun executeAndCaptureAsync(
    contextData: Map<String, Any>,
    executor: Executor,
    action: (Context, Executor) -> CompletionStage<TResult>
  ): CompletionStage<PolicyResult<TResult>> =
    executeAndCaptureAsync(Context(contextData.toMutableMap()), executor) { ctx, ex -> action(ctx, ex) }

  /**
   * Executes the specified asynchronous action within the policy and returns the result.
   *
   * @param context Context data that is passed to the exception policy.
   * @param action The action to perform.
   * @return The captured result.
   */
  fun executeAndCaptureAsync(
    context: Context,
    action: (Context) -> CompletionStage<TResult>
  ): CompletionStage<PolicyResult<TResult>>

  /**
   * Executes the specified asynchronous action within the policy and returns the result.
   *
   * @param context Context data that is passed to the exception policy.
   * @param executor The executor to use for asynchronous execution.
   * @param action The action to perform.
   * @return The captured result.
   */
  fun executeAndCaptureAsync(
    context: Context,
    executor: Executor,
    action: (Context, Executor) -> CompletionStage<TResult>
  ): CompletionStage<PolicyResult<TResult>>
}
