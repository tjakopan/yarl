package hr.tjakopan.yarl

/**
 * Exception thrown when a policy rejects execution of a delegate.
 *
 * More specific exception which derive from this type are generally thrown.
 *
 * @constructor Initializes a new instance of the [ExecutionRejectedException] class.
 * @param message The message that describes the error.
 * @param cause The cause of the exception.
 *
 * @property message The message that describes the error.
 * @property cause The cause of the exception.
 */
class ExecutionRejectedException(message: String?, cause: Throwable?) : Throwable(message, cause) {
  /**
   * Initializes a new instance of the [ExecutionRejectedException] class.
   * @param message The message that describes the error.
   */
  constructor(message: String?) : this(message, null)

  /**
   * Initializes a new instance of the [ExecutionRejectedException] class.
   * @param cause The cause of the exception.
   */
  constructor(cause: Throwable?) : this(null, cause)

  /**
   * Initializes a new instance of the [ExecutionRejectedException] class.
   */
  constructor() : this(null, null)
}
