package hr.tjakopan.yarl.wrap

import hr.tjakopan.yarl.Context
import hr.tjakopan.yarl.ISyncPolicy

internal object PolicyWrapEngine {
  @JvmSynthetic
  internal fun <TResult> implementation1(
    context: Context,
    outerPolicy: ISyncPolicy<TResult>,
    innerPolicy: ISyncPolicy<TResult>,
    func: (Context) -> TResult?
  ): TResult? =
    outerPolicy.execute(context) { innerPolicy.execute(it, func) }
}
