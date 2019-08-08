package hr.tjakopan.yarl.utilities

import java.util.*

internal object KeyHelper {
  @JvmSynthetic
  internal fun guidPart(): String = UUID.randomUUID().toString().substring(0, 8)
}
