package hr.tjakopan.yarl

import java.util.*

data class Context(
  val policyWrapKey: String? = null,
  val policyKey: String? = null,
  val operationKey: String? = null,
  val contextData: MutableMap<String, Any> = mutableMapOf()
) {
  companion object {
    @JvmSynthetic
    internal val NONE = Context()
      @JvmSynthetic get

    @JvmStatic
    fun builder(): Builder = Builder()
  }

  val correlationId: UUID by lazy { UUID.randomUUID() }

  class Builder {
    var policyWrapKey: String? = null
    var policyKey: String? = null
    var operationKey: String? = null
    var contextData: MutableMap<String, Any> = mutableMapOf()

    fun policyWrapKey(policyWrapKey: String): Builder {
      this.policyWrapKey = policyWrapKey
      return this
    }

    fun policyKey(policyKey: String): Builder {
      this.policyKey = policyKey
      return this
    }

    fun operationKey(operationKey: String): Builder {
      this.operationKey = operationKey
      return this
    }

    fun contextData(contextData: MutableMap<String, Any>): Builder {
      this.contextData = contextData
      return this;
    }

    fun build(): Context = Context(policyWrapKey, policyKey, operationKey, contextData)
  }
}
