package com.rainbowforest.recommendationservice.http.header;

import java.net.URI;
import java.net.URISyntaxException;

// Sửa javax thành jakarta
import jakarta.servlet.http.HttpServletRequest; 

import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;

@Service
public class HeaderGenerator {
    
    public HttpHeaders getHeadersForSuccessGetMethod() {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("Content-Type", "application/json; charset=UTF-8");
        return httpHeaders;
    }
    
    public HttpHeaders getHeadersForError() {
        HttpHeaders httpHeaders = new HttpHeaders();
        // Chuẩn "problem+json" rất tốt cho việc trả về lỗi trong Microservices
        httpHeaders.add("Content-Type", "application/problem+json; charset=UTF-8");
        return httpHeaders;
    }
    
    public HttpHeaders getHeadersForSuccessPostMethod(HttpServletRequest request, Long newResourceId) {
        HttpHeaders httpHeaders = new HttpHeaders();
        try {
            // Tự động tạo URI cho tài nguyên mới (ví dụ: /recommendations/5)
            httpHeaders.setLocation(new URI(request.getRequestURI() + "/" + newResourceId));
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        httpHeaders.add("Content-Type", "application/json; charset=UTF-8");
        return httpHeaders;
    }
}