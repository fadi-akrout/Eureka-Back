package org.example.eurekaback.Repository;

import org.example.eurekaback.Entity.Option;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OptionRepository  extends JpaRepository<Option, Integer> {
}
