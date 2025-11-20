package com.finance_control.shared.converter;

import com.finance_control.shared.service.EncryptionService;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * JPA AttributeConverter that automatically encrypts and decrypts email addresses.
 * Encrypts on write to database, decrypts on read from database.
 */
@Slf4j
@Component
@Converter(autoApply = false)
public class EncryptedEmailConverter implements AttributeConverter<String, String> {

    private static EncryptionService encryptionService;

    @Autowired
    public void setEncryptionService(EncryptionService encryptionService) {
        EncryptedEmailConverter.encryptionService = encryptionService;
    }

    @Override
    public String convertToDatabaseColumn(String attribute) {
        if (attribute == null) {
            return null;
        }

        if (encryptionService == null || !encryptionService.isEnabled()) {
            log.debug("Encryption service not available or disabled, storing email as plaintext");
            return attribute;
        }

        try {
            return encryptionService.encrypt(attribute);
        } catch (Exception e) {
            log.error("Failed to encrypt email, storing as plaintext", e);
            return attribute;
        }
    }

    @Override
    public String convertToEntityAttribute(String dbData) {
        if (dbData == null) {
            return null;
        }

        if (encryptionService == null || !encryptionService.isEnabled()) {
            return dbData;
        }

        try {
            return encryptionService.decrypt(dbData);
        } catch (Exception e) {
            log.error("Failed to decrypt email, returning as-is", e);
            return dbData;
        }
    }
}
