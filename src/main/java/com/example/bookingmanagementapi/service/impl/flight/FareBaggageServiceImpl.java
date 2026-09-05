package com.example.bookingmanagementapi.service.impl.flight;

import com.example.bookingmanagementapi.dto.filter.FareBaggageFilter;
import com.example.bookingmanagementapi.dto.request.FareBaggageRequest;
import com.example.bookingmanagementapi.dto.response.FareBaggageResponse;
import com.example.bookingmanagementapi.entity.FareBaggageEntity;
import com.example.bookingmanagementapi.exception.NotFoundException;
import com.example.bookingmanagementapi.mapper.FareBaggageMapper;
import com.example.bookingmanagementapi.repository.FareBaggageRepository;
import com.example.bookingmanagementapi.service.FareBaggageService;
import com.example.bookingmanagementapi.util.ValidationUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FareBaggageServiceImpl implements FareBaggageService {

    private final FareBaggageRepository fareBaggageRepository;
    private final FareBaggageMapper fareBaggageMapper;
    private final ValidationUtil validationUtil;

    @Transactional
    @Override
    public void createFareBaggage(FareBaggageRequest fareBaggageRequest) {

        FareBaggageEntity fareBaggageEntity = fareBaggageMapper.toEntity(fareBaggageRequest);

        fareBaggageRepository.save(fareBaggageEntity);
    }


    @Transactional
    @Override
    public void deleteFareBaggageById(Long id) {

        validationUtil.validateId(id);

        if (!fareBaggageRepository.existsById(id)) {
            throw new NotFoundException("FareBaggage not found");
        }

        fareBaggageRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    @Override
    public FareBaggageResponse findById(Long id) {

        validationUtil.validateId(id);

        FareBaggageEntity fareBaggageEntity = fareBaggageRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("FareBaggageEntity not found with id: " + id));

        return fareBaggageMapper.toDto(fareBaggageEntity);
    }

    @Transactional(readOnly = true)
    @Override
    public List<FareBaggageResponse> getAll(FareBaggageFilter fareBaggageFilter) {

        List<FareBaggageEntity> fareBaggageEntities = fareBaggageRepository.findAll();

        return fareBaggageMapper.toListDto(fareBaggageEntities);
    }
}
