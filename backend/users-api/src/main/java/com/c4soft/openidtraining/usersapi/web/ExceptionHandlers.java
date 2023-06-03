package com.c4soft.openidtraining.usersapi.web;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

import jakarta.validation.ConstraintViolationException;

@ControllerAdvice
public class ExceptionHandlers {
	
	@ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
	@ExceptionHandler(ConstraintViolationException.class)
	public List<String> handleConstraintViolation(ConstraintViolationException ex) {
		return ex.getConstraintViolations().stream().map(cv -> "%s: %s".formatted(cv.getPropertyPath(), cv.getMessage())).toList();
	}

}
