package com.hn.onelabel.adapter.api.common;

import org.springframework.http.HttpStatus;

import java.util.HashMap;

public class Result<T> extends HashMap<String, Object> {

    public Result<T> code(HttpStatus status) {
        this.put("code", status.value());
        return this;
    }

    public Result<T> message(String message) {
        this.put("message", message);
        return this;
    }

    public Result<T> data(T data) {
        this.put("data", data);
        return this;
    }

    public Result<T> success() {
        this.code(HttpStatus.OK);
        this.data(null);
        this.message("");
        return this;
    }

    public Result<T> success(T data) {
        this.code(HttpStatus.OK);
        this.data(data);
        this.message("");
        return this;
    }

    public Result<T> fail() {
        this.code(HttpStatus.INTERNAL_SERVER_ERROR);
        return this;
    }

    @Override
    public Result<T> put(String key, Object value) {
        super.put(key, value);
        return this;
    }
}
