package com.publicis.sapient.p2p.contoller;

import com.publicis.sapient.p2p.controller.NotificationController;
import com.publicis.sapient.p2p.exception.BusinessException;
import com.publicis.sapient.p2p.exception.util.ErrorCode;
import com.publicis.sapient.p2p.model.CookieResponse;
import com.publicis.sapient.p2p.model.Notification;
import com.publicis.sapient.p2p.model.NotificationDto;
import com.publicis.sapient.p2p.model.ServiceResponseDto;
import com.publicis.sapient.p2p.service.JwtUtils;
import com.publicis.sapient.p2p.service.NotificationServices;
import com.publicis.sapient.p2p.validator.NotificationValidator;
import com.publicis.sapient.p2p.vo.ServiceResponse;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@ContextConfiguration(classes = {NotificationController.class})
@ExtendWith(SpringExtension.class)
class NotificationControllerTest {

    @Autowired
    NotificationController notificationController;

    @MockBean
    NotificationServices notificationService;

    @MockBean
    ModelMapper modelMapper;

    @MockBean
    JwtUtils jwtUtils;

    @MockBean
    NotificationValidator notificationValidator;

    @Test
    void testSendEmailNotification() {
        NotificationDto notificationDto = new NotificationDto();
        Notification notification = modelMapper.map(notificationDto, Notification.class);
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);

        ServiceResponse expectedResponse = new ServiceResponse();
        when(notificationService.saveEmailNotifications(anyString(), any(Notification.class))).thenReturn(expectedResponse);
        when(jwtUtils.getTokenFromCookie(any())).thenReturn(new CookieResponse("userId", "a@a.com", "token", new Cookie("a", "a"), new Cookie("a", "a"), new Cookie("a", "a")));
        ResponseEntity<ServiceResponse> responseEntity = notificationController.sendMailNotification(request, response, notificationDto);

        verify(notificationService).saveEmailNotifications("userId", notification);
        assertEquals(200,responseEntity.getStatusCode().value());


    }


    @Test
    void testUserMsgList() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);

        ServiceResponse expectedResponse = new ServiceResponse();
        when(notificationService.getUserMsgList(anyString())).thenReturn(expectedResponse);
        when(jwtUtils.getTokenFromCookie(any())).thenReturn(new CookieResponse("id", "a@a.com", "token", new Cookie("a", "a"), new Cookie("a", "a"), new Cookie("a", "a")));
        ResponseEntity<ServiceResponse> responseEntity = notificationController.userMsgList(request,response);

        verify(notificationService).getUserMsgList("id");
        assertEquals(200,responseEntity.getStatusCode().value());


    }

    @Test
    void testGetAllMsgOfUser() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);

        ServiceResponse expectedResponse = new ServiceResponse();
        when(notificationService.getAllMsgOfUser(anyString(),anyString(),anyString())).thenReturn(expectedResponse);
        when(jwtUtils.getTokenFromCookie(any())).thenReturn(new CookieResponse("1", "a@a.com", "token", new Cookie("a", "a"), new Cookie("a", "a"), new Cookie("a", "a")));
        ResponseEntity<ServiceResponse> responseEntity = notificationController.getAllMsgOfUser(request, response,"1","2");

        verify(notificationService).getAllMsgOfUser("1","1","2");
        assertEquals(200,responseEntity.getStatusCode().value());


    }

    @Test
    void testNewNotificationCount() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);

        ServiceResponse expectedResponse = new ServiceResponse();
        when(notificationService.getNewNotificationCount(anyString())).thenReturn(expectedResponse);
        when(jwtUtils.getTokenFromCookie(any())).thenReturn(new CookieResponse("1", "a@a.com", "token", new Cookie("a", "a"), new Cookie("a", "a"), new Cookie("a", "a")));
        ResponseEntity<ServiceResponse> responseEntity = notificationController.getNewNotificationCount(request, response);

        verify(notificationService).getNewNotificationCount("1");
        assertEquals(200,responseEntity.getStatusCode().value());


    }


    @Test
    void testDeleteAllMessagesByUser() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);

        when(jwtUtils.getTokenFromCookie(any())).thenReturn(new CookieResponse("userId", "a@a.com", "token", new Cookie("a", "a"), new Cookie("a", "a"), new Cookie("a", "a")));
        ServiceResponseDto expectedResponse = new ServiceResponseDto();
        expectedResponse.setStatusCode(HttpStatus.OK.value());
        expectedResponse.setMessage("All Messages Deleted");

        doNothing().when(notificationService).deleteAllMessagesByUser("userId");

        ServiceResponseDto actualResponse = notificationController.deleteAllMessagesByUser(request, response, "userId");

        assertEquals(expectedResponse.getStatusCode(), actualResponse.getStatusCode());
        assertEquals(expectedResponse.getMessage(), actualResponse.getMessage());
        verify(notificationService).deleteAllMessagesByUser("userId");
    }

    @Test
    void testDeleteAllMessagesByUserWithException() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);

        when(jwtUtils.getTokenFromCookie(any())).thenReturn(new CookieResponse("userId1", "a@a.com", "token", new Cookie("a", "a"), new Cookie("a", "a"), new Cookie("a", "a")));
        BusinessException ex = Assertions.assertThrows(BusinessException.class, () -> notificationController.deleteAllMessagesByUser(request, response, "userId"));
        String expectedErrorMessage =  "Deleting for user : userId  & Logged in user : userId1";
        assertEquals(expectedErrorMessage, ex.getMessage());
        assertEquals(ErrorCode.UNAUTHORIZED, ex.getErrorCode());
    }

    @Test
    void testDeleteAllMessagesByProduct() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);

        when(jwtUtils.getTokenFromCookie(any())).thenReturn(new CookieResponse("userId", "a@a.com", "token", new Cookie("a", "a"), new Cookie("a", "a"), new Cookie("a", "a")));
        ServiceResponseDto expectedResponse = new ServiceResponseDto();
        expectedResponse.setStatusCode(HttpStatus.OK.value());
        expectedResponse.setMessage("All Messages Deleted");

        doNothing().when(notificationService).deleteAllMessagesByProduct("productId");

        ServiceResponseDto actualResponse = notificationController.deleteAllMessagesByProduct(request, response, "userId","productId");

        assertEquals(expectedResponse.getStatusCode(), actualResponse.getStatusCode());
        assertEquals(expectedResponse.getMessage(), actualResponse.getMessage());
        verify(notificationService).deleteAllMessagesByProduct("productId");
    }

    @Test
    void testDeleteAllMessagesByProductWithException() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);

        when(jwtUtils.getTokenFromCookie(any())).thenReturn(new CookieResponse("userId1", "a@a.com", "token", new Cookie("a", "a"), new Cookie("a", "a"), new Cookie("a", "a")));

        BusinessException ex = Assertions.assertThrows(BusinessException.class, () -> notificationController.deleteAllMessagesByProduct(request, response, "userId","productId"));
        String expectedErrorMessage =  "Deleting for user : userId  & Logged in user : userId1";
        assertEquals(expectedErrorMessage, ex.getMessage());
        assertEquals(ErrorCode.UNAUTHORIZED, ex.getErrorCode());
    }
}
