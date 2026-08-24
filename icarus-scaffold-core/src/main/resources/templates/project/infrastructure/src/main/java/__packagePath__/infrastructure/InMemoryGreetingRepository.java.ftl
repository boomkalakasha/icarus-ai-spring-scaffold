package ${packageName}.infrastructure;

import ${packageName}.domain.Greeting;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/** Replace this adapter with a persistence implementation when the service needs one. */
public final class InMemoryGreetingRepository {

    private final ConcurrentMap<String, Greeting> values = new ConcurrentHashMap<>();

    public void save(Greeting greeting) {
        values.put(greeting.subject(), greeting);
    }

    public Optional<Greeting> findBySubject(String subject) {
        return Optional.ofNullable(values.get(subject));
    }
}
