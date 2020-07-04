package hr.tjakopan.yarl.test.helpers

import hr.tjakopan.yarl.AsyncPolicy

internal suspend fun <R> AsyncPolicy<R, *>.raiseResults(vararg resultsToRaise: R) {
  for (result in resultsToRaise) {
    this.execute { result }
  }
}
