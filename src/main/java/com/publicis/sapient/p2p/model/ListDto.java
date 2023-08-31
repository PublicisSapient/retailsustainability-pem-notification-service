package com.publicis.sapient.p2p.model;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@ToString
@Setter
@Getter
public class ListDto {

    private String toId;
    private String userName;
    private String productId;
    private String productName;
    private Boolean isRead = true;
}
