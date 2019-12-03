package uk.dsx.reactiveconfig

import uk.dsx.reactiveconfig.interfaces.PropertyDescription
import uk.dsx.reactiveconfig.interfaces.RawProperty

class RawString(override val description: PropertyDescription, override val value: Any) :
    RawProperty {
}