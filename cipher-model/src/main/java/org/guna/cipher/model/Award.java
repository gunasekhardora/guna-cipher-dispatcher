package org.guna.cipher.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Award {
    @JsonProperty("employee_of_the_month")
    private boolean employeeOfTheMonth;
    @JsonProperty("innovation_award")
    private boolean innovationAward;
}
