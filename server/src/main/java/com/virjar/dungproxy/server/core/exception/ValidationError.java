package com.virjar.dungproxy.server.core.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import java.util.Map;

@JsonInclude(Include.NON_EMPTY)
public class ValidationError {
    private Map<String, String> globalErrors;

    private Map<String, String> fieldErrors;

    public Map<String, String> getGlobalErrors() {
        return globalErrors;
    }

    public void setGlobalErrors(Map<String, String> globalErrors) {
        this.globalErrors = globalErrors;
        this.globalErrors = globalErrors;
    }

    public Map<String, String> getFieldErrors() {
        return fieldErrors;
    }

    public void setFieldErrors(Map<String, String> fieldErrors) {
        this.fieldErrors = fieldErrors;
        this.fieldErrors = fieldErrors;
    }
}