package com.publicis.sapient.p2p.validator;

import com.publicis.sapient.p2p.exception.BusinessException;
import com.publicis.sapient.p2p.exception.util.ErrorCode;
import com.publicis.sapient.p2p.model.NotificationDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;


@Component
public class NotificationValidator {

    private final Logger logger = LoggerFactory.getLogger(NotificationValidator.class);

    public void validate(NotificationDto notificationDto) {
        logger.info("Entering validate method inside NotificationValidator");
        validateFromId(notificationDto.getFromId());
        validateToId(notificationDto.getToId());
        validateProductId(notificationDto.getProductId());
        validateMessage(notificationDto.getMessage());
    }

    private void validateFromId(String fromId) {
        logger.info("Entering validateFromId method inside NotificationValidator");
        if(fromId == null) {
            logger.error("Invalid Request Body: FromId cannot be null");
            throw new BusinessException(ErrorCode.BAD_REQUEST, "Invalid Request Body: FromId cannot be null");
        }
    }

    private void validateToId(String toId) {
        logger.info("Entering validateToId method inside NotificationValidator");
        if(toId == null) {
            logger.error("Invalid Request Body: ToId cannot be null");
            throw new BusinessException(ErrorCode.BAD_REQUEST, "Invalid Request Body: ToId cannot be null");
        }
    }

    private void validateProductId(String productId) {
        logger.info("Entering validateProductId method inside NotificationValidator");
        if(productId == null) {
            logger.error("Invalid Request Body: ProductId cannot be null");
            throw new BusinessException(ErrorCode.BAD_REQUEST, "Invalid Request Body: ProductId cannot be null");
        }
    }

    private void validateMessage(String message) {
        logger.info("Entering validateMessage method inside NotificationValidator");
        if(message == null) {
            logger.error("Invalid Request Body: Message cannot be null");
            throw new BusinessException(ErrorCode.BAD_REQUEST, "Invalid Request Body: Message cannot be null");
        }

        if(message.length()<1){
            logger.error("Invalid Request Body: Message should contain a character");
            throw new BusinessException(ErrorCode.BAD_REQUEST, "Invalid Request Body: Message should contain a character");
        }
    }
}
