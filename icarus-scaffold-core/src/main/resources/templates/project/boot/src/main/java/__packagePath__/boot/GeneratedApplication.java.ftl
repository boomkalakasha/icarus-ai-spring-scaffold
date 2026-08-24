package ${packageName}.boot;

import ${packageName}.application.GreetingService;
import ${packageName}.application.GreetingUseCase;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication(scanBasePackages = "${packageName}")
public class GeneratedApplication {

    public static void main(String[] args) {
        SpringApplication.run(GeneratedApplication.class, args);
    }

    @Bean
    GreetingUseCase greetingUseCase() {
        return new GreetingService();
    }
}
