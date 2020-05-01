import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.dsx.reactiveconfig.*;

import static org.junit.jupiter.api.Assertions.*;

public class PropertyTypeTest {
    private ReactiveConfig config;
    private ConfigMock source;

    @BeforeEach
    void init(){
        source = new ConfigMock();
        config = new ReactiveConfig.Builder()
                .addSource("configMock", source)
                .build();
    }

    @Test
    void stringNullableTypeTest() throws InterruptedException {
        source.addToMap("nullableStringProperty", null);
        Thread.sleep(10);
        Reloadable nullableStringProperty = config.get("nullableStringProperty", PropertyTypesKt.nullable(PropertyTypesKt.stringType));
        assertNull(nullableStringProperty.get());
    }

    @Test
    void accessingToThePropertyTwiceTest() {
        source.addToMap("property", "something");

        Reloadable property1 = config.get("property", PropertyTypesKt.stringType);
        Reloadable property2 = config.get("property", PropertyTypesKt.stringType);

        assertEquals(property1, property2);
        assertEquals("something", property1.get());
        assertEquals("something", property2.get());
    }

    @Test
    void stringTypeTest() throws InterruptedException {
        source.addToMap("stringProperty", "something1");
        Reloadable<String> stringProperty = config.get("stringProperty", PropertyTypesKt.stringType);

        source.pushChanges("stringProperty", "something2");
        Thread.sleep(10);

        assertEquals("something2", stringProperty.get());
    }

    @Test
    void intTypeTest(){
        source.addToMap("intProperty", 1313);
        Reloadable<Integer> intProperty = config.get("intProperty", PropertyTypesKt.intType);

        assertEquals(1314, intProperty.get() + 1);
    }

    @Test
    void longTypeTest(){
        source.addToMap("longProperty", 1212L);
        Reloadable<Long> longProperty = config.get("longProperty", PropertyTypesKt.longType);

        assertEquals(1212L, longProperty.get());
    }

    @Test
    void floatTypeTest(){
        source.addToMap("floatProperty", 1414F);
        Reloadable<Float>floatProperty = config.get("floatProperty", PropertyTypesKt.floatType);

        assertEquals(1414F, floatProperty.get());
    }

    @Test
    void doubleTypeTest() throws InterruptedException {
        source.addToMap("doubleProperty", 1515.0);
        Reloadable<Double>doubleProperty = config.get("doubleProperty", PropertyTypesKt.doubleType);

        assertEquals(1515.0, doubleProperty.get());
    }

    @Test
    void booleanTypeTest(){
        source.addToMap("booleanProperty", true);
        Reloadable<Boolean> booleanProperty = config.get("booleanProperty", PropertyTypesKt.booleanType);

        assertTrue(booleanProperty.get());
    }
}
