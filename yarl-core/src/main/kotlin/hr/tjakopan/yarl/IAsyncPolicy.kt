package hr.tjakopan.yarl

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.future.await
import kotlinx.coroutines.future.future
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executor
import java.util.concurrent.ForkJoinPool

interface IAsyncPolicy<R> : IPolicy {
  companion object {
    @JvmSynthetic
    internal val DEFAULT_EXECUTOR: Executor = ForkJoinPool.commonPool()
  }

  @JvmSynthetic
  suspend fun execute(action: suspend () -> R): R = execute(Context()) { action() }

  @JvmSynthetic
  suspend fun execute(contextData: MutableMap<String, Any>, action: suspend (Context) -> R): R =
    execute(Context(contextData = contextData), action)

  @JvmSynthetic
  suspend fun execute(context: Context, action: suspend (Context) -> R): R

  fun executeAsync(action: () -> CompletableFuture<R>): CompletableFuture<R> = executeAsync(Context()) { action() }

  fun executeAsync(
    contextData: MutableMap<String, Any>,
    action: (Context) -> CompletableFuture<R>
  ): CompletableFuture<R> =
    executeAsync(Context(contextData = contextData)) { action(it) }

  fun executeAsync(context: Context, action: (Context) -> CompletableFuture<R>): CompletableFuture<R> =
    executeAsync(context, DEFAULT_EXECUTOR, action)

  fun executeAsync(executor: Executor, action: () -> CompletableFuture<R>): CompletableFuture<R> =
    executeAsync(Context(), executor) { action() }

  fun executeAsync(contextData: MutableMap<String, Any>, executor: Executor, action: (Context) -> CompletableFuture<R>):
    CompletableFuture<R> =
    executeAsync(Context(contextData = contextData), executor) { action(it) }

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
  suspend fun executeAndCapture(contextData: MutableMap<String, Any>, action: suspend (Context) -> R): PolicyResult<R> =
    executeAndCapture(Context(contextData = contextData)) { action(it) }

  @JvmSynthetic
  suspend fun executeAndCapture(context: Context, action: suspend (Context) -> R): PolicyResult<R>

  fun executeAndCaptureAsync(action: () -> CompletableFuture<R>): CompletableFuture<PolicyResult<R>> =
    executeAndCaptureAsync(Context()) { action() }

  fun executeAndCaptureAsync(
    contextData: MutableMap<String, Any>,
    action: (Context) -> CompletableFuture<R>
  ): CompletableFuture<PolicyResult<R>> =
    executeAndCaptureAsync(Context(contextData = contextData)) { action(it) }

  fun executeAndCaptureAsync(context: Context, action: (Context) -> CompletableFuture<R>):
    CompletableFuture<PolicyResult<R>> =
    executeAndCaptureAsync(context, DEFAULT_EXECUTOR) { ctx -> action(ctx) }

  fun executeAndCaptureAsync(executor: Executor, action: () -> CompletableFuture<R>):
    CompletableFuture<PolicyResult<R>> =
    executeAndCaptureAsync(Context(), executor) { action() }

  fun executeAndCaptureAsync(
    contextData: MutableMap<String, Any>,
    executor: Executor,
    action: (Context) -> CompletableFuture<R>
  ): CompletableFuture<PolicyResult<R>> =
    executeAndCaptureAsync(Context(contextData = contextData), executor) { ctx -> action(ctx) }

  fun executeAndCaptureAsync(context: Context, executor: Executor, action: (Context) -> CompletableFuture<R>):
    CompletableFuture<PolicyResult<R>> =
    CoroutineScope(executor.asCoroutineDispatcher()).future {
      executeAndCapture(context) { ctx ->
        action(ctx).await()
      }
    }
}
