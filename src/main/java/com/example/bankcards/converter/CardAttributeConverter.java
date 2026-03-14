package com.example.bankcards.converter;

import com.example.bankcards.util.CardUtil;
import jakarta.persistence.AttributeConverter;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

@org.springframework.stereotype.Component
public class CardAttributeConverter implements AttributeConverter<String, String>, ApplicationContextAware {

    private static ApplicationContext context;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        context = applicationContext;
    }

    @Override
    public String convertToDatabaseColumn(String cardNumber) {
        if (cardNumber == null) return null;
        CardUtil util = context.getBean(CardUtil.class);
        return util.encrypt(cardNumber);
    }

    @Override
    public String convertToEntityAttribute(String encryptedCardNumber) {
        if (encryptedCardNumber == null) return null;
        CardUtil util = context.getBean(CardUtil.class);
        return util.decrypt(encryptedCardNumber);
    }
}