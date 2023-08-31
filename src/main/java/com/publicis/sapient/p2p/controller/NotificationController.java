package com.publicis.sapient.p2p.controller;

import com.publicis.sapient.p2p.exception.BusinessException;
import com.publicis.sapient.p2p.exception.util.ErrorCode;
import com.publicis.sapient.p2p.model.Notification;
import com.publicis.sapient.p2p.model.NotificationDto;
import com.publicis.sapient.p2p.model.ServiceResponseDto;
import com.publicis.sapient.p2p.service.JwtUtils;
import com.publicis.sapient.p2p.service.NotificationServices;
import com.publicis.sapient.p2p.validator.NotificationValidator;
import com.publicis.sapient.p2p.vo.ServiceResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.text.MessageFormat;

@RestController
@RequestMapping(value = "/notification")
@Tag(name = "Notification", description = "Notification Services is used for managing the messaging and notification services")
public class NotificationController {

    private final Logger logger = LoggerFactory.getLogger(NotificationController.class);

    @Autowired
    private NotificationServices notificationServices;

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private NotificationValidator notificationValidator;

    private static final String DELETE_MESSAGES = "Deleting for user : {0}  & Logged in user : {1}";

    @PostMapping
    @Operation(operationId = "sendMailNotification", description = "Send Notification Message", summary = "Saves the notification received in database", tags = {"Notification"},
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Notification Details Dto",
                    content = @Content(schema = @Schema(implementation = NotificationDto.class)), required = true),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Message Sent Successfully", content = @Content(schema = @Schema(implementation = ServiceResponse.class))),
                    @ApiResponse(responseCode = "400", description = "Error Sending the Message", content = @Content(schema = @Schema(implementation = ServiceResponse.class))),
            })
    public ResponseEntity<ServiceResponse> sendMailNotification(HttpServletRequest request, HttpServletResponse response, @RequestBody NotificationDto notificationDto){
        logger.info("Request received for registering user profile");
        var cookieResponse = jwtUtils.getTokenFromCookie(request);
        notificationValidator.validate(notificationDto);
        Notification notification = modelMapper.map(notificationDto, Notification.class);
        ServiceResponse serviceResponse = notificationServices.saveEmailNotifications(cookieResponse.getUserId(), notification);

        response.addCookie(cookieResponse.getTokenCookie());
        response.addCookie(cookieResponse.getRefreshTokenCookie());
        response.addCookie(cookieResponse.getNormalCookie());
        return ResponseEntity.ok(serviceResponse);
    }

    @GetMapping(value="/userList")
    @PostMapping
    @Operation(operationId = "userMsgList", description = "Get all the user messages List", summary = "Get all the user messages List", tags = {"Notification"},
            responses = {
                    @ApiResponse(responseCode = "200", description = "List Fetched Successfully", content = @Content(schema = @Schema(implementation = ServiceResponse.class))),
            })
    public ResponseEntity<ServiceResponse> userMsgList(HttpServletRequest request, HttpServletResponse response){
        logger.info("Entered userMsgList in NotificationController.");
        var cookieResponse = jwtUtils.getTokenFromCookie(request);
        ServiceResponse serviceResponse = notificationServices.getUserMsgList(cookieResponse.getUserId());

        response.addCookie(cookieResponse.getTokenCookie());
        response.addCookie(cookieResponse.getRefreshTokenCookie());
        response.addCookie(cookieResponse.getNormalCookie());
        return ResponseEntity.ok(serviceResponse);
    }

    @GetMapping(value="/{toId}/{productId}")
    @Operation(operationId = "getAllMsgOfUser", description = "Get all the Messages from a user of a product.", summary = "Get all the Messages from a user of a product.", tags = {"Notification"},
            responses = {
                    @ApiResponse(responseCode = "200", description = "Messages Fetched Successfully", content = @Content(schema = @Schema(implementation = ServiceResponse.class))),
            })
    public ResponseEntity<ServiceResponse> getAllMsgOfUser( HttpServletRequest request, HttpServletResponse response, @PathVariable(value="toId") String toId, @PathVariable(value="productId") String productId){
        logger.info("Entered getAllMsgOfUser in NotificationController.");
        var cookieResponse = jwtUtils.getTokenFromCookie(request);
        ServiceResponse serviceResponse = notificationServices.getAllMsgOfUser(cookieResponse.getUserId(),toId,productId);

        response.addCookie(cookieResponse.getTokenCookie());
        response.addCookie(cookieResponse.getRefreshTokenCookie());
        response.addCookie(cookieResponse.getNormalCookie());
        return ResponseEntity.ok(serviceResponse);
    }


    @GetMapping(value="/newNotification")
    @Operation(operationId = "getNewNotificationCount", description = "Get all the new Notification Count of User", summary = "Get all the new Notification Count of User", tags = {"Notification"},
            responses = {
                    @ApiResponse(responseCode = "200", description = "Count Fetched Successfully", content = @Content(schema = @Schema(implementation = ServiceResponse.class))),
            })
    public ResponseEntity<ServiceResponse> getNewNotificationCount(HttpServletRequest request, HttpServletResponse response){
        logger.info("Entered getNewNotificationCount in NotificationController.");
        var cookieResponse = jwtUtils.getTokenFromCookie(request);
        ServiceResponse serviceResponse = notificationServices.getNewNotificationCount(cookieResponse.getUserId());

        response.addCookie(cookieResponse.getTokenCookie());
        response.addCookie(cookieResponse.getRefreshTokenCookie());
        response.addCookie(cookieResponse.getNormalCookie());
        return ResponseEntity.ok(serviceResponse);
    }


    @DeleteMapping("/user/{userId}")
    public ServiceResponseDto deleteAllMessagesByUser(HttpServletRequest request, HttpServletResponse response, @PathVariable("userId") String userId){
        logger.info("Entering deleteAllMessagesByUser method in NotificationController");
        var cookieResponse = jwtUtils.getTokenFromCookie(request);
        String loggedInUserId = cookieResponse.getUserId();
        if(!userId.equals(loggedInUserId)){
            logger.error("Unauthorized Access : Deleting All Messages for user not logged in.");
            throw new BusinessException(ErrorCode.UNAUTHORIZED, MessageFormat.format(DELETE_MESSAGES, userId, loggedInUserId));
        }
        notificationServices.deleteAllMessagesByUser(userId);
        ServiceResponseDto serviceResponseDto=new ServiceResponseDto();
        serviceResponseDto.setStatusCode(HttpStatus.OK.value());
        serviceResponseDto.setMessage("All Messages Deleted");

        response.addCookie(cookieResponse.getTokenCookie());
        response.addCookie(cookieResponse.getRefreshTokenCookie());
        response.addCookie(cookieResponse.getNormalCookie());
        return serviceResponseDto;
    }

    @DeleteMapping("/product/{userId}/{productId}")
    public ServiceResponseDto deleteAllMessagesByProduct(HttpServletRequest request, HttpServletResponse response, @PathVariable("userId") String userId, @PathVariable("productId") String productId){
        logger.info("Entering deleteAllMessagesByProduct method in NotificationController");
        var cookieResponse = jwtUtils.getTokenFromCookie(request);
        String loggedInUserId = cookieResponse.getUserId();
        if(!userId.equals(loggedInUserId)){
            logger.error("Unauthorized Access : Deleting All Messages for user not logged in.");
            throw new BusinessException(ErrorCode.UNAUTHORIZED, MessageFormat.format(DELETE_MESSAGES, userId, loggedInUserId));
        }
        notificationServices.deleteAllMessagesByProduct(productId);
        ServiceResponseDto serviceResponseDto=new ServiceResponseDto();
        serviceResponseDto.setStatusCode(HttpStatus.OK.value());
        serviceResponseDto.setMessage("All Messages Deleted");

        response.addCookie(cookieResponse.getTokenCookie());
        response.addCookie(cookieResponse.getRefreshTokenCookie());
        response.addCookie(cookieResponse.getNormalCookie());
        return serviceResponseDto;
    }
    
}
