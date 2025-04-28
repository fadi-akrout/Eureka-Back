package org.example.eurekaback.Service.ServiceQuestion;

import lombok.AllArgsConstructor;
import org.example.eurekaback.Entity.Question;
import org.example.eurekaback.Repository.QuestionRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class QuestionServiceImpl implements QuestionService {
    private final QuestionRepository questionRepository;

    @Override
    public List<Question> getAllQuestions() {
        return questionRepository.findAll();
    }

    @Override
    public Optional<Question> getQuestionById(int id) {
        return questionRepository.findById(id);
    }

    @Override
    public Question createQuestion(Question question) {
        return questionRepository.save(question);
    }

    @Override
    public void deleteQuestion(int id) {
        questionRepository.deleteById(id);
    }
}