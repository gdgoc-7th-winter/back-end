package com.project.global.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * -----------------------------------------------------------------------
 * 컨트롤러 메서드 파라미터에 붙여, 세션 기반 로그인 사용자(User)를 주입받기 위한 어노테이션
 * -----------------------------------------------------------------------
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface CurrentUser {
}
