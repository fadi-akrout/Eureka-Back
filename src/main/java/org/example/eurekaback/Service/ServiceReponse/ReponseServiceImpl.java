package org.example.eurekaback.Service.ServiceReponse;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.example.eurekaback.DTO.ReponseDTO;
import org.example.eurekaback.DTO.ReponseDetailDTO;
import org.example.eurekaback.Entity.Paneliste;
import org.example.eurekaback.Entity.Question;
import org.example.eurekaback.Entity.Reponse;
import org.example.eurekaback.Repository.FormulaireRepository;
import org.example.eurekaback.Repository.PanelisteRepository;
import org.example.eurekaback.Repository.QuestionRepository;
import org.example.eurekaback.Repository.ReponseRepository;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class ReponseServiceImpl implements ReponseService {
    private final ReponseRepository reponseRepository;
    private final QuestionRepository questionRepository;
    private final FormulaireRepository formulaireRepository;
    private final PanelisteRepository panelisteRepository;

    @Override
    public List<Reponse> getAllReponses() {
        return reponseRepository.findAll();
    }

    @Override
    public Optional<Reponse> getReponseById(Integer id) {
        return reponseRepository.findById(id);
    }

    @Override
    public Reponse createReponse(Reponse reponse) {
        return reponseRepository.save(reponse);
    }

    @Override
    public void deleteReponse(Integer id) {
        reponseRepository.deleteById(id);
    }
    
    @Override
    @Transactional
    public void saveBatchResponses(List<ReponseDTO> responseDTOs, Integer campaignId, Integer panelisteId) {
        // Validate that the formulaire exists for the campaign
        // Use a simpler query to avoid MultipleBagFetchException
        formulaireRepository.findByCampagneIdWithQuestions(campaignId)
            .orElseThrow(() -> new EntityNotFoundException("Formulaire not found for campaign ID: " + campaignId));
        
        List<Reponse> reponses = new ArrayList<>();
        
        for (ReponseDTO dto : responseDTOs) {
            Reponse reponse = new Reponse();
            
            // Set basic properties
            reponse.setDate(dto.getDate() != null ? dto.getDate() : new Date());
            reponse.setResponseText(dto.getResponseText());
            
            // Set relationships
            Question question = questionRepository.findById(dto.getQuestionId())
                .orElseThrow(() -> new EntityNotFoundException("Question not found with ID: " + dto.getQuestionId()));
            reponse.setQuestion(question);
            
            // Set IDs
            reponse.setPanelisteId(panelisteId);
            reponse.setFormulaireId(dto.getFormulaireId());
            
            // Handle option IDs
            if (dto.getOptionIds() != null && !dto.getOptionIds().isEmpty()) {
                reponse.setOptionIds(dto.getOptionIds());
            }
            
            reponses.add(reponse);
        }
        
        // Save all responses
        reponseRepository.saveAll(reponses);
    }
    
    @Override
    public List<Reponse> getResponsesByFormulaireId(Integer formulaireId) {
        return reponseRepository.findByFormulaireId(formulaireId);
    }
    
    @Override
    public List<Reponse> getResponsesByPanelisteId(Integer panelisteId) {
        return reponseRepository.findByPanelisteId(panelisteId);
    }
    
    @Override
    public List<Reponse> getResponsesByPanelisteIdAndFormulaireId(Integer panelisteId, Integer formulaireId) {
        return reponseRepository.findByPanelisteIdAndFormulaireId(panelisteId, formulaireId);
    }
    
    @Override
    public List<Reponse> getResponsesByCampaignIdAndPanelisteId(Integer campaignId, Integer panelisteId) {
        return reponseRepository.findByCampaignIdAndPanelisteId(campaignId, panelisteId);
    }
    
    @Override
    public List<ReponseDetailDTO> getResponsesByCampaignWithDetails(Integer campaignId) {
        // Get all responses for the campaign
        List<Reponse> responses = reponseRepository.findByCampaignId(campaignId);
        return mapToDetailDTOs(responses);
    }
    
    @Override
    public List<ReponseDetailDTO> getResponsesByCampaignAndPanelisteWithDetails(Integer campaignId, Integer panelisteId) {
        // Get responses for the specific campaign and panelist
        List<Reponse> responses = reponseRepository.findByCampaignIdAndPanelisteId(campaignId, panelisteId);
        return mapToDetailDTOs(responses);
    }
    
    @Override
    public ResponseEntity<Resource> exportResponsesToCsv(Integer campaignId) {
        List<ReponseDetailDTO> responses = getResponsesByCampaignWithDetails(campaignId);
        
        // Create CSV content
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PrintWriter writer = new PrintWriter(new OutputStreamWriter(out, StandardCharsets.UTF_8));
        
        // Write CSV header
        writer.println("Response ID,Question ID,Question Text,Response Text,Panelist ID,Panelist Name,Panelist Email,Submission Date");
        
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        
        // Write data rows
        for (ReponseDetailDTO response : responses) {
            writer.println(
                String.format("%d,%d,\"%s\",\"%s\",%d,\"%s\",\"%s\",%s",
                    response.getId(),
                    response.getQuestionId(),
                    escapeCSV(response.getQuestionText()),
                    escapeCSV(response.getResponseText()),
                    response.getPanelisteId(),
                    escapeCSV(response.getPanelisteName()),
                    escapeCSV(response.getPanelisteEmail()),
                    response.getDate() != null ? dateFormat.format(response.getDate()) : ""
                )
            );
        }
        
        writer.flush();
        
        // Create resource from CSV content
        ByteArrayResource resource = new ByteArrayResource(out.toByteArray());
        
        // Build response
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=campaign_" + campaignId + "_responses.csv")
            .contentType(MediaType.parseMediaType("text/csv"))
            .contentLength(resource.contentLength())
            .body(resource);
    }
    
    /**
     * Helper method to escape CSV values
     */
    private String escapeCSV(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("\"", "\"\"");
    }
    
    /**
     * Helper method to map Reponse entities to ReponseDetailDTOs with panelist information
     */
    private List<ReponseDetailDTO> mapToDetailDTOs(List<Reponse> responses) {
        return responses.stream().map(response -> {
            ReponseDetailDTO dto = new ReponseDetailDTO();
            
            // Set basic response properties
            dto.setId(response.getId());
            dto.setResponseText(response.getResponseText());
            dto.setDate(response.getDate());
            dto.setOptionIds(response.getOptionIds());
            
            // Set question information
            Question question = response.getQuestion();
            if (question != null) {
                dto.setQuestionId(question.getId());
                dto.setQuestionText(question.getTexte());
            }
            
            // Set panelist information
            Integer panelisteId = response.getPanelisteId();
            dto.setPanelisteId(panelisteId);
            
            if (panelisteId != null) {
                Optional<Paneliste> panelisteOpt = panelisteRepository.findById(panelisteId);
                if (panelisteOpt.isPresent()) {
                    Paneliste paneliste = panelisteOpt.get();
                    dto.setPanelisteName(paneliste.getNom());
                    dto.setPanelisteEmail(paneliste.getEmail());
                }
            }
            
            return dto;
        }).collect(Collectors.toList());
    }
}
