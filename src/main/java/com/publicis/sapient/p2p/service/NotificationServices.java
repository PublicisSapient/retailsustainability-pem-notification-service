package com.publicis.sapient.p2p.service;

import com.publicis.sapient.p2p.model.Notification;
import com.publicis.sapient.p2p.vo.ServiceResponse;
import org.springframework.stereotype.Service;

@Service
public interface NotificationServices {

    ServiceResponse saveEmailNotifications(String id, Notification notificationRequest);
    ServiceResponse msgRead(String userId ,String toId, String productId);
    ServiceResponse getUserMsgList(String id);
    ServiceResponse getAllMsgOfUser(String userId,String toId, String productId);
    ServiceResponse getNewNotificationCount(String userId);
    void deleteAllMessagesByUser(String userId);
    void deleteAllMessagesByProduct(String productId);
}
