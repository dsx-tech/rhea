# Rhea
**Rhea** is a type-safe dynamic configuration library for JVM  applications. This library uses *asynchronous data flows* that gives a natural auto-reloading feature in runtime, so it allows to change configuration properties and get the freshest values in your application without the need to restart.

## Features
* Open source project under the Apache-2.0 License
* Extendable with user-defined property types
* Extendable with user-defined configuration sources
* Suppotrs .yaml, .properties, .json out of the box 

## Getting Started

### Setting up dependency
To get started, add `uk.dsxt:rhea:<version>` as a dependency:

#### Gradle
```groovy
dependencies {
  compile group: "uk.dsxt", name:"rhea", version: "1.0-SNAPSHOT"
}
```

#### Maven
```xml
<dependencies>
  <dependency>
    <groupId>uk.dsxt</groupId>
    <artifactId>rhea</artifactId>
    <version>1.0-SNAPSHOT</version>
  </dependency>
</dependencies>
```
## Usage

### Kotlin
1. Build a configuration object that holds properties, add configuration sources 

    ```kotlin
     val jsonSource: ConfigSource =
        JsonConfigSource(Paths.get("src" + File.separator + "test" + File.separator + "resources"), "jsonSource.json")
        
    val config: ReactiveConfig = ReactiveConfig.Builder()
        .addSource("jsonConfig", jsonSource)
        .build()
    ```

2. Create reloadable property or declare object that extends PropertyGroup to define hierarchies of properties

    ```kotlin
    val isSomethingOn: Realoadable<Boolean> = config["flag", booleanType]
    if (isSomethingOn.get()) {...}
    
    object server : PropertyGroup() {
        val name by stringType
        val port by intType
    }
    val server = Server(config[server.port].get(), config[server.name].get()).start()
    ```

3. Also, you can add some complex logic that will execute every time the property is changed

    ```kotlin
    val port: Reloadable<Int> = config[server.port]
    
    port.onChange() {
        server = server(it, config[server.name].get())
        server.start()
        }
    ```
## Extending
### Property Types
To add a custom property type, you should create an instance of PropertyType class and provide it with default property value and parse function.

### Configuration Sources
To support a new configuration source, you should implement ConfigSource interface.

## Contributors
* **Dmitry Vologin** - [GitHub account](https://github.com/vologin-dmitry)
* **Alexandra Osipova** - [GitHub account](https://github.com/FemiLame)
* **Anton Plotnikov** - [GitHub account](https://github.com/pltanton)
* **Philipp Dolgolev** - [GitHub account](https://github.com/phil-dolgolev)

## License
This project is licensed under the Apache-2.0 License. The full text could be found in [LICENSE.md](https://github.com/dsx-tech/rhea/blob/master/LICENSE).

## Acknowledgments
* Inspired by Tinkoff ReactiveConfig and [Konfig](https://github.com/npryce/konfig)

## Notes
* [Rhea](https://en.wikipedia.org/wiki/Rhea_(mythology)) is one of the Titans in Greek mythology, the mother of the first generation of the Olympian gods
* Rhea's name is believed to be derived from the word ῥέω (rheo), meaning “flow” or “ease”