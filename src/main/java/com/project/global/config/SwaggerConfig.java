package com.project.global.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import java.util.List;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI openAPI(@Value("${app.openapi.server-url:}") String serverUrl) {
        OpenAPI api = new OpenAPI()
                .info(new Info()
                        .title("Backend API")
                        .description("한국외대 개발자 커뮤니티 백엔드 API")
                        .version("v1.0.0"))
                .components(new Components()
                        .addSecuritySchemes("csrf-token", new SecurityScheme()
                                .type(SecurityScheme.Type.APIKEY)
                                .in(SecurityScheme.In.HEADER)
                                .name("X-XSRF-TOKEN")
                                .description("브라우저 Application → Cookies → `XSRF-TOKEN` 값을 복사해 입력")))
                .addSecurityItem(new SecurityRequirement().addList("csrf-token"));
        if (StringUtils.hasText(serverUrl)) {
            api.setServers(List.of(new Server().url(serverUrl.trim()).description("API")));
        }
        return api;
    }

    @Bean
    public GroupedOpenApi allApi() {
        return GroupedOpenApi.builder()
                .group("all")
                .packagesToScan("com.project")
                .build();
    }
}
