package com.project.global.response;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record CommonResponse<T>(
        boolean success,
        T data
) {

    public static <T> CommonResponse<T> ok(T data) {
        return new CommonResponse<>(true, data);
    }

    public static CommonResponse<Void> ok() {
        return new CommonResponse<>(true, null);
    }
}
