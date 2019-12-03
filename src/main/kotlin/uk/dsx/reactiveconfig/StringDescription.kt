package uk.dsx.reactiveconfig

import uk.dsx.reactiveconfig.interfaces.PropertyDescription

class StringDescription(override val key: String) :
    PropertyDescription {
    override val type: Any
        get() {
            return String
        }
}