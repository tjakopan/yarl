package hr.tjakopan.yarl

interface ISyncPolicy<R> {
  @JvmDefault
  fun execute(action: () -> R): R = execute(Context.NONE) { action() }

  @JvmDefault
  fun execute(contextData: Map<String, Any>, action: (Context) -> R): R =
    execute(Context(contextData = contextData)) { action(it) }

  fun execute(context: Context, action: (Context) -> R): R

  @JvmDefault
  fun executeAndCapture(action: () -> R): PolicyResult<R> =
    executeAndCapture(Context.NONE) { action() }

  @JvmDefault
  fun executeAndCapture(contextData: Map<String, Any>, action: (Context) -> R): PolicyResult<R> =
    executeAndCapture(Context(contextData = contextData)) { action(it) }

  fun executeAndCapture(context: Context, action: (Context) -> R): PolicyResult<R>
}
