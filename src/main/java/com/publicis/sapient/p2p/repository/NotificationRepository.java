package com.publicis.sapient.p2p.repository;

import com.publicis.sapient.p2p.model.Notification;
import com.publicis.sapient.p2p.model.ResultDto;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends MongoRepository<Notification, String> {


    @Query("{'toId': ?0, 'fromId': ?1, 'productId': ?2 , 'isReaded': false}")
    List<Notification> findUnreadMessages(String userid, String toId, String productId);

    @Aggregation({"{ $match: { $or: [{ toId: ?0 },{ fromId: ?0 }] }}","{ $project: { toId: { $cond: { if: { $eq: ['$fromId', ?0] }, then: '$toId', else: '$fromId' } }, productId: '$productId' ,date: '$date' }}","{ $sort: { date: -1 } }","{ $group: { _id: { productId: '$productId', toId: '$toId' }, date: { $max: '$date' } } }","{ $sort: { date: -1 } }"})
    List<ResultDto> getAllUserList(String userId);

    @Query("{$or: [ { fromId: ?0, toId: ?1 }, { toId: ?0, fromId: ?1 } ], productId: ?2 }")
    List<Notification> getAllMsgOfUser(String fromId, String toId, String productId, Sort sort);

    @Query("{'toId': ?0, 'isReaded': false }")
    List<Notification> getNewNotification(String userId);

    @Query(value="{ $or: [ { 'fromId': ?0 }, { 'toId': ?0 } ] }",delete = true)
    void deleteAllByUserId(String userId);

    @Query(value="{ productId: ?0 }",delete = true)
    void deleteAllByProductId(String productId);

}
