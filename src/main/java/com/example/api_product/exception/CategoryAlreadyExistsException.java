package com.example.api_product.exception;

public class CategoryAlreadyExistsException extends RuntimeException {
    public CategoryAlreadyExistsException(String message) {
        super("Category already exists with name " + message);
    }
}
