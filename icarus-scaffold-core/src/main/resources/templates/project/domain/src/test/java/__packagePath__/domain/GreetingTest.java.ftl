package ${packageName}.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

class GreetingTest {

    @Test
    void rejectsBlankSubject() {
        assertThrows(IllegalArgumentException.class, () -> new Greeting(" ", "hello"));
    }
}
