package uk.dsx.reactiveconfig;

import org.junit.jupiter.api.*;


public class ReactiveConfigJavaTest {
    private ConfigMock source;
    private ReactiveConfig config;

    @BeforeEach
    void creation() {
        source = new ConfigMock();

        config = new ReactiveConfig.Builder()
                .addSource("configMock", source)
                .build();
        source.addToMap("property", 3);
    }

    @Test
    void testing() {
        var d = PropertyTypesKt.intType;
        var prop = config.dawd("property", PropertyTypesKt.intType)
    }

}
