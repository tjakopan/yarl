package hr.tjakopan.yarl

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.future.await
import kotlinx.coroutines.future.future
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executor
import java.util.concurrent.ForkJoinPool
import java.util.function.Supplier

interface IAsyncPolicy<R> {
  companion object {
    @JvmSynthetic
    internal val DEFAULT_EXECUTOR: Executor = ForkJoinPool.commonPool()
  }

  @JvmDefault
  suspend fun execute(action: suspend () -> R): R = execute(Context()) { action() }

  @JvmDefault
  suspend fun execute(contextData: Map<String, Any>, action: suspend (Context) -> R): R =
    execute(Context(contextData = contextData), action)

  suspend fun execute(context: Context, action: suspend (Context) -> R): R

  @JvmDefault
  fun executeAsync(action: () -> R): CompletableFuture<R> = executeAsync(Context()) { action() }

  @JvmDefault
  fun executeAsync(contextData: Map<String, Any>, action: (Context) -> R): CompletableFuture<R> =
    executeAsync(Context(contextData = contextData)) { action(it) }

  @JvmDefault
  fun executeAsync(context: Context, action: (Context) -> R): CompletableFuture<R> =
    executeAsync(context, DEFAULT_EXECUTOR, action)

  @JvmDefault
  fun executeAsync(executor: Executor, action: () -> R): CompletableFuture<R> =
    executeAsync(Context(), executor) { action() }

  @JvmDefault
  fun executeAsync(contextData: Map<String, Any>, executor: Executor, action: (Context) -> R): CompletableFuture<R> =
    executeAsync(Context(contextData = contextData), executor) { action(it) }

  @JvmDefault
  fun executeAsync(context: Context, executor: Executor, action: (Context) -> R): CompletableFuture<R> =
    CoroutineScope(executor.asCoroutineDispatcher()).future {
      execute(context) { ctx ->
        CompletableFuture.supplyAsync(Supplier { action(ctx) }, executor).await()
      }
    }

  @JvmDefault
  suspend fun executeAndCapture(action: suspend () -> R): PolicyResult<R> = executeAndCapture(Context()) { action() }

  @JvmDefault
  suspend fun executeAndCapture(contextData: Map<String, Any>, action: suspend (Context) -> R): PolicyResult<R> =
    executeAndCapture(Context(contextData = contextData)) { action(it) }

  suspend fun executeAndCapture(context: Context, action: suspend (Context) -> R): PolicyResult<R>

  @JvmDefault
  fun executeAndCaptureAsync(action: () -> R): CompletableFuture<PolicyResult<R>> =
    executeAndCaptureAsync(Context()) { action() }

  @JvmDefault
  fun executeAndCaptureAsync(
    contextData: Map<String, Any>,
    action: (Context) -> R
  ): CompletableFuture<PolicyResult<R>> =
    executeAndCaptureAsync(Context(contextData = contextData)) { action(it) }

  @JvmDefault
  fun executeAndCaptureAsync(context: Context, action: (Context) -> R): CompletableFuture<PolicyResult<R>> =
    executeAndCaptureAsync(context, DEFAULT_EXECUTOR) { ctx -> action(ctx) }

  @JvmDefault
  fun executeAndCaptureAsync(executor: Executor, action: () -> R): CompletableFuture<PolicyResult<R>> =
    executeAndCaptureAsync(Context(), executor) { action() }

  @JvmDefault
  fun executeAndCaptureAsync(
    contextData: Map<String, Any>,
    executor: Executor,
    action: (Context) -> R
  ): CompletableFuture<PolicyResult<R>> =
    executeAndCaptureAsync(Context(contextData = contextData), executor) { ctx -> action(ctx) }

  @JvmDefault
  fun executeAndCaptureAsync(
    context: Context,
    executor: Executor,
    action: (Context) -> R
  ): CompletableFuture<PolicyResult<R>> = CoroutineScope(executor.asCoroutineDispatcher()).future {
    executeAndCapture(context) { ctx ->
      CompletableFuture.supplyAsync(Supplier { action(ctx) }, executor).await()
    }
  }
}
