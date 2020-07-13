package hr.tjakopan.yarl

typealias ResultPredicate<R> = (result: R) -> Boolean

class ResultPredicates<R> private constructor(private val predicates: List<ResultPredicate<R>>) {
  companion object {
    @JvmField
    val NONE = ResultPredicates<Any>()
  }

  constructor() : this(listOf())

  @JvmSynthetic
  internal operator fun plus(predicate: ResultPredicate<R>): ResultPredicates<R> {
    val predicates = this.predicates + predicate
    return ResultPredicates(predicates)
  }

  fun anyMatch(result: R): Boolean = this.predicates.any { it(result) }
}
