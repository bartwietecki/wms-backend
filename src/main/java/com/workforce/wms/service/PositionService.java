package com.workforce.wms.service;

import com.workforce.wms.entity.Position;
import com.workforce.wms.repository.PositionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class PositionService {

    private final PositionRepository positionRepository;

    public PositionService(PositionRepository positionRepository) {
        this.positionRepository = positionRepository;
    }

    public Position create(Position position) {
        return positionRepository.save(position);
    }

    @Transactional(readOnly = true)
    public Optional<Position> findById(Long id) {
        return positionRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public List<Position> findAll() {
        return positionRepository.findAll();
    }
}
