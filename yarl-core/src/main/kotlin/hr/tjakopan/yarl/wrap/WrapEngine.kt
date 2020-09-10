package hr.tjakopan.yarl.wrap

import hr.tjakopan.yarl.Context
import hr.tjakopan.yarl.IAsyncPolicy
import hr.tjakopan.yarl.ISyncPolicy

internal object WrapEngine {
  @JvmSynthetic
  fun <R> implementation(
    action: (Context) -> R,
    context: Context,
    outerPolicy: ISyncPolicy<R>,
    innerPolicy: ISyncPolicy<R>
  ): R = outerPolicy.execute(context) { ctx -> innerPolicy.execute(ctx, action) }

  @JvmSynthetic
  suspend fun <R> implementation(
    action: suspend (Context) -> R,
    context: Context,
    outerPolicy: IAsyncPolicy<R>,
    innerPolicy: IAsyncPolicy<R>
  ): R = outerPolicy.execute(context) { ctx -> innerPolicy.execute(ctx, action) }
}
