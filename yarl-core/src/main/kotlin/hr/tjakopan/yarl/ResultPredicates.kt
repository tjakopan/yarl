package hr.tjakopan.yarl

import hr.tjakopan.yarl.annotations.Immutable

typealias ResultPredicate<R> = (result: R) -> Boolean

@Immutable
class ResultPredicates<R> private constructor(private val predicates: List<ResultPredicate<R>>) {
  constructor() : this(listOf())

  @JvmSynthetic
  internal operator fun plus(predicate: ResultPredicate<R>): ResultPredicates<R> {
    val predicates = this.predicates + predicate
    return ResultPredicates(predicates)
  }

  fun anyMatch(result: R): Boolean = this.predicates.any { it(result) }

  companion object {
    @JvmField
    val NONE = ResultPredicates<Any>()
  }
}
