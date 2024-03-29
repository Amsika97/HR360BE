package com.maveric.hr360.config;

import org.springframework.beans.factory.annotation.Value;
        import org.springframework.context.annotation.Bean;
        import org.springframework.context.annotation.Configuration;
        import org.springframework.web.servlet.config.annotation.CorsRegistry;
        import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class Configurations {

    @Value("${cors.localhost.endpoint}")
    public String corsEndPointForLocalHost;
    @Value("${cors.port}")
    private String port;

    @Value("${cors.endpoint}")
    private String corsEndPoint;

    @Bean
    public WebMvcConfigurer webMvcConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**")
//.allowedOrigins(corsEndPoint.concat(port),corsEndPointForLocalHost.concat(port))
                        //.allowedOrigins("http://172.16.238.24","http://172.16.238.22","http://localhost:4201")
                        .allowedOrigins("*")
                        .allowedMethods("GET", "POST", "PUT", "DELETE")
                        .allowedHeaders("Origin", "Content-Type", "Accept","Access-Control-Allow-Origin","Content-Disposition")
                        .exposedHeaders("Origin", "Content-Type", "Accept","Access-Control-Allow-Origin","Content-Disposition");
                       //  .allowCredentials(true);
            }
        };

    }

}
