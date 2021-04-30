package hr.tjakopan.yarl.noop

import hr.tjakopan.yarl.Policy

fun <R> Policy.Policy.noOp(): NoOpPolicy<R> = NoOpPolicy(NoOpPolicyBuilder())

fun <R> Policy.Policy.asyncNoOp(): AsyncNoOpPolicy<R> = AsyncNoOpPolicy()
