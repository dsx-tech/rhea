package uk.dsx.reactiveconfig


open class ReactiveConfig(block: ReactiveConfig.() -> Unit) {
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

    class PropertyGroup : ReactiveConfig()
    {
        
    }
    operator fun get(key : String) = manager.properties[key]
}
 