package com.maveric.hr360;

import org.springframework.boot.*;
import org.springframework.boot.autoconfigure.*;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.web.bind.annotation.*;


@SpringBootApplication
@EnableFeignClients
public class HR360Application {

	public static void main(String[] args) {
		SpringApplication.run(HR360Application.class, args);
	}
}
