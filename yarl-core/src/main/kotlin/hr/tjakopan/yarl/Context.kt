package hr.tjakopan.yarl

import arrow.core.Eval
import arrow.core.Option
import java.util.*

data class Context(
  val policyWrapKey: Option<String> = Option.empty(),
  val policyKey: Option<String> = Option.empty(),
  val operationKey: Option<String> = Option.empty(),
  val correlationId: Eval<UUID> = Eval.later { UUID.randomUUID() },
  private val contextData: Map<String, Any> = mapOf()
) : Map<String, Any> by contextData {
  internal companion object {
    @JvmSynthetic
    internal val none = Context()
      @JvmSynthetic get
  }
}
