import org.junit.jupiter.api.Test;
import uk.dsx.reactiveconfig.ConfigManager;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class ConfigManagerJavaTest {
    @Test
    void notNullTest(){
        ConfigManager manager = new ConfigManager();
        assertNotNull(manager);
    }
}
