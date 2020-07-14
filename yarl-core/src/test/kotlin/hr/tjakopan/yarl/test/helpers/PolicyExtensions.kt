package hr.tjakopan.yarl.test.helpers

import hr.tjakopan.yarl.Policy

internal fun <R> Policy<R, *>.raiseResults(vararg resultsToRaise: R): R {
  val iterator = resultsToRaise.iterator()
  return this.execute {
    if (!iterator.hasNext()) {
      throw IllegalArgumentException("Not enough values in resultsToRaise.")
    }
    return@execute iterator.next();
  }
}
