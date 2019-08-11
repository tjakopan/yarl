package hr.tjakopan.yarl.noop

import hr.tjakopan.yarl.Context

internal object NoOpEngine {
  fun implementation(context: Context, action: (Context) -> Unit) = action(context)

  fun <TResult> implementation(context: Context, action: (Context) -> TResult?): TResult? = action(context)
}
