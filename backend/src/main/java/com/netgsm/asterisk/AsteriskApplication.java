package com.netgsm.asterisk;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class AsteriskApplication {

    public static void main(String[] args) {
        SpringApplication.run(AsteriskApplication.class, args);
    }

}
