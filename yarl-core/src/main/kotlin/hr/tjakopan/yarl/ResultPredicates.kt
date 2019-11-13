package hr.tjakopan.yarl

import arrow.core.Option

typealias ResultPredicate<R> = (result: Option<R>) -> Boolean
typealias ResultPredicates<R> = List<ResultPredicate<R>>

fun <R> noResultPredicates(): ResultPredicates<R> = listOf()

fun <R> ResultPredicates<R>.anyMatch(result: Option<R>): Boolean = this.any { it(result) }
