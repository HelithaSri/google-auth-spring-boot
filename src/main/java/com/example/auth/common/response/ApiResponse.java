package com.example.auth.common.response;


import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    private boolean success;
    private String message;
    private T data;
    private List<FieldError> errors;
    private Meta meta;

    // ─── Success (single object) ───────────────────────────
    public static <T> ApiResponse<T> success(T data, String message) {
        return ApiResponse.<T>builder()
                .success(true)
                .message(message)
                .data(data)
                .build();
    }

    public static <T> ApiResponse<T> success(T data) {
        return ApiResponse.<T>builder()
                .success(true)
                .message("Success")
                .data(data)
                .build();
    }

    // ─── Success (paginated list) ──────────────────────────
    public static <T> ApiResponse<T> success(T data, String message, Meta meta) {
        return ApiResponse.<T>builder()
                .success(true)
                .message(message)
                .data(data)
                .meta(meta)
                .build();
    }

    // ─── Error (simple message) ────────────────────────────
    public static <T> ApiResponse<T> error(String message) {
        return ApiResponse.<T>builder()
                .success(false)
                .message(message)
                .build();
    }

    // ─── Error (with field validation errors) ─────────────
    public static <T> ApiResponse<T> error(String message, List<FieldError> errors) {
        return ApiResponse.<T>builder()
                .success(false)
                .message(message)
                .errors(errors)
                .build();
    }

    // ─── Nested classes ────────────────────────────────────
    @Getter
    @AllArgsConstructor
    public static class FieldError {
        private String field;
        private String message;
    }

    @Getter
    @AllArgsConstructor
    @Builder
    public static class Meta {
        private int page;
        private int size;
        private long total;
        private int totalPages;
    }
}