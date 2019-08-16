package hr.tjakopan.yarl.fallback

import hr.tjakopan.yarl.Policy
import kotlin.test.Test
import kotlin.test.assertFalse

class FallbackTest {
  @Test
  fun shouldNotExecuteFallbackWhenExecutedDelegateDoesNotThrow() {
    var fallbackActionExecuted = false
    val fallbackAction = { fallbackActionExecuted = true }
    val fallbackPolicy = Policy
      .handle(ArithmeticException::class.java)
      .fallback(fallbackAction)

    fallbackPolicy.execute { }

    assertFalse(fallbackActionExecuted)
  }
}
