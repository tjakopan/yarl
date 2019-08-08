package hr.tjakopan.yarl

abstract class PolicyBase<TResult> {
  companion object {
    @JvmStatic
    val DEFAULT_CONTINUE_ON_CAPTURED_CONTEXT = false
  }

  var policyKey : String? = null
}
