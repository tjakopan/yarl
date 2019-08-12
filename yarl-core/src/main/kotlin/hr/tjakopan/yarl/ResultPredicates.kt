package hr.tjakopan.yarl

/**
 * A predicate that can be run against a passed result value of type TResult. Predicates are used to define whether
 * policies handle the given result.
 *
 * @param result The passed result, against which to evaluate the predicate.
 * @param TResult The type of results which this predicate can evaluate.
 * @return True if the passed result matched the predicate; otherwise, false.
 *
 */
typealias ResultPredicate<TResult> = (result: TResult?) -> Boolean

/**
 * A collection of predicates used to define whether a policy handles a given TResult value.
 */
class ResultPredicates<TResult>(private val predicates: MutableList<ResultPredicate<TResult>> = mutableListOf()) {
  companion object {
    /**
     * Specifies that no result-handling filters are applied or are required.
     */
    @JvmStatic
    fun <TResult> none() = ResultPredicates<TResult>()
  }

  constructor() : this(mutableListOf())

  @JvmSynthetic
  internal fun add(predicate: ResultPredicate<TResult>) {
    predicates += predicate
  }

  /**
   * Returns a boolean indicating whether the passed TResult value matched any predicates.
   *
   * @param result The TResult value to assess against the predicates.
   */
  fun anyMatch(result: TResult?) = predicates.any { it(result) }
}
