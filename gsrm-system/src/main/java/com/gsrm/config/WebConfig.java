package com.gsrm.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC 配置類別.
 *
 * <p>設定跨域資源共用（CORS），允許前端 React 應用程式存取後端 API。</p>
 *
 * @author Jeff
 * @since 2026-03-06
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    /**
     * 設定 CORS 規則.
     *
     * <p>允許來自 {@code http://localhost:3000}（React dev server）的請求，
     * 支援所有常用 HTTP 方法與標頭。</p>
     *
     * @param registry CORS 規則登錄器
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins(
                        "http://localhost:3000",
                        "http://localhost:5173",
                        "http://localhost:8080"
                )
                .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }
}
