package hr.tjakopan.yarl

import arrow.core.Option
import arrow.core.Some
import arrow.core.extensions.sequence.foldable.firstOption
import arrow.core.firstOrNone
import arrow.core.getOrElse

typealias ExceptionPredicate = (ex: Throwable) -> Option<Throwable>
typealias ExceptionPredicates = List<ExceptionPredicate>

@JvmField
val NO_EXCEPTION_PREDICATES: ExceptionPredicates = listOf();

fun ExceptionPredicates.firstMatchOrNone(ex: Throwable): Option<Throwable> {
  return map { it(ex) }
    .firstOrNone { it.isDefined() }
    .getOrElse { Option.empty() }
}

@JvmSynthetic
internal fun ExceptionPredicates.getExceptionType(exception: Throwable): ExceptionType {
  return when (firstMatchOrNone(exception)) {
    is Some -> ExceptionType.HANDLED_BY_THIS_POLICY
    else -> ExceptionType.UNHANDLED
  }
}

@JvmSynthetic
internal fun handleCause(predicate: (Throwable) -> Boolean): ExceptionPredicate {
  return fun(exception: Throwable): Option<Throwable> {
    return generateSequence(exception, Throwable::cause)
      .firstOption(predicate)
  }
}
