package ${packageName}.application;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GreetingServiceTest {

    @Test
    void usesWorldWhenSubjectIsMissing() {
        assertEquals("world", new GreetingService().greet(" ").subject());
    }
}
