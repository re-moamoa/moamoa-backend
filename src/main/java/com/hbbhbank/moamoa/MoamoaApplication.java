package com.hbbhbank.moamoa;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class MoamoaApplication {

  public static void main(String[] args) {
    SpringApplication.run(MoamoaApplication.class, args);
  }

}
