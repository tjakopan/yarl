package hr.tjakopan.yarl.noop

import hr.tjakopan.yarl.Context

internal object NoOpEngine {
  @JvmSynthetic
  fun <R> implementation(context: Context, action: (Context) -> R): R = action(context)

  @JvmSynthetic
  suspend fun <R> implementation(context: Context, action: suspend (Context) -> R): R = action(context)
}
