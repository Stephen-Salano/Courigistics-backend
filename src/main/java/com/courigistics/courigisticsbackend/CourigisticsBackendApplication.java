package com.courigistics.courigisticsbackend;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync
@SpringBootApplication

public class CourigisticsBackendApplication {
    private  static final Logger logger = LoggerFactory.getLogger(CourigisticsBackendApplication.class);

    public static void main(String[] args)
    {
        SpringApplication app =  new SpringApplication(CourigisticsBackendApplication.class);
        Environment environment = app.run(args).getEnvironment();

        String activeProfiles = String.join(", ", environment.getActiveProfiles());

    }

}
