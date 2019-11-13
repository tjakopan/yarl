package hr.tjakopan.yarl

import arrow.core.Either

typealias DelegateResult<R> = Either<Throwable, R>
