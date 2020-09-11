package hr.tjakopan.yarl.bulkhead

import hr.tjakopan.yarl.Policy

fun <R> Policy.Policy.bulkhead(): BulkheadPolicyBuilder<R> = BulkheadPolicyBuilder()

fun <R> Policy.Policy.asyncBulkhead(): AsyncBulkheadPolicyBuilder<R> = AsyncBulkheadPolicyBuilder()
