package hr.tjakopan.yarl.wrap

import hr.tjakopan.yarl.Context
import hr.tjakopan.yarl.ISyncPolicy
import hr.tjakopan.yarl.ISyncPolicyGeneric

internal object PolicyWrapEngine {
  internal fun <TResult> implementation(
    context: Context,
    outerPolicy: ISyncPolicyGeneric<TResult>,
    innerPolicy: ISyncPolicyGeneric<TResult>,
    func: (Context) -> TResult?
  ): TResult? =
    outerPolicy.execute(context) { innerPolicy.execute(it, func) }

  internal fun <TResult> implementation(
    context: Context,
    outerPolicy: ISyncPolicyGeneric<TResult>,
    innerPolicy: ISyncPolicy,
    func: (Context) -> TResult?
  ): TResult? =
    outerPolicy.execute(context) { innerPolicy.execute<TResult>(it, func) }

  internal fun <TResult> implementation(
    context: Context,
    outerPolicy: ISyncPolicy,
    innerPolicy: ISyncPolicyGeneric<TResult>,
    func: (Context) -> TResult?
  ): TResult? =
    outerPolicy.execute<TResult>(context) { innerPolicy.execute(it, func) }

  internal fun <TResult> implementation(
    context: Context,
    outerPolicy: ISyncPolicy,
    innerPolicy: ISyncPolicy,
    func: (Context) -> TResult?
  ): TResult? =
    outerPolicy.execute<TResult>(context) { innerPolicy.execute<TResult>(it, func) }

  internal fun implementation(
    context: Context,
    outerPolicy: ISyncPolicy,
    innerPolicy: ISyncPolicy,
    action: (Context) -> Unit
  ) =
    outerPolicy.execute(context) { innerPolicy.execute(it, action) }
}
