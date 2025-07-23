package com.petlogue.duopetbackend.common.response;

public class ResponseUtil {

    public static <T> ApiResponse<T> success(String message, T data) {
        return ApiResponse.success(message, data);
    }

    // 필요하면 실패 응답도 추가 가능
    public static <T> ApiResponse<T> error(String message) {
        return new ApiResponse<>(message, null);
    }
}
