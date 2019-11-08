package hr.tjakopan.yarl

import arrow.core.Option
import arrow.core.Some
import java.util.stream.Stream

typealias ExceptionPredicate = (ex: Throwable) -> Option<Throwable>

data class ExceptionPredicates(private val predicates: List<ExceptionPredicate> = listOf()) :
  List<ExceptionPredicate> by predicates {
  companion object {
    @JvmField
    val NONE = ExceptionPredicates()
  }

  fun firstMatch(ex: Throwable): Option<Throwable> {
    return predicates.map { it(ex) }
      .first()
  }
}

@JvmSynthetic
internal fun getExceptionType(exceptionPredicates: ExceptionPredicates, exception: Throwable): ExceptionType {
  return when (exceptionPredicates.firstMatch(exception)) {
    Some<Throwable>() -> ExceptionType.HANDLED_BY_THIS_POLICY
    false -> ExceptionType.UNHANDLED
  }
}

@JvmSynthetic
internal fun handleCause(predicate: (Throwable) -> Boolean): ExceptionPredicate {
  return fun(exception: Throwable): Throwable? {
    return Stream.iterate(exception, Throwable::cause)
      .filter { it != null }
      .filter(predicate)
      .findFirst()
      .orElse(null)
  }
}
