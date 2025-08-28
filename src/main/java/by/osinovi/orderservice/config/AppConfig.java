package by.osinovi.orderservice.config;

import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableFeignClients(basePackages = "by.osinovi.orderservice.client")
public class AppConfig {
}