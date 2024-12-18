package com.mgu.r2dbc.entity;

import lombok.*;
import org.springframework.data.annotation.Id;

@NoArgsConstructor
@RequiredArgsConstructor
@Data
public class Station {
    @Id
    private Long id;
    @NonNull
    private String iataCode;
    @NonNull
    private String fullName;

    public String toString() {
        return "Station: " + id + "/" + iataCode + "/"+fullName;
    }
}
