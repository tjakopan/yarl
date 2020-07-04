package hr.tjakopan.yarl.test.helpers

import hr.tjakopan.yarl.Policy

internal fun <R> Policy<R, *>.raiseResults(vararg resultsToRaise: R) {
  for (result in resultsToRaise) {
    this.execute { result }
  }
}
