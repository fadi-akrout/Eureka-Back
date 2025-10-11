package org.example.eurekaback.DTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ReponseDTO {
    private Integer questionId;
    private String responseText;
    private List<Long> optionIds;
    private Integer panelisteId;
    private Integer formulaireId;
    private Date date;
}