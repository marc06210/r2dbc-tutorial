package com.mgu.r2dbc.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;

@Data
public class Station {
    @Id
    private Long id;
    private String iataCode;
    private String fullName;
 
    public Long getId() {
        return id;
    }
 
    public Station() {
    }
 
    public Station(String iataCode, String fullName) {
        this.iataCode = iataCode;
        this.fullName = fullName;
    }

    public String toString() {
        return "Station: " + id + "/" + iataCode + "/"+fullName;
    }
}