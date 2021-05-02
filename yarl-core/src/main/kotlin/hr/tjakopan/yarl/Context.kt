package hr.tjakopan.yarl

import java.util.*

data class Context internal constructor(
  val policyWrapKey: String? = null,
  val policyKey: String? = null,
  val operationKey: String? = null,
  val contextData: MutableMap<String, Any> = mutableMapOf(),
  private val _correlationId: Lazy<UUID>
) {
  constructor(
    policyWrapKey: String? = null,
    policyKey: String? = null,
    operationKey: String? = null,
    contextData: MutableMap<String, Any> = mutableMapOf()
  ) : this(policyWrapKey, policyKey, operationKey, contextData, lazy { UUID.randomUUID() })

  constructor() : this(null, null, null)

  val correlationId: UUID
    get() = _correlationId.value

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

  companion object {
    @JvmSynthetic
    internal val NONE = Context()
      @JvmSynthetic get

    @JvmStatic
    fun builder(): Builder = Builder()
  }
}
