package com.publicis.sapient.p2p.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.publicis.sapient.p2p.exception.BusinessException;
import com.publicis.sapient.p2p.exception.util.ErrorCode;
import com.publicis.sapient.p2p.external.ProductService;
import com.publicis.sapient.p2p.external.ProfileService;
import com.publicis.sapient.p2p.model.*;
import com.publicis.sapient.p2p.repository.NotificationRepository;
import com.publicis.sapient.p2p.service.JwtUtils;
import com.publicis.sapient.p2p.service.NotificationServices;
import com.publicis.sapient.p2p.vo.ServiceResponse;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.text.MessageFormat;
import java.time.Instant;
import java.util.*;


@Service
public class NotificationServiceImpl implements NotificationServices {

    private final Logger logger = LoggerFactory.getLogger(NotificationServiceImpl.class);
    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private JwtUtils jwtUtils;


    @Autowired
    private ProfileService profileService;

    @Autowired
    private ProductService productService;

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private ObjectMapper objectMapper;

    private static final String PROFILE_LOGGER = "Not able to get Profile Details : Exception occurred while calling profile-service : {0} : {1} ";
    private static final String PROFILE_ERROR = "Error Receiving Data. Unable to fetch Profile Data: FeignException";
    private static final String PRODUCT_LOGGER = "Not able to get Product Details : Exception occurred while calling product-service : {0} : {1} ";
    private static final String PRODUCT_ERROR = "Error Receiving Data. Unable to fetch Product Data: FeignException";

    @Override
    public ServiceResponse saveEmailNotifications(String id, Notification notificationRequest) {
        logger.info("Entered saveEmailNotifications method in NotificationServiceImpl.");
        ServiceResponse serviceResponse = new ServiceResponse();
        notificationRequest.setDate(Timestamp.from(Instant.now()));
        validateProfileProduct(notificationRequest.getToId(), notificationRequest.getProductId());
        if (id.equals(notificationRequest.getFromId())) {
            serviceResponse.setOutput(notificationRepository.save(notificationRequest));
            logger.info("Message Sent Successfully.");
            serviceResponse.setStatusCode(String.valueOf(HttpStatus.OK.value()));
            serviceResponse.setStatusMessage("Message Sent Successfully.");
        } else {
            logger.error("Unauthorized Access : Saving Messages for user not logged in.");
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "Unauthorized Access : Saving Messages for user not logged in.");
        }
        return serviceResponse;
    }

    private void validateProfileProduct(String profileId, String productId){
        logger.info("Entered validateProfileProduct method in NotificationServiceImpl.");
        try {
            profileService.getUserDetails(profileId);
        }
        catch(Exception e){
            logger.error("Profile not found");
            throw new BusinessException(ErrorCode.BAD_REQUEST, MessageFormat.format("Profile not found with ID: {0}",profileId));
        }
        try {
            productService.getPDP(productId);
        }
        catch(Exception e){
            logger.error("Product not found");
            throw new BusinessException(ErrorCode.BAD_REQUEST, MessageFormat.format("Product not found with ID: {0}",productId));
        }
    }

    @Override
    public ServiceResponse msgRead(String userId, String toId, String productId) {
        logger.info("Entered msgRead method in NotificationServiceImpl.");
        ServiceResponse serviceResponse = new ServiceResponse();
        logger.info("Fetched all unread messages.");
        List<Notification> notification = notificationRepository.findUnreadMessages(userId, toId, productId);
        for (Notification msg : notification) {
            msg.setReaded(true);
            notificationRepository.save(msg);
        }
        serviceResponse.setStatusCode(String.valueOf(HttpStatus.OK.value()));
        serviceResponse.setStatusMessage(notification.isEmpty() ? "All Messages are Read." : "Message made as Read.");
        return serviceResponse;
    }

    @Override
    public ServiceResponse getUserMsgList(String userId) {

        logger.info("Entered getUserMsgList method in NotificationServiceImpl.");
        ServiceResponse response = new ServiceResponse();
        ServiceResponse countResponse = getNewNotificationCount(userId);
        int count = (int) countResponse.getOutput();
        List<ListDto> userMsg = new ArrayList<>();
        List<ResultDto> result = notificationRepository.getAllUserList(userId);
        for (ResultDto res : result) {
            ListDto listDto = res.getId();
            List<Notification> unRead = notificationRepository.findUnreadMessages(userId, listDto.getToId(), listDto.getProductId());
            listDto.setIsRead(unRead.isEmpty());
            try {
                PublicUserDto publicToIdUserDto = modelMapper.map(profileService.getUserDetails(listDto.getToId()).getData(), PublicUserDto.class);
                listDto.setUserName(publicToIdUserDto.getFirstName() + " " + publicToIdUserDto.getLastName());
            } catch (Exception ex) {
                logger.error(MessageFormat.format(PROFILE_LOGGER, ex.getClass(), ex.getMessage()));
                throw new BusinessException(ErrorCode.SERVICE_NOT_AVAILABLE, PROFILE_ERROR);
            }
            try {
                Object prod = productService.getPDP(listDto.getProductId()).getData();
                String json = objectMapper.writeValueAsString(prod);
                JsonNode jsonNode = objectMapper.readTree(json);
                listDto.setProductName(jsonNode.get("product").get("name").asText());
            } catch (Exception ex) {
                logger.error(MessageFormat.format(PRODUCT_LOGGER, ex.getClass(), ex.getMessage()));
                throw new BusinessException(ErrorCode.SERVICE_NOT_AVAILABLE, PRODUCT_ERROR);
            }
            userMsg.add(listDto);
        }
        response.setStatusCode(String.valueOf(HttpStatus.OK.value()));
        response.setStatusMessage(result.isEmpty() ? "No Messages to Users are Found." : "Fetched All UserList.");
        UserListDto userListDto = new UserListDto(userMsg, count);
        response.setOutput(userListDto);

        return response;
    }

    @Override
    public ServiceResponse getAllMsgOfUser(String userId, String toId, String productId) {

        logger.info("Entered getAllMsgOfUser method in NotificationServiceImpl.");
        ServiceResponse response = new ServiceResponse();
        msgRead(userId, toId, productId);
        logger.info("Get Count of New Notification.");
        ServiceResponse countResponse = getNewNotificationCount(userId);
        int count = (int) countResponse.getOutput();
        logger.info("Fetched All Messages of User.");
        Sort sort = Sort.by(Sort.Direction.ASC, "date");
        List<Notification> result = notificationRepository.getAllMsgOfUser(userId, toId, productId, sort);
        if (!result.isEmpty()) {
            response.setStatusMessage("Fetched all Messages.");
            ProductDto productDto = new ProductDto();
            UserDto userDto = new UserDto();
            try {
                PublicUserDto publicToIdUserDto = modelMapper.map(profileService.getUserDetails(toId).getData(), PublicUserDto.class);
                userDto.setUserName(publicToIdUserDto.getFirstName() + " " + publicToIdUserDto.getLastName());
            } catch (Exception ex) {
                logger.error(MessageFormat.format(PROFILE_LOGGER, ex.getClass(), ex.getMessage()));
                throw new BusinessException(ErrorCode.SERVICE_NOT_AVAILABLE, PROFILE_ERROR);
            }
            try {
                Object prod = productService.getPDP(productId).getData();
                String json = objectMapper.writeValueAsString(prod);
                JsonNode jsonNode = objectMapper.readTree(json);
                JsonNode prodJson = jsonNode.get("product");
                productDto.setId(prodJson.get("id").asText());
                productDto.setName(prodJson.get("name").asText());
                productDto.setPrice(prodJson.get("price").asText());
                productDto.setOfferType(prodJson.get("offerType").asText());
                productDto.setImages(prodJson.get("images").get(0).asText());
            } catch (Exception ex) {
                logger.error(MessageFormat.format(PRODUCT_LOGGER, ex.getClass(), ex.getMessage()));
                throw new BusinessException(ErrorCode.SERVICE_NOT_AVAILABLE, PRODUCT_ERROR);
            }
            MessagesDto msg = new MessagesDto(result, productDto, userDto, count);
            response.setOutput(msg);
        } else {
            response.setStatusMessage("No Messages Found.");
            MessagesDto msg = new MessagesDto(null, null, null, count);
            response.setOutput(msg);
        }
        response.setStatusCode(String.valueOf(HttpStatus.OK.value()));


        return response;
    }

    @Override
    public ServiceResponse getNewNotificationCount(String userId) {

        logger.info("Entered getNewNotificationCount method in NotificationServiceImpl.");

        ServiceResponse response = new ServiceResponse();
        logger.info("Fetched all Messages.");
        List<Notification> result = notificationRepository.getNewNotification(userId);
        response.setStatusCode(String.valueOf(HttpStatus.OK.value()));
        response.setStatusMessage("New Notification Count.");
        response.setOutput(result.size());
        return response;
    }

    @Override
    @Async
    public void deleteAllMessagesByUser(String userId) {

        logger.info("Entered deleteAllByProductId method in NotificationServiceImpl.");
        notificationRepository.deleteAllByUserId(userId);

    }

    @Override
    @Async
    public void deleteAllMessagesByProduct(String productId) {

        logger.info("Entered deleteAllByProductId method in NotificationServiceImpl.");
        notificationRepository.deleteAllByProductId(productId);

    }
}
