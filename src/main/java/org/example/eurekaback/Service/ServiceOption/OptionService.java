package org.example.eurekaback.Service.ServiceOption;

import org.example.eurekaback.Entity.Option;

import java.util.List;
import java.util.Optional;

public interface OptionService {
    List<Option> getAllOptions();
    Optional<Option> getOptionById(int id);
    Option createOption(Option option);
    void deleteOption(int id);
}
