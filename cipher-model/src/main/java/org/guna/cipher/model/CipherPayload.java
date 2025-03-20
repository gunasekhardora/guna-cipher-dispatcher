package org.guna.cipher.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CipherPayload {
    private String name;
    private int age;
    private double height;
    @JsonProperty(value = "is_student")
    private boolean isStudent;
    private List<Integer> grades;
    private Address address;

    @JsonFormat(pattern = "yyyyMMdd")
    @JsonProperty(value = "birth_date")
    private String birthDate;
    @JsonProperty(value = "is_employed")
    private boolean isEmployed;
    private double salary;
    private List<String> skills;
    private Achievements achievements;
}
