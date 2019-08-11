package hr.tjakopan.yarl.wrap

import hr.tjakopan.yarl.IsPolicy

/**
 * Defines properties and methods common to all PolicyWrap policies.
 */
interface IPolicyWrap : IsPolicy {
  /**
   * Returns the outer [IsPolicy] in this [IPolicyWrap].
   */
  val outer: IsPolicy

  /**
   * Returns the next inner [IsPolicy] in this [IPolicyWrap].
   */
  val inner: IsPolicy
}
