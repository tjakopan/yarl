package hr.tjakopan.yarl

import java.util.concurrent.CompletableFuture

/**
 * An interface defining all executions available on an asynchronous policy genery-type for executions returning
 * results of type [TResult].
 *
 * @param TResult The type of the result of functions executed through the policy.
 */
interface IAsyncPolicy<TResult> : IsPolicy {
  @JvmDefault
  fun executeAsync(action: () -> TResult?): CompletableFuture<TResult?> = executeAsync(Context()) { action() }

  @JvmDefault
  fun executeAsync(contextData: Map<String, Any>, action: (Context) -> TResult?): CompletableFuture<TResult?> =
    executeAsync(Context(contextData.toMutableMap())) { action(it) }

  fun executeAsync(context: Context, action: (Context) -> TResult?): CompletableFuture<TResult?>

  @JvmDefault
  fun executeAndCaptureAsync(action: () -> TResult?): CompletableFuture<PolicyResultGeneric<TResult?>> =
    executeAndCaptureAsync(Context()) { action() }

  @JvmDefault
  fun executeAndCaptureAsync(
    contextData: Map<String, Any>,
    action: (Context) -> TResult?
  ): CompletableFuture<PolicyResultGeneric<TResult?>> = executeAndCaptureAsync(
    Context(contextData.toMutableMap())
  ) { action(it) }

  fun executeAndCaptureAsync(context: Context, action: (Context) -> TResult?): CompletableFuture<PolicyResultGeneric<TResult?>>
}
