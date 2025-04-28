package org.example.eurekaback.Repository;

import org.example.eurekaback.Entity.Question;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuestionRepository extends JpaRepository<Question, Integer> {
}
