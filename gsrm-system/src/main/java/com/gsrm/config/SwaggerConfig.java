package com.gsrm.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Swagger / OpenAPI 3 配置類別.
 *
 * <p>設定 API 文件的基本資訊、聯絡方式與 JWT Bearer Token 安全方案。
 * 啟動後可透過 {@code /swagger-ui.html} 存取互動式 API 文件。</p>
 *
 * @author Jeff
 * @since 2026-03-06
 */
@Configuration
public class SwaggerConfig {

    /** JWT Bearer Token 安全方案名稱. */
    private static final String SECURITY_SCHEME_NAME = "bearerAuth";

    /**
     * 建立 OpenAPI 文件定義.
     *
     * @return OpenAPI 物件
     */
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(buildApiInfo())
                .addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME_NAME))
                .components(new Components()
                        .addSecuritySchemes(SECURITY_SCHEME_NAME,
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .name(SECURITY_SCHEME_NAME)
                                        .description("輸入 JWT Token（無需加 Bearer 前綴）")));
    }

    /**
     * 建立 API 基本資訊.
     *
     * @return Info 物件
     */
    private Info buildApiInfo() {
        return new Info()
                .title("地面站資源管理系統 API")
                .description("Ground Station Resource Management System (GSRM) RESTful API\n\n"
                        + "提供地面站排程管理、衛星需求匯入/匯出、甘特圖視覺化等功能。")
                .version("1.0.0")
                .contact(new Contact()
                        .name("Jeff")
                        .email("jeff@gsrm.example.com"))
                .license(new License()
                        .name("Internal Use Only"));
    }
}
