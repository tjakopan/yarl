package hr.tjakopan.yarl

sealed class ExecutionRejectedException : Throwable {
  constructor(message: String, cause: Throwable) : super(message, cause)
  constructor(message: String) : super(message)
  constructor(cause: Throwable) : super(cause)
  constructor() : super()
}
