package configsources;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.dsx.reactiveconfig.PropertyTypesKt;
import uk.dsx.reactiveconfig.ReactiveConfig;
import uk.dsx.reactiveconfig.Reloadable;
import uk.dsx.reactiveconfig.configsources.PropertiesConfigSource;
import uk.dsx.reactiveconfig.interfaces.ConfigSource;

import java.io.File;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PropertiesConfigSourceJavaTest {
    private ConfigSource propertiesSource;
    private ReactiveConfig config;

    @BeforeEach
    void init(){
        propertiesSource =
                new PropertiesConfigSource(
                        Paths.get("src" + File.separator + "test" + File.separator + "resources"),
                        "propertiesSource.properties"
                );

        config = new ReactiveConfig.Builder()
                .addSource("propertiesConfig", propertiesSource)
                .build();
    }

    @Test
    void intTypeTest(){
        Reloadable<String> name = config.get("login", PropertyTypesKt.stringType);
        assertEquals("Felix", name.get());
    }

    @Test
    void stringTypeTest(){
        Reloadable<Integer> number = config.get("password", PropertyTypesKt.intType);
        assertEquals(1234, number.get());
    }
}
