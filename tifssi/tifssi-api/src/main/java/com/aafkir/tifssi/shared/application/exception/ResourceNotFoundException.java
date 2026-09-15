package com.aafkir.tifssi.shared.application.exception;

public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String resourceName, Long resourceId) {
        super("%s with id %d was not found.".formatted(resourceName, resourceId));
    }

    public ResourceNotFoundException(String resourceName, String identifier) {
        super("%s '%s' was not found.".formatted(resourceName, identifier));
    }
}
