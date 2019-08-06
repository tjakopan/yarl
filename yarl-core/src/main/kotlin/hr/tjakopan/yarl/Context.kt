package hr.tjakopan.yarl

import java.util.*

/**
 * Context that carries with a single execution through a policy. Commonly used properties are directly on the class.
 * Backed by a mutable map of string key / any value pairs, to which user defined values may be added.
 *
 * Do not re-use an instance of Context across more than one call through .execute(...) or executeAsync(...).
 *
 * @constructor Initializes a new instance of the [Context] class, with the specified [operationKey] and the supplied
 * [contextData].
 * @param operationKey The operation key.
 * @param contextData The context data.
 *
 * @property operationKey A key unique to the call site of the current execution.
 *
 * Policy instances are commonly reused across multiple call sites. Set an operationKey so that logging and metrics
 * can distinguish usages of policy instances at different call sites.
 *
 * The value is set by using Context(String) constructor taking an operationKey parameter.
 */
class Context(val operationKey: String?, private val contextData: MutableMap<String, Any>) :
  MutableMap<String, Any> by contextData {
  /* For an individual execution through a policy or policywrap, it is expected that all execution steps (for example
   * executing the user delegate, invoking policy-activity delegates such as onRetry, onBreak, onTimeout etc) execute
   * sequentially. Therefore, this class is intentionally not constructed to be safe for concurrent access from
   * multiple threads.
   */

  companion object {
    internal val none = Context()
  }

  /**
   * An [UUID] guaranteed to be unique to each execution.
   *
   * Acts as a correlation id so that events specific to a single execution can be identified in logging and metrics.
   */
  val correlationId by lazy {
    UUID.randomUUID()
  }

  /**
   * When execution is through [hr.tjakopan.yarl.wrap.PolicyWrap], identifies the PolicyWrap executing the current
   * delegate by returning the [PolicyBase.policyKey] of the outermost layer in the PolicyWrap; otherwise, null.
   *
   */
  var policyWrapKey: String? = null
    internal set
  /**
   * The [PolicyBase.policyKey] of the policy instance executing the current delegate.
   */
  var policyKey: String? = null
    internal set

  /**
   * Initializes a new instance of the [Context] class, with the specified [operationKey].
   * @param operationKey The operation key.
   */
  constructor(operationKey: String) : this(operationKey, mutableMapOf()) {
  }

  /**
   * Initializes a new instance of the [Context] class.
   */
  constructor() : this(null, mutableMapOf()) {
  }
}
