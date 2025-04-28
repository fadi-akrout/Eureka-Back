package org.example.eurekaback.Service.ServiceOption;

import lombok.AllArgsConstructor;
import org.example.eurekaback.Entity.Option;
import org.example.eurekaback.Repository.OptionRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class OptionServiceImpl implements OptionService {
    private final OptionRepository optionRepository;

    @Override
    public List<Option> getAllOptions() {
        return optionRepository.findAll();
    }

    @Override
    public Optional<Option> getOptionById(int id) {
        return optionRepository.findById(id);
    }

    @Override
    public Option createOption(Option option) {
        return optionRepository.save(option);
    }

    @Override
    public void deleteOption(int id) {
        optionRepository.deleteById(id);
    }
}