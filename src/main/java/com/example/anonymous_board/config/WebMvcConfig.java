package com.example.anonymous_board.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.http.CacheControl;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Paths;

@Configuration
// WebMvcConfigurer 인터페이스 구현
public class WebMvcConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 프로젝트 루트의 절대 경로 계산
        String projectRoot = Paths.get("").toAbsolutePath().toString();
        String uploadsProfilesPath = "file:" + projectRoot + "/uploads/profiles/";
        String uploadsPostsPath = "file:" + projectRoot + "/uploads/posts/";

        // /profiles/** 요청을 uploads/profiles/ 디렉토리 및 classpath의 정적 리소스로 매핑
        // classpath를 먼저 확인하고, 그 다음 uploads 폴더를 확인하도록 순서 변경
        registry.addResourceHandler("/profiles/**")
                .addResourceLocations("classpath:/static/profiles/", uploadsProfilesPath)
                .setCacheControl(CacheControl.noCache());

        // /posts/images/** 요청을 uploads/posts/ 디렉토리로 매핑
        registry.addResourceHandler("/posts/images/**")
                .addResourceLocations(uploadsPostsPath);
    }
}
