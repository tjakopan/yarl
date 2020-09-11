package hr.tjakopan.yarl.bulkhead

import hr.tjakopan.yarl.ExecutionRejectedException

class BulkheadRejectedException : ExecutionRejectedException {
  constructor(message: String, cause: Throwable) : super(message, cause)
  constructor(message: String) : super(message)
  constructor(cause: Throwable) : super(cause)
  constructor() : this("The bulkhead semaphore and queue are full and execution was rejected.")
  constructor(message: String?, cause: Throwable?, enableSuppression: Boolean, writableStackTrace: Boolean) : super(
    message,
    cause,
    enableSuppression,
    writableStackTrace
  )
}
