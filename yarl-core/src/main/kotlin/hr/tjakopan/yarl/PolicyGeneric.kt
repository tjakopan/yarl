package hr.tjakopan.yarl

open class PolicyGeneric<TResult>(
  exceptionPredicates: ExceptionPredicates?,
  resultPredicates: ResultPredicates<TResult>?
) : PolicyBaseGeneric<TResult>(exceptionPredicates, resultPredicates), ISyncPolicyGeneric<TResult> {
  protected constructor(policyBuilderGeneric: PolicyBuilderGeneric<TResult>? = null) : this(
    policyBuilderGeneric?.exceptionPredicates,
    policyBuilderGeneric?.resultPredicates
  )

  override fun withPolicyKey(policyKey: String): ISyncPolicyGeneric<TResult> {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
  }

  override fun execute(action: () -> TResult?): TResult? {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
  }

  override fun execute(contextData: Map<String, Any>, action: (Context) -> TResult?): TResult? {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
  }

  override fun execute(context: Context, action: (Context) -> TResult?): TResult? {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
  }

  override fun executeAndCapture(action: () -> TResult?): PolicyResultGeneric<TResult> {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
  }

  override fun executeAndCapture(
    contextData: Map<String, Any>,
    action: (Context) -> TResult?
  ): PolicyResultGeneric<TResult> {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
  }

  override fun executeAndCapture(context: Context, action: (Context) -> TResult?): PolicyResultGeneric<TResult> {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
  }
}
