package com.publicis.sapient.p2p.model;

import lombok.*;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@ToString
@Setter
@Getter
public class MessagesDto {

    List<Notification> message;
    ProductDto product;
    UserDto user;
    int count;
}