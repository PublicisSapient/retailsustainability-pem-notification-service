package com.publicis.sapient.p2p.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;

@NoArgsConstructor
@AllArgsConstructor
@ToString
@Setter
@Getter
@Schema(name = "Notification", description = "Dto of the Notification Service")
public class NotificationDto implements Serializable {


    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String id;
    @Schema(type = "String", description = "Id to whom message was sent")
    private String toId;
    @Schema(type = "String", description = "Id from where the message was sent")
    private String fromId;
    @Schema(type = "String", description = "Product Id for the Product")
    private String productId;
    @Schema(type = "String", description = "Text Message")
    private String message;
}
