package hr.tjakopan.yarl

import java.util.stream.Stream

/**
 * A predicate that can be run against a passed [Throwable].
 *
 * @param ex The passed exception, against which to evaluate the predicate.
 * @return A matched [Throwable] or null if an exception was not matched. May return the passed exception [ex],
 * indicating that it matched the predicate. May also return cause of the passed exception [ex] to indicate that the
 * return cause matched the predicate.
 */
typealias ExceptionPredicate = (ex: Throwable) -> Throwable?

/**
 * A collection of predicates used to define whether a policy handles a given [Throwable].
 */
class ExceptionPredicates(private val predicates: MutableList<ExceptionPredicate> = mutableListOf()) {
  companion object {
    /**
     * Specifies that no exception handling filters are applied or are required.
     */
    @JvmField
    val NONE = ExceptionPredicates()
  }

  @JvmSynthetic
  internal fun add(predicate: ExceptionPredicate) {
    predicates += predicate
  }

  /**
   * Assess whether the passed [Throwable], [ex], matches any of the predicates.
   *
   * It the .handleCause() method was used when configuring the policy, predicates may test whether any causes of [ex]
   * match and may return a matching cause.
   *
   * @param ex The exception to assess against the predicates.
   * @return The first exception to match a predicate; or null, if no match is found.
   */
  fun firstMatchOrNull(ex: Throwable): Throwable? {
    return predicates.map { it(ex) }
      .firstOrNull { it != null }
  }
}

@JvmSynthetic
internal fun getExceptionType(exceptionPredicates: ExceptionPredicates, exception: Throwable): ExceptionType {
  return when (exceptionPredicates.firstMatchOrNull(exception) != null) {
    true -> ExceptionType.HANDLED_BY_THIS_POLICY
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
