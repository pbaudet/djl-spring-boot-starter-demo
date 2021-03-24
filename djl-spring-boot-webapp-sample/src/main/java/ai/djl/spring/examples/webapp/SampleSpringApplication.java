package ai.djl.spring.examples.webapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.AutoConfigurationPackage;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@AutoConfigurationPackage(basePackages = "conf")
public class SampleSpringApplication {
    public static void main(String[] args) {
        SpringApplication.run(SampleSpringApplication.class, args);
    }}
