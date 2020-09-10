package hr.tjakopan.yarl.wrap

import hr.tjakopan.yarl.IAsyncPolicy
import hr.tjakopan.yarl.ISyncPolicy

fun <R> ISyncPolicy<R>.wrap(innerPolicy: ISyncPolicy<R>): WrapPolicy<R> =
  WrapPolicyBuilder<R>().wrap(this, innerPolicy)

fun <R> IAsyncPolicy<R>.wrap(innerPolicy: IAsyncPolicy<R>): AsyncWrapPolicy<R> =
  AsyncWrapPolicyBuilder<R>().wrap(this, innerPolicy)
