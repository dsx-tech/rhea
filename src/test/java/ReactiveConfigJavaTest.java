import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import uk.dsx.reactiveconfig.ConfigMock;
import uk.dsx.reactiveconfig.PropertyTypesKt;
import uk.dsx.reactiveconfig.ReactiveConfig;
import uk.dsx.reactiveconfig.Reloadable;

import static org.junit.jupiter.api.Assertions.*;

class ReactiveConfigJavaTest {
    private ReactiveConfig config;
    private ConfigMock source;

    @BeforeEach
    void init(){
        source = new ConfigMock();
        config = new ReactiveConfig.Builder()
                .addSource("configMock", source)
                .build();
        source.addToMap("property", 3);
    }

    @Test
    void rightTypeTest() throws InterruptedException {
        Reloadable<Integer> property = config.get("property", PropertyTypesKt.intType);
        Thread.sleep(20);
        assertEquals(10, property.get() + 7);
    }

    @Test
    void wrongKeyTest() {
        assertNull(config.get("propp", PropertyTypesKt.intType));
    }

    @Test
    void booleanTest() throws InterruptedException {
        source.addToMap("prop1", true);
        Reloadable<Boolean> prop1 = config.get("prop1", PropertyTypesKt.booleanType);
        Thread.sleep(20);
        assertTrue(prop1.get());
    }
}