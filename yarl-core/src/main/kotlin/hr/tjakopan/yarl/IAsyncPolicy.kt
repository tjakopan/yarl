package hr.tjakopan.yarl

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.future.await
import kotlinx.coroutines.future.future
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executor
import java.util.concurrent.ForkJoinPool

interface IAsyncPolicy<R> {
  companion object {
    @JvmSynthetic
    internal val DEFAULT_EXECUTOR: Executor = ForkJoinPool.commonPool()
  }

  @JvmSynthetic
  suspend fun execute(action: suspend () -> R): R = execute(Context()) { action() }

  @JvmSynthetic
  suspend fun execute(contextData: Map<String, Any>, action: suspend (Context) -> R): R =
    execute(Context(contextData = contextData), action)

  @JvmSynthetic
  suspend fun execute(context: Context, action: suspend (Context) -> R): R

  @JvmDefault
  fun executeAsync(action: () -> CompletableFuture<R>): CompletableFuture<R> = executeAsync(Context()) { action() }

  @JvmDefault
  fun executeAsync(contextData: Map<String, Any>, action: (Context) -> CompletableFuture<R>): CompletableFuture<R> =
    executeAsync(Context(contextData = contextData)) { action(it) }

  @JvmDefault
  fun executeAsync(context: Context, action: (Context) -> CompletableFuture<R>): CompletableFuture<R> =
    executeAsync(context, DEFAULT_EXECUTOR, action)

  @JvmDefault
  fun executeAsync(executor: Executor, action: () -> CompletableFuture<R>): CompletableFuture<R> =
    executeAsync(Context(), executor) { action() }

  @JvmDefault
  fun executeAsync(contextData: Map<String, Any>, executor: Executor, action: (Context) -> CompletableFuture<R>):
    CompletableFuture<R> =
    executeAsync(Context(contextData = contextData), executor) { action(it) }

  @JvmDefault
  fun executeAsync(context: Context, executor: Executor, action: (Context) -> CompletableFuture<R>):
    CompletableFuture<R> =
    CoroutineScope(executor.asCoroutineDispatcher()).future {
      execute(context) { ctx ->
        action(ctx).await()
      }
    }

  @JvmSynthetic
  suspend fun executeAndCapture(action: suspend () -> R): PolicyResult<R> = executeAndCapture(Context()) { action() }

  @JvmSynthetic
  suspend fun executeAndCapture(contextData: Map<String, Any>, action: suspend (Context) -> R): PolicyResult<R> =
    executeAndCapture(Context(contextData = contextData)) { action(it) }

  @JvmSynthetic
  suspend fun executeAndCapture(context: Context, action: suspend (Context) -> R): PolicyResult<R>

  @JvmDefault
  fun executeAndCaptureAsync(action: () -> CompletableFuture<R>): CompletableFuture<PolicyResult<R>> =
    executeAndCaptureAsync(Context()) { action() }

  @JvmDefault
  fun executeAndCaptureAsync(
    contextData: Map<String, Any>,
    action: (Context) -> CompletableFuture<R>
  ): CompletableFuture<PolicyResult<R>> =
    executeAndCaptureAsync(Context(contextData = contextData)) { action(it) }

  @JvmDefault
  fun executeAndCaptureAsync(context: Context, action: (Context) -> CompletableFuture<R>):
    CompletableFuture<PolicyResult<R>> =
    executeAndCaptureAsync(context, DEFAULT_EXECUTOR) { ctx -> action(ctx) }

  @JvmDefault
  fun executeAndCaptureAsync(executor: Executor, action: () -> CompletableFuture<R>):
    CompletableFuture<PolicyResult<R>> =
    executeAndCaptureAsync(Context(), executor) { action() }

  @JvmDefault
  fun executeAndCaptureAsync(
    contextData: Map<String, Any>,
    executor: Executor,
    action: (Context) -> CompletableFuture<R>
  ): CompletableFuture<PolicyResult<R>> =
    executeAndCaptureAsync(Context(contextData = contextData), executor) { ctx -> action(ctx) }

  @JvmDefault
  fun executeAndCaptureAsync(context: Context, executor: Executor, action: (Context) -> CompletableFuture<R>):
    CompletableFuture<PolicyResult<R>> =
    CoroutineScope(executor.asCoroutineDispatcher()).future {
      executeAndCapture(context) { ctx ->
        action(ctx).await()
      }
    }
}
