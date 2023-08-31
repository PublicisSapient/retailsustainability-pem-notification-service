package com.publicis.sapient.p2p.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.publicis.sapient.p2p.exception.BusinessException;
import com.publicis.sapient.p2p.exception.util.ErrorCode;
import com.publicis.sapient.p2p.external.ProductService;
import com.publicis.sapient.p2p.external.ProfileService;
import com.publicis.sapient.p2p.model.*;
import com.publicis.sapient.p2p.repository.NotificationRepository;
import com.publicis.sapient.p2p.service.JwtUtils;
import com.publicis.sapient.p2p.vo.ServiceResponse;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@ContextConfiguration(classes = {NotificationServiceImpl.class})
@ExtendWith(SpringExtension.class)
class NotificationServiceImplTest {

    @MockBean
    private NotificationRepository notificationRepository;

    @MockBean
    private JwtUtils jwtUtils;


    @MockBean
    private ProfileService profileService;

    @MockBean
    private ProductService productService;

    @MockBean
    private ModelMapper modelMapper;

    @MockBean
    private ObjectMapper objectMapper;

    @Autowired
    NotificationServiceImpl notificationServiceImpl;

    @Test
    void testSendEmailNotifications() {
        Notification notificationRequest = new Notification();
        notificationRequest.setFromId("userId");
        when(notificationRepository.save(any(Notification.class))).thenReturn(notificationRequest);

        ServiceResponse serviceResponse = notificationServiceImpl.saveEmailNotifications("userId", notificationRequest);

        verify(notificationRepository).save(notificationRequest);
        assertEquals(notificationRequest, serviceResponse.getOutput());
        assertEquals(String.valueOf(HttpStatus.OK.value()), serviceResponse.getStatusCode());
        assertEquals("Message Sent Successfully.", serviceResponse.getStatusMessage());

    }

    @Test
    void testSendEmailNotificationsWithNoProfile() {
        Notification notificationRequest = new Notification();
        notificationRequest.setFromId("userId");
        notificationRequest.setToId("toId");
        notificationRequest.setProductId("productId");

        when(profileService.getUserDetails("toId")).thenThrow(new BusinessException(ErrorCode.BAD_REQUEST,"Profile not found with ID: toId"));

        Assertions.assertThrows(BusinessException.class, () -> notificationServiceImpl.saveEmailNotifications("userId", notificationRequest));

    }

    @Test
    void testSendEmailNotificationsWithNoProduct() {
        Notification notificationRequest = new Notification();
        notificationRequest.setFromId("userId");
        notificationRequest.setToId("toId");
        notificationRequest.setProductId("productId");

        when(productService.getPDP("productId")).thenThrow(new BusinessException(ErrorCode.BAD_REQUEST,"Product not found with ID: productId"));

        Assertions.assertThrows(BusinessException.class, () -> notificationServiceImpl.saveEmailNotifications("userId", notificationRequest));

    }


    @Test
    void testSendEmailNotificationsWithException() {
        Notification notificationRequest = new Notification();
        notificationRequest.setFromId("userId1");
        when(notificationRepository.save(any(Notification.class))).thenReturn(notificationRequest);

        BusinessException ex = Assertions.assertThrows(BusinessException.class, () -> notificationServiceImpl.saveEmailNotifications("userId", notificationRequest));
        String expectedErrorMessage =  "Unauthorized Access : Saving Messages for user not logged in.";
        assertEquals(expectedErrorMessage, ex.getMessage());
        assertEquals(ErrorCode.UNAUTHORIZED, ex.getErrorCode());

    }

    @Test
    void testMsgRead() {

        String toId = "receiverId";
        String productId = "productId";

        Notification notification = new Notification();
        List<Notification> notificationList = new ArrayList<>();
        notificationList.add(notification);
        when(notificationRepository.findUnreadMessages("userId", toId, productId)).thenReturn(notificationList);
        when(notificationRepository.save(notification)).thenReturn(notification);

        ServiceResponse serviceResponse = notificationServiceImpl.msgRead("userId", toId, productId);

        List<Notification> notificationList1 = new ArrayList<>();
        when(notificationRepository.findUnreadMessages("userId", "toId1", productId)).thenReturn(notificationList1);
        ServiceResponse serviceResponse1 = notificationServiceImpl.msgRead("userId", "toId1", productId);
        assertEquals(String.valueOf(HttpStatus.OK.value()), serviceResponse1.getStatusCode());
        assertEquals("All Messages are Read.", serviceResponse1.getStatusMessage());

        verify(notificationRepository).findUnreadMessages("userId", toId, productId);
        verify(notificationRepository).save(notification);
        assertEquals(String.valueOf(HttpStatus.OK.value()), serviceResponse.getStatusCode());
        assertEquals("Message made as Read.", serviceResponse.getStatusMessage());

    }

    @Test
    void testGetUserMsgList() throws JsonProcessingException {
        List<ResultDto> repositoryResult = new ArrayList<>();
        repositoryResult.add(new ResultDto(new ListDto("toId",null,"productId",null,true)));
        when(notificationRepository.getAllUserList(anyString())).thenReturn(repositoryResult);

        PublicUserDto publicUserDto = new PublicUserDto();
        publicUserDto.setFirstName("firstname");
        publicUserDto.setLastName("lastname");
        ServiceResponseDto serviceResponseDto = new ServiceResponseDto();
        serviceResponseDto.setData(publicUserDto);
        when(profileService.getUserDetails(anyString())).thenReturn(serviceResponseDto);
        when(modelMapper.map(any(PublicUserDto.class),any())).thenReturn(publicUserDto);

        Object productServiceResponse = new Object();
        ServiceResponseDto product = new ServiceResponseDto();
        product.setData(productServiceResponse);
        when(productService.getPDP(anyString())).thenReturn(product);
        String jsonString = "{\"product\":{\"name\":\"Example Product\"}}";
        when(objectMapper.writeValueAsString(any())).thenReturn(jsonString);
        JsonNode jsonNode = mock(JsonNode.class);
        when(objectMapper.readTree(jsonString)).thenReturn(jsonNode);
        when(jsonNode.get("product")).thenReturn(jsonNode);
        when(jsonNode.get("name")).thenReturn(jsonNode);
        when(jsonNode.asText()).thenReturn("Example Product");

        List<Notification> countResult = new ArrayList<>();
        countResult.add(new Notification());
        when(notificationRepository.getNewNotification(anyString())).thenReturn(countResult);

        ServiceResponse result = notificationServiceImpl.getUserMsgList("userId");
        List<ResultDto> resultDto1 = new ArrayList<>();
        when(notificationRepository.getAllUserList("userId1")).thenReturn(resultDto1);
        ServiceResponse serviceResponse1 = notificationServiceImpl.getUserMsgList("userId1");
        assertEquals(String.valueOf(HttpStatus.OK.value()), serviceResponse1.getStatusCode());
        assertEquals("No Messages to Users are Found.", serviceResponse1.getStatusMessage());

        assertNotNull(result);
        assertEquals(HttpStatus.OK.value(), Integer.parseInt(result.getStatusCode()));
        assertEquals("Fetched All UserList.", result.getStatusMessage());

    }



    @Test
    void testGetUserMsgListWithProfileException(){
        List<ResultDto> repositoryResult = new ArrayList<>();
        repositoryResult.add(new ResultDto(new ListDto("toId",null,"productId",null,true)));
        when(notificationRepository.getAllUserList(anyString())).thenReturn(repositoryResult);
        List<Notification> countResult = new ArrayList<>();
        countResult.add(new Notification());
        when(notificationRepository.getNewNotification(anyString())).thenReturn(countResult);

        BusinessException ex = Assertions.assertThrows(BusinessException.class, () -> notificationServiceImpl.getUserMsgList("userId"));
        String expectedErrorMessage =  "Error Receiving Data. Unable to fetch Profile Data: FeignException";
        assertEquals(expectedErrorMessage, ex.getMessage());
        assertEquals(ErrorCode.SERVICE_NOT_AVAILABLE, ex.getErrorCode());

    }


    @Test
    void testGetUserMsgListWithProductException(){
        List<ResultDto> repositoryResult = new ArrayList<>();
        repositoryResult.add(new ResultDto(new ListDto("toId",null,"productId",null,true)));
        when(notificationRepository.getAllUserList(anyString())).thenReturn(repositoryResult);

        PublicUserDto publicUserDto = new PublicUserDto();
        publicUserDto.setFirstName("firstname");
        publicUserDto.setLastName("lastname");
        ServiceResponseDto serviceResponseDto = new ServiceResponseDto();
        serviceResponseDto.setData(publicUserDto);
        when(profileService.getUserDetails(anyString())).thenReturn(serviceResponseDto);
        when(modelMapper.map(any(PublicUserDto.class),any())).thenReturn(publicUserDto);

        List<Notification> countResult = new ArrayList<>();
        countResult.add(new Notification());
        when(notificationRepository.getNewNotification(anyString())).thenReturn(countResult);
        BusinessException ex = Assertions.assertThrows(BusinessException.class, () -> notificationServiceImpl.getUserMsgList("userId"));
        String expectedErrorMessage =  "Error Receiving Data. Unable to fetch Product Data: FeignException";
        assertEquals(expectedErrorMessage, ex.getMessage());
        assertEquals(ErrorCode.SERVICE_NOT_AVAILABLE, ex.getErrorCode());

    }

    @Test
    void getAllMsgOfUserTest() throws JsonProcessingException {

        Notification notification = new Notification();
        List<Notification> notificationList = new ArrayList<>();
        notificationList.add(notification);
        when(notificationRepository.findUnreadMessages("userId", "toId", "productId")).thenReturn(notificationList);
        when(notificationRepository.save(notification)).thenReturn(notification);
        List<Notification> repositoryResult = new ArrayList<>();
        repositoryResult.add(new Notification());
        when(notificationRepository.getNewNotification(anyString())).thenReturn(repositoryResult);

        List<Notification> result = new ArrayList<>();
        result.add(new Notification());
        when(notificationRepository.getAllMsgOfUser(anyString(), anyString(), anyString(), any(Sort.class))).thenReturn(result);

        PublicUserDto publicUserDto = new PublicUserDto();
        publicUserDto.setFirstName("firstname");
        publicUserDto.setLastName("lastname");
        ServiceResponseDto serviceResponseDto = new ServiceResponseDto();
        serviceResponseDto.setData(publicUserDto);
        when(profileService.getUserDetails(anyString())).thenReturn(serviceResponseDto);
        when(modelMapper.map(any(PublicUserDto.class),any())).thenReturn(publicUserDto);

        Object productServiceResponse = new Object();
        ServiceResponseDto product = new ServiceResponseDto();
        product.setData(productServiceResponse);
        when(productService.getPDP(anyString())).thenReturn(product);
        String jsonString = "{\"product\":{\"name\":\"Example Product\"}}";
        when(objectMapper.writeValueAsString(any())).thenReturn(jsonString);
        JsonNode jsonNode = mock(JsonNode.class);
        when(objectMapper.readTree(jsonString)).thenReturn(jsonNode);
        when(jsonNode.get("product")).thenReturn(jsonNode);
        when(jsonNode.get("name")).thenReturn(jsonNode);
        when(jsonNode.get("id")).thenReturn(jsonNode);
        when(jsonNode.get("price")).thenReturn(jsonNode);
        when(jsonNode.get("offerType")).thenReturn(jsonNode);
        when(jsonNode.get("images")).thenReturn(jsonNode);
        when(jsonNode.get(0)).thenReturn(jsonNode);
        when(jsonNode.asText()).thenReturn("Example Product");

        ServiceResponse serviceResult = notificationServiceImpl.getAllMsgOfUser("userId", "toId", "productId");

        assertNotNull(serviceResult);
        assertEquals(HttpStatus.OK.value(), Integer.parseInt(serviceResult.getStatusCode()));
        verify(notificationRepository).findUnreadMessages("userId", "toId", "productId");
        verify(notificationRepository).save(notification);
        verify(notificationRepository).getNewNotification(anyString());
        verify(notificationRepository).getAllMsgOfUser(anyString(), anyString(), anyString(), any(Sort.class));
    }


    @Test
    void getAllMsgOfUserTestWithProfileException(){
        Notification notification = new Notification();
        List<Notification> notificationList = new ArrayList<>();
        notificationList.add(notification);
        when(notificationRepository.findUnreadMessages("userId", "toId", "productId")).thenReturn(notificationList);
        when(notificationRepository.save(notification)).thenReturn(notification);
        List<Notification> repositoryResult = new ArrayList<>();
        repositoryResult.add(new Notification());
        when(notificationRepository.getNewNotification(anyString())).thenReturn(repositoryResult);

        List<Notification> result = new ArrayList<>();
        result.add(new Notification());
        when(notificationRepository.getAllMsgOfUser(anyString(), anyString(), anyString(), any(Sort.class))).thenReturn(result);
        BusinessException ex = Assertions.assertThrows(BusinessException.class, () -> notificationServiceImpl.getAllMsgOfUser("userId", "toId", "productId"));
        String expectedErrorMessage =  "Error Receiving Data. Unable to fetch Profile Data: FeignException";
        assertEquals(expectedErrorMessage, ex.getMessage());
        assertEquals(ErrorCode.SERVICE_NOT_AVAILABLE, ex.getErrorCode());

    }

    @Test
    void getAllMsgOfUserTestWithProductException(){
        Notification notification = new Notification();
        List<Notification> notificationList = new ArrayList<>();
        notificationList.add(notification);
        when(notificationRepository.findUnreadMessages("userId", "toId", "productId")).thenReturn(notificationList);
        when(notificationRepository.save(notification)).thenReturn(notification);
        List<Notification> repositoryResult = new ArrayList<>();
        repositoryResult.add(new Notification());
        when(notificationRepository.getNewNotification(anyString())).thenReturn(repositoryResult);

        PublicUserDto publicUserDto = new PublicUserDto();
        publicUserDto.setFirstName("firstname");
        publicUserDto.setLastName("lastname");
        ServiceResponseDto serviceResponseDto = new ServiceResponseDto();
        serviceResponseDto.setData(publicUserDto);
        when(profileService.getUserDetails(anyString())).thenReturn(serviceResponseDto);
        when(modelMapper.map(any(PublicUserDto.class),any())).thenReturn(publicUserDto);

        List<Notification> result = new ArrayList<>();
        result.add(new Notification());
        when(notificationRepository.getAllMsgOfUser(anyString(), anyString(), anyString(), any(Sort.class))).thenReturn(result);
        BusinessException ex = Assertions.assertThrows(BusinessException.class, () -> notificationServiceImpl.getAllMsgOfUser("userId", "toId", "productId"));
        String expectedErrorMessage =  "Error Receiving Data. Unable to fetch Product Data: FeignException";
        assertEquals(expectedErrorMessage, ex.getMessage());
        assertEquals(ErrorCode.SERVICE_NOT_AVAILABLE, ex.getErrorCode());

    }

    @Test
    void getAllMsgOfUserTestWithNoMessage(){
        Notification notification = new Notification();
        List<Notification> notificationList = new ArrayList<>();
        notificationList.add(notification);
        when(notificationRepository.findUnreadMessages("userId", "toId", "productId")).thenReturn(notificationList);
        when(notificationRepository.save(notification)).thenReturn(notification);
        List<Notification> repositoryResult = new ArrayList<>();
        repositoryResult.add(new Notification());
        when(notificationRepository.getNewNotification(anyString())).thenReturn(repositoryResult);
        List<Notification> result = new ArrayList<>();
        when(notificationRepository.getAllMsgOfUser(anyString(), anyString(), anyString(), any(Sort.class))).thenReturn(result);
        ServiceResponse serviceResult = notificationServiceImpl.getAllMsgOfUser("userId", "toId", "productId");
        String expectedMessage =  "No Messages Found.";

        assertNotNull(serviceResult);
        assertEquals(HttpStatus.OK.value(), Integer.parseInt(serviceResult.getStatusCode()));
        assertEquals(expectedMessage,serviceResult.getStatusMessage());
    }

    @Test
    void getNewNotificationCountTest(){
        List<Notification> repositoryResult = new ArrayList<>();
        repositoryResult.add(new Notification());
        when(notificationRepository.getNewNotification(anyString())).thenReturn(repositoryResult);
        ServiceResponse result = notificationServiceImpl.getNewNotificationCount("UserId");

        assertNotNull(result);
        assertEquals(HttpStatus.OK.value(), Integer.parseInt(result.getStatusCode()));
        assertEquals(repositoryResult.size(), result.getOutput());
        verify(notificationRepository).getNewNotification(anyString());
    }

    @Test
    void testDeleteAllMessagesByUser() {
        String userId = "123";
        doNothing().when(notificationRepository).deleteAllByUserId(userId);
        notificationServiceImpl.deleteAllMessagesByUser(userId);

        verify(notificationRepository).deleteAllByUserId(userId);
    }

    @Test
    void testDeleteAllMessagesByProduct() {
        String productId = "123a";
        doNothing().when(notificationRepository).deleteAllByProductId(productId);
        notificationServiceImpl.deleteAllMessagesByProduct(productId);

        verify(notificationRepository).deleteAllByProductId(productId);
    }
}
