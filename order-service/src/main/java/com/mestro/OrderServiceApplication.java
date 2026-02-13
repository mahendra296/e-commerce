package com.mestro;

import com.mestro.common.client.ProductServiceClient;
import com.mestro.common.config.CommonAutoConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Import;

@SpringBootApplication
@Import(CommonAutoConfiguration.class)
@EnableFeignClients(clients = {ProductServiceClient.class})
public class OrderServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(OrderServiceApplication.class, args);
    }
}
