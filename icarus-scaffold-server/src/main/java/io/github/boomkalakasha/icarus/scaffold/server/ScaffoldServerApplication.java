package io.github.boomkalakasha.icarus.scaffold.server;

import io.github.boomkalakasha.icarus.scaffold.core.ScaffoldGenerator;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class ScaffoldServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(ScaffoldServerApplication.class, args);
    }

    @Bean
    ScaffoldGenerator scaffoldGenerator() {
        return new ScaffoldGenerator();
    }

}
