package com.reoagms.notification_service.exception;

public class EmailTemplateNotFoundException extends RuntimeException {

    public EmailTemplateNotFoundException(String message) {
        super(message);
    }

}
