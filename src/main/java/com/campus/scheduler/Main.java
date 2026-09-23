package com.campus.scheduler;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Main {
    public static void main(String[] args) {
        if (args.length > 0 && "--cli".equalsIgnoreCase(args[0])) {
            new com.campus.scheduler.cli.MainCLI().start();
        } else {
            SpringApplication.run(Main.class, args);
        }
    }
}
