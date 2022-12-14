package com.openjob.admin.job;

import com.openjob.common.model.Job;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class JobService {
    private final JobRepository jobRepo;
    private final Sort sort = Sort.by(Sort.Direction.DESC,"createdAt");

    public Page<Job> getAll(Integer page, Integer size, String keyword, Integer majorId, Integer specializationId) {
        Pageable pageable = PageRequest.of(page, size, sort);
        List<Job> results;
        if (Objects.isNull(keyword) || keyword.isBlank()) {
            results = jobRepo.findAll(pageable).getContent();
        } else {
            results = jobRepo.findAllWithKeyword(keyword, pageable).getContent();
        }

        if (Objects.nonNull(majorId)) {
            results = results.stream()
                    .filter(job -> Objects.equals(job.getSpecialization().getMajor().getId(), majorId))
                    .collect(Collectors.toList());
        }
        if (Objects.nonNull(specializationId)) {
            results = results.stream()
                    .filter(job -> Objects.equals(job.getSpecialization().getId(), specializationId))
                    .collect(Collectors.toList());
        }
        return new PageImpl<>(results);
    }

    public Page<Job> getAllByCompanyId(Integer page, Integer size, String keyword, String companyId) {
        Pageable pageable = PageRequest.of(page, size, sort);
        if (Objects.isNull(keyword) || keyword.isBlank()) {
            return jobRepo.findAllByCompanyId(companyId, pageable);
        }
        return jobRepo.findAllByCompanyIdWithKeyword(companyId, keyword, pageable);
    }

    public Optional<Job> getById(String jobId) {
        return jobRepo.findById(jobId);
    }

//    public Page<Job> getAllwithSkillnotverified(Integer page, Integer size) {
//        Pageable pageable = PageRequest.of(page, size);
//        return jobRepo.findAllwithSkillnotVerified(pageable);
//    }

}
