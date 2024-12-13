package com.example.RDSExample.controller;


import org.apache.coyote.Response;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public class BaseController<ApiResponse> {

    public ResponseEntity<ApiResponse> buildResponse(ApiResponse apiResponse, HttpStatus statusCode) {
        return ResponseEntity.status(statusCode).body(apiResponse);
    }
}

