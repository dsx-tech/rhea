package uk.dsxt.rhea;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertEquals;

class YamlConfigSourceJavaTest {
    private ReactiveConfig config;

    @BeforeEach
    void init() {
        YamlConfigSource yamlSource = new YamlConfigSource(
                Paths.get("src" + File.separator + "test" + File.separator + "resources"),
                "yamlSource.yml"
        );

        config = new ReactiveConfig.Builder()
                .addSource("yamlConfig", yamlSource)
                .build();
    }

    @Test
    void stringTypeTest() throws InterruptedException {
        Reloadable<String> job = config.get("job", PropertyTypesKt.stringType);
        Thread.sleep(10);
        assertEquals("Developer", job.get());
    }

    @Test
    void intTypeTest() throws InterruptedException {
        Reloadable<Integer> number = config.get("age", PropertyTypesKt.intType);
        Thread.sleep(10);
        assertEquals(27, number.get());
    }
}
