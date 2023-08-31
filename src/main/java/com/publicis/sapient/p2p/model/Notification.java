package com.publicis.sapient.p2p.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;
import java.util.Date;


@Document
@Entity
@Getter
@Setter
public class Notification implements Serializable {
    @JsonProperty("id")
    @Id
    private String id;
    @JsonProperty("fromId")
    private String fromId;
    @JsonProperty("toId")
    private String toId;
    @JsonProperty("productId")
    private String productId;
    @JsonProperty("message")
    private String message;
    @JsonProperty("date")
    private Date date;
    @JsonProperty("isReaded")
    private boolean isReaded;
}
