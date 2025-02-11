package com.pioneer.agro_claim.dto;

import lombok.Data;

@Data
public class FailureResponseDto {
    String status;
    String message;

    public FailureResponseDto(Object message){
        this.status="Failure";
        this.message=(message != null) ? message.toString() : "";
    }
}
