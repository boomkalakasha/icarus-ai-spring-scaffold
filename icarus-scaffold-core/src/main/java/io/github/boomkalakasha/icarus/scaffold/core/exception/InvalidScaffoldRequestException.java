package io.github.boomkalakasha.icarus.scaffold.core.exception;

/** Raised when a scaffold request does not satisfy the public input contract. */
public final class InvalidScaffoldRequestException extends IllegalArgumentException {

    public InvalidScaffoldRequestException(String message) {
        super(message);
    }
}
