package uk.dsx.reactiveconfig


class ReactiveConfig(block: ReactiveConfig.() -> Unit) {
    init {
        apply(block)
    }

    val manager = ConfigManager

    infix fun <T : Any> String.of(type: PropertyType<T>) {
        ReloadableFactory.createReloadable(this, type)
    }
    companion object {
        fun <T : Any> reloadable(key: String, type: PropertyType<T>): Reloadable<T> {
            return ReloadableFactory.createReloadable(key, type)
        }
    }
}
 