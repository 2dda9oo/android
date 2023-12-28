package com.example.this_is_changwon;

// 콜백 인터페이스 정의
interface SearchCallback {
    void onSuccess(String result);
    void onFailure(Exception e);
}

