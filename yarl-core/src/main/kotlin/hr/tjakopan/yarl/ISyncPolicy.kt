package hr.tjakopan.yarl

interface ISyncPolicy<R> : IPolicy {
  fun execute(action: () -> R): R = execute(Context.NONE) { action() }

  fun execute(contextData: MutableMap<String, Any>, action: (Context) -> R): R =
    execute(Context(contextData = contextData)) { action(it) }

  fun execute(context: Context, action: (Context) -> R): R

  fun executeAndCapture(action: () -> R): PolicyResult<R> =
    executeAndCapture(Context.NONE) { action() }

  fun executeAndCapture(contextData: MutableMap<String, Any>, action: (Context) -> R): PolicyResult<R> =
    executeAndCapture(Context(contextData = contextData)) { action(it) }

  fun executeAndCapture(context: Context, action: (Context) -> R): PolicyResult<R>
}
