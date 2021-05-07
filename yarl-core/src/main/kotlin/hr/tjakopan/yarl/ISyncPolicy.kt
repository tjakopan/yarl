package hr.tjakopan.yarl

interface ISyncPolicy<R> : IPolicy {
  fun execute(action: () -> R): R = execute(Context.none()) { action() }

  fun execute(contextData: Map<String, Any>, action: (Context) -> R): R = execute(Context(contextData), action)

  fun execute(context: Context, action: (Context) -> R): R

  fun executeAndCapture(action: () -> R): PolicyResult<R> = executeAndCapture(Context.none()) { action() }

  fun executeAndCapture(contextData: Map<String, Any>, action: (Context) -> R): PolicyResult<R> =
    executeAndCapture(Context(contextData), action)

  fun executeAndCapture(context: Context, action: (Context) -> R): PolicyResult<R>
}
