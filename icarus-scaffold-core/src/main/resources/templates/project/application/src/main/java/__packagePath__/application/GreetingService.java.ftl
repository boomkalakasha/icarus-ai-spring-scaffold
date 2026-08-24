package ${packageName}.application;

import ${packageName}.domain.Greeting;

public final class GreetingService implements GreetingUseCase {

    @Override
    public Greeting greet(String subject) {
        String normalized = subject == null || subject.isBlank() ? "world" : subject.trim();
        return new Greeting(normalized, "Hello, " + normalized + "!");
    }
}
