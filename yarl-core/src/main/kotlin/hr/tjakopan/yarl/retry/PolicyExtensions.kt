package hr.tjakopan.yarl.retry

import hr.tjakopan.yarl.Policy

fun <R> Policy.Policy.retry(): RetryPolicyBuilder<R> = RetryPolicyBuilder()

fun <R> Policy.Policy.asyncRetry(): AsyncRetryPolicyBuilder<R> = AsyncRetryPolicyBuilder()
