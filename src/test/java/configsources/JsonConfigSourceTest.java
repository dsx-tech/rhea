package configsources;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.dsx.reactiveconfig.PropertyTypesKt;
import uk.dsx.reactiveconfig.ReactiveConfig;
import uk.dsx.reactiveconfig.Reloadable;
import uk.dsx.reactiveconfig.configsources.JsonConfigSource;
import uk.dsx.reactiveconfig.interfaces.ConfigSource;

import java.io.File;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class JsonConfigSourceTest {
    private ConfigSource jsonSource;
    private ReactiveConfig config;
    @BeforeEach
    void init(){
        jsonSource =
                new JsonConfigSource(Paths.get("src" + File.separator + "test" + File.separator + "resources"), "jsonSource.json");

        config = new ReactiveConfig.Builder()
                .addSource("jsonConfig", jsonSource)
                .build();
    }

    @Test
    void booleanTypeTest(){
        Reloadable<Boolean> isSomethingOn = config.get("isSomethingOn", PropertyTypesKt.booleanType);
        assertTrue(isSomethingOn.get());
    }

    @Test
    void stringTypeTest(){
        Reloadable<String> property = config.get("property", PropertyTypesKt.stringType);
        assertEquals("someInfo", property.get());
    }
}