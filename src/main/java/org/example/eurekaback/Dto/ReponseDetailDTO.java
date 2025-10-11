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
public class ReponseDetailDTO {
    private Integer id;
    private Integer questionId;
    private String questionText;
    private String responseText;
    private List<Long> optionIds;
    private Integer panelisteId;
    private String panelisteName;
    private String panelisteEmail;
    private Date date;
}