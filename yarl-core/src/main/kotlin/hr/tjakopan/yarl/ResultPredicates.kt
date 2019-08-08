package hr.tjakopan.yarl

/**
 * A collection of predicates used to define whether a policy handles a given TResult value.
 */
class ResultPredicates<TResult>(private val predicates: MutableList<ResultPredicate<TResult>>) {
  companion object {
    /**
     * Specifies that no result-handling filters are applied or are required.
     */
    @JvmStatic
    val NONE = ResultPredicates<Nothing>()
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
  fun anyMatch(result: TResult) = predicates.any { it(result) }
}
