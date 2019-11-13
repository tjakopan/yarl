package hr.tjakopan.yarl

import java.util.concurrent.CompletionStage
import java.util.concurrent.Executor
import java.util.concurrent.ForkJoinPool

/**
 * An interface defining all executions available on an asynchronous policy generic-typed for executions returning
 * results of type [R].
 *
 * @param R The type of the result of functions executed through the policy.
 */
interface IAsyncPolicy<R> {
  companion object {
    @JvmField
    val DEFAULT_EXECUTOR: Executor = ForkJoinPool.commonPool()
  }

  /**
   * Executes the specified asynchronous action within the policy and returns the result.
   *
   * @param action The action to perform.
   * @return The value returned by the action.
   */
  @JvmDefault
  fun executeAsync(action: () -> CompletionStage<R>): CompletionStage<R> =
    executeAsync(Context()) { action() }

  /**
   * Executes the specified asynchronous action within the policy and returns the result.
   *
   * @param executor The executor to use for asynchronous execution.
   * @param action The action to perform.
   * @return The value returned by the action.
   */
  @JvmDefault
  fun executeAsync(executor: Executor, action: (Executor) -> CompletionStage<R>): CompletionStage<R> =
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
    action: (Context) -> CompletionStage<R>
  ): CompletionStage<R> =
    executeAsync(Context(contextData = contextData)) { action(it) }

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
    action: (Context, Executor) -> CompletionStage<R>
  ): CompletionStage<R> =
    executeAsync(Context(contextData = contextData), executor) { ctx, ex -> action(ctx, ex) }

  /**
   * Executes the specified asynchronous action within the policy and returns the result.
   *
   * @param context Context data that is passed to the exception policy.
   * @param action The action to perform.
   * @return The value returned by the action.
   */
  @JvmDefault
  fun executeAsync(context: Context, action: (Context) -> CompletionStage<R>): CompletionStage<R> =
    executeAsync(context, DEFAULT_EXECUTOR) { ctx, _ -> action(ctx) }

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
    action: (Context, Executor) -> CompletionStage<R>
  ): CompletionStage<R>

  /**
   * Executes the specified asynchronous action within the policy and returns the result.
   *
   * @param action The action to perform.
   * @return The captured result.
   */
  @JvmDefault
  fun executeAndCaptureAsync(action: () -> CompletionStage<R>): CompletionStage<PolicyResult<R>> =
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
    action: (Executor) -> CompletionStage<R>
  ): CompletionStage<PolicyResult<R>> =
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
    action: (Context) -> CompletionStage<R>
  ): CompletionStage<PolicyResult<R>> =
    executeAndCaptureAsync(Context(contextData = contextData)) { action(it) }

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
    action: (Context, Executor) -> CompletionStage<R>
  ): CompletionStage<PolicyResult<R>> =
    executeAndCaptureAsync(Context(contextData = contextData), executor) { ctx, ex -> action(ctx, ex) }

  /**
   * Executes the specified asynchronous action within the policy and returns the result.
   *
   * @param context Context data that is passed to the exception policy.
   * @param action The action to perform.
   * @return The captured result.
   */
  fun executeAndCaptureAsync(
    context: Context,
    action: (Context) -> CompletionStage<R>
  ): CompletionStage<PolicyResult<R>> =
    executeAndCaptureAsync(context, DEFAULT_EXECUTOR) { ctx, _ -> action(ctx) }

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
    action: (Context, Executor) -> CompletionStage<R>
  ): CompletionStage<PolicyResult<R>>
}
