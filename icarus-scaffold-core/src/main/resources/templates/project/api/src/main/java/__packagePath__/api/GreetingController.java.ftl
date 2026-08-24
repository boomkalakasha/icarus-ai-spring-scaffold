package ${packageName}.api;

import ${packageName}.application.GreetingUseCase;
import ${packageName}.domain.Greeting;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/greetings")
public final class GreetingController {

    private final GreetingUseCase useCase;

    public GreetingController(GreetingUseCase useCase) {
        this.useCase = useCase;
    }

    @GetMapping
    public Greeting greet(@RequestParam(defaultValue = "world") String subject) {
        return useCase.greet(subject);
    }
}
