package com.community.dogcat.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.http.MediaType;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;

// 가독성을 위해 어노테이션 사용자 정의
// UploadController에 넣을 swagger multipart 파라미터

@Target({ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Parameter(
	content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE)
)
public @interface MultipartParam {
}
