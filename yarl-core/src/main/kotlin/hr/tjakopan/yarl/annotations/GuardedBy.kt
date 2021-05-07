package hr.tjakopan.yarl.annotations

@Target(AnnotationTarget.FIELD, AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.BINARY)
annotation class GuardedBy(val value: String)
