package ${packageName}.infrastructure;

import ${packageName}.domain.Greeting;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class InMemoryGreetingRepositoryTest {

    @Test
    void storesAndReadsAValue() {
        InMemoryGreetingRepository repository = new InMemoryGreetingRepository();
        repository.save(new Greeting("team", "Hello, team!"));
        assertTrue(repository.findBySubject("team").isPresent());
    }
}
