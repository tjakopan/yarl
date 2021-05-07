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
  suspend fun execute(action: suspend () -> R): R = execute(Context.none()) { action() }

  @JvmSynthetic
  suspend fun execute(contextData: Map<String, Any>, action: suspend (Context) -> R): R =
    execute(Context(contextData), action)

  @JvmSynthetic
  suspend fun execute(context: Context, action: suspend (Context) -> R): R

  fun executeAsync(action: () -> CompletableFuture<R>): CompletableFuture<R> = executeAsync(Context.none()) { action() }

  fun executeAsync(contextData: Map<String, Any>, action: (Context) -> CompletableFuture<R>): CompletableFuture<R> =
    executeAsync(Context(contextData), action)

  fun executeAsync(context: Context, action: (Context) -> CompletableFuture<R>): CompletableFuture<R> =
    executeAsync(context, DEFAULT_EXECUTOR, action)

  fun executeAsync(executor: Executor, action: () -> CompletableFuture<R>): CompletableFuture<R> =
    executeAsync(Context.none(), executor) { action() }

  fun executeAsync(
    contextData: Map<String, Any>,
    executor: Executor,
    action: (Context) -> CompletableFuture<R>
  ): CompletableFuture<R> = executeAsync(Context(contextData), executor, action)

  fun executeAsync(context: Context, executor: Executor, action: (Context) -> CompletableFuture<R>):
    CompletableFuture<R> =
    CoroutineScope(executor.asCoroutineDispatcher()).future {
      execute(context) { ctx ->
        action(ctx).await()
      }
    }

  @JvmSynthetic
  suspend fun executeAndCapture(action: suspend () -> R): PolicyResult<R> =
    executeAndCapture(Context.none()) { action() }

  @JvmSynthetic
  suspend fun executeAndCapture(contextData: Map<String, Any>, action: suspend (Context) -> R): PolicyResult<R> =
    executeAndCapture(Context(contextData), action)

  @JvmSynthetic
  suspend fun executeAndCapture(context: Context, action: suspend (Context) -> R): PolicyResult<R>

  fun executeAndCaptureAsync(action: () -> CompletableFuture<R>): CompletableFuture<PolicyResult<R>> =
    executeAndCaptureAsync(Context.none()) { action() }

  fun executeAndCaptureAsync(
    contextData: Map<String, Any>,
    action: (Context) -> CompletableFuture<R>
  ): CompletableFuture<PolicyResult<R>> = executeAndCaptureAsync(Context(contextData), action)

  fun executeAndCaptureAsync(
    context: Context,
    action: (Context) -> CompletableFuture<R>
  ): CompletableFuture<PolicyResult<R>> = executeAndCaptureAsync(context, DEFAULT_EXECUTOR, action)

  fun executeAndCaptureAsync(
    executor: Executor,
    action: () -> CompletableFuture<R>
  ): CompletableFuture<PolicyResult<R>> = executeAndCaptureAsync(Context.none(), executor) { action() }

  fun executeAndCaptureAsync(
    contextData: Map<String, Any>,
    executor: Executor,
    action: (Context) -> CompletableFuture<R>
  ): CompletableFuture<PolicyResult<R>> = executeAndCaptureAsync(Context(contextData), executor, action)

  fun executeAndCaptureAsync(context: Context, executor: Executor, action: (Context) -> CompletableFuture<R>):
    CompletableFuture<PolicyResult<R>> =
    CoroutineScope(executor.asCoroutineDispatcher()).future {
      executeAndCapture(context) { ctx ->
        action(ctx).await()
      }
    }
}
