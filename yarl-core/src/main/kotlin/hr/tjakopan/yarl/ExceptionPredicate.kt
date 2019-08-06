package hr.tjakopan.yarl

/**
 * A predicate that can be run against a passed [Throwable].
 */
interface ExceptionPredicate {
  /**
   * @param ex The passed exception, against which to evaluate the predicate.
   * @return A matched [Throwable] or null if an exception was not matched. May return the passed exception [ex],
   * indicating that it matched the predicate. May also return cause of the passed exception [ex] to indicate that the
   * return cause matched the predicate.
   */
  fun test(ex: Throwable): Throwable?;
}
