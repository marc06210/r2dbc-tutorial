package com.mgu.r2dbc.entity;

import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.annotation.Version;

import java.time.LocalDateTime;

@Data
public class AirPlane {
    @Id
    private Long id;
    private String name;
    private boolean workInProgress = false;

    @Version
    private Long version;
    @CreatedDate
    private LocalDateTime createdDate;
    @LastModifiedDate
    private LocalDateTime lastModifiedDate;

    public AirPlane() {}
 
    public AirPlane(Long id, String name, boolean workInProgress) {
        this.id = id;
        this.name = name;
        this.workInProgress = workInProgress;
    }
 }