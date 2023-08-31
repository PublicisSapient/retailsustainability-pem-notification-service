package com.publicis.sapient.p2p.model;

import lombok.*;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@ToString
@Setter
@Getter
public class UserListDto {

    List<ListDto> user;
    int count;
}
