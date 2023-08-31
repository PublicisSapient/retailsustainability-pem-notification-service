package com.publicis.sapient.p2p.model;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@ToString
@Setter
@Getter
public class ProductDto {

    String id;
    String name;
    String price;
    String offerType;
    String images;

}
