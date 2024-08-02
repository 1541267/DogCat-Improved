package com.community.dogcat.controller.advice;

import antlr.NoViableAltException;
import javassist.NotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.ModelAndView;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

import javax.servlet.http.HttpServletResponse;

@Slf4j
@RestControllerAdvice
public class CustomRestAdvice {

    @ExceptionHandler(BindException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST) // 400
    public void handleBindException(BindException e, HttpServletResponse response) throws IOException {

        log.error(e.getMessage());

        response.sendRedirect("/error");
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public void handleFKException(DataIntegrityViolationException  e, HttpServletResponse response) throws IOException {

        log.error(e.getMessage());

        response.sendRedirect("/error");
    }

    @ExceptionHandler(AuthenticationException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED) // 401
    public void handleAuthenticationException(AuthenticationException e, HttpServletResponse response) throws IOException {

        log.error(e.getMessage());

        response.sendRedirect("/user/login");
    }

    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN) // 403
    public void handleAccessDeniedException(AccessDeniedException e, HttpServletResponse response) throws IOException {
        log.error(e.getMessage());

        response.sendRedirect("/error");
    }

    @ExceptionHandler({
        NoSuchElementException.class,
        EmptyResultDataAccessException.class})
    @ResponseStatus(HttpStatus.NOT_FOUND) // 404
    public void handleNoSuchElement(Exception e, HttpServletResponse response) throws IOException {

        log.error(e.getMessage());

        response.sendRedirect("/error");
    }

}
