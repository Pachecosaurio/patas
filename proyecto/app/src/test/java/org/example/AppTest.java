package org.example;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class AppTest {
    @Test 
    void appCanBeInstantiated() {
        // Test que verifica que la aplicación puede ser instanciada
        App app = new App();
        assertNotNull(app, "app should not be null");
    }
    
    @Test
    void mainMethodExists() {
        // Test que verifica que el método main existe
        try {
            App.class.getMethod("main", String[].class);
            assertTrue(true, "main method exists");
        } catch (NoSuchMethodException e) {
            fail("main method should exist");
        }
    }
}
