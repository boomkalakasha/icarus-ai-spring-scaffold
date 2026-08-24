package ${packageName}.domain;

/** A domain value with a small invariant at the edge of the model. */
public record Greeting(String subject, String message) {

    public Greeting {
        if (subject == null || subject.isBlank()) {
            throw new IllegalArgumentException("subject must not be blank");
        }
        if (message == null || message.isBlank()) {
            throw new IllegalArgumentException("message must not be blank");
        }
    }
}
