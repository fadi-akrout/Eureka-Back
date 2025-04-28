package org.example.eurekaback.Repository;

import org.example.eurekaback.Entity.Feedback;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FeedbackRepository extends JpaRepository<Feedback, Integer> {
}
