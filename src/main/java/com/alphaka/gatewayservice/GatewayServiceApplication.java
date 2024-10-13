package com.alphaka.gatewayservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import reactivefeign.spring.config.EnableReactiveFeignClients;

@SpringBootApplication
@EnableDiscoveryClient
@EnableReactiveFeignClients
public class GatewayServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(GatewayServiceApplication.class, args);
    }

}
