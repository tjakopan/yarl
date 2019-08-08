package hr.tjakopan.yarl

/**
 * A marker interface identifying policies of all types and containing properties common to all policies.
 */
interface IsPolicy {
  /**
   * A key intended to be unique to each policy instance, which is passed with executions as [Context.policyKey]
   * property.
   */
  val policyKey: String
}
