package com.publicis.sapient.p2p.validator;

import com.publicis.sapient.p2p.exception.BusinessException;
import com.publicis.sapient.p2p.model.NotificationDto;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;


@ExtendWith(MockitoExtension.class)
@ContextConfiguration(classes = {NotificationValidator.class})
@ExtendWith(SpringExtension.class)
class NotificationValidatorTest {

    @Autowired
    private NotificationValidator notificationValidator;


    @Test
    void validateTest(){
        NotificationDto notificationDto = new NotificationDto();
        notificationDto.setFromId("fromId");
        notificationDto.setToId("toId");
        notificationDto.setProductId("productId");
        notificationDto.setMessage("message");

        Assertions.assertDoesNotThrow(() -> notificationValidator.validate(notificationDto));

    }

    @Test
    void validateNullFromId(){
        NotificationDto notificationDto = new NotificationDto();
        notificationDto.setToId("toId");
        notificationDto.setProductId("productId");
        notificationDto.setMessage("message");
        validateError(notificationDto);
    }

    @Test
    void validateNullToId(){
        NotificationDto notificationDto = new NotificationDto();
        notificationDto.setFromId("fromId");
        notificationDto.setProductId("productId");
        notificationDto.setMessage("message");
        validateError(notificationDto);
    }

    @Test
    void validateNullProductId(){
        NotificationDto notificationDto = new NotificationDto();
        notificationDto.setToId("toId");
        notificationDto.setFromId("fromId");
        notificationDto.setMessage("message");
        validateError(notificationDto);
    }

    @Test
    void validateNullMessage(){
        NotificationDto notificationDto = new NotificationDto();
        notificationDto.setToId("toId");
        notificationDto.setProductId("productId");
        notificationDto.setFromId("fromId");
        validateError(notificationDto);
    }

    @Test
    void validateNoMessage(){
        NotificationDto notificationDto = new NotificationDto();
        notificationDto.setToId("toId");
        notificationDto.setProductId("productId");
        notificationDto.setFromId("fromId");
        notificationDto.setMessage("");
        validateError(notificationDto);
    }

    void validateError(NotificationDto notificationDto){
        Assertions.assertThrows(BusinessException.class, () -> notificationValidator.validate(notificationDto));
    }

}
