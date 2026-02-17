package com.example.myapp.data.datasource.remote;
public interface OnResult<T> {
    void onSuccess(T result);
    void onFailure(Exception e);
}