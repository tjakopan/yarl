package hr.tjakopan.yarl

import hr.tjakopan.yarl.annotations.GuardedBy
import hr.tjakopan.yarl.annotations.ThreadSafe
import java.util.*
import java.util.concurrent.ConcurrentHashMap

@ThreadSafe
class Context private constructor(
  val operationKey: String?,
  contextData: MutableMap<String, Any>
) : MutableMap<String, Any> by contextData {
  @Volatile
  @GuardedBy("this")
  var policyWrapKey: String? = null
    internal set

  @Volatile
  @GuardedBy("this")
  var policyKey: String? = null
    internal set

  val correlationId: UUID by lazy { UUID.randomUUID() }

  companion object {
    @JvmSynthetic
    internal fun none() = invoke()

    @JvmSynthetic
    operator fun invoke(operationKey: String, contextData: Map<String, Any>) =
      Context(operationKey, ConcurrentHashMap(contextData))

    @JvmStatic
    fun of(operationKey: String, contextData: Map<String, Any>) = invoke(operationKey, contextData)

    @JvmSynthetic
    operator fun invoke(operationKey: String) = Context(operationKey, ConcurrentHashMap())

    @JvmStatic
    fun of(operationKey: String) = invoke(operationKey)

    @JvmSynthetic
    operator fun invoke(contextData: Map<String, Any>) = Context(null, ConcurrentHashMap(contextData))

    @JvmStatic
    fun of(contextData: Map<String, Any>) = invoke(contextData)

    @JvmSynthetic
    operator fun invoke() = Context(null, ConcurrentHashMap())

    @JvmStatic
    fun of() = invoke()
  }
}
