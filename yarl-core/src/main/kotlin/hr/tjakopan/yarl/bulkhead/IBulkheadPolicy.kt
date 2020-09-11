package hr.tjakopan.yarl.bulkhead

import hr.tjakopan.yarl.IPolicy

interface IBulkheadPolicy : IPolicy {
  val bulkheadAvailableCount: Int
  val queueAvailableCount: Int
}
