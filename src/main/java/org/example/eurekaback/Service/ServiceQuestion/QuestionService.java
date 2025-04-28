package org.example.eurekaback.Service.ServiceQuestion;

import org.example.eurekaback.Entity.Question;

import java.util.List;
import java.util.Optional;

public interface QuestionService {
    List<Question> getAllQuestions();
    Optional<Question> getQuestionById(int id);
    Question createQuestion(Question question);
    void deleteQuestion(int id);
}
