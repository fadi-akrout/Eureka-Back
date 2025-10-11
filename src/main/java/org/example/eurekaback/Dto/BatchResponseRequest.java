package org.example.eurekaback.DTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class BatchResponseRequest {
    private List<ReponseDTO> responses;
    private Integer campaignId;
    private Integer panelisteId;
}