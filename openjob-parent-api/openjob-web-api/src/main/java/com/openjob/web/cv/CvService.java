package com.openjob.web.cv;

import com.openjob.common.enums.CvStatus;
import com.openjob.common.model.*;
import com.openjob.web.dto.CVRequestDTO;
import com.openjob.web.job.JobRepository;
import com.openjob.web.jobcv.JobCvService;
import com.openjob.web.major.MajorService;
import com.openjob.web.skill.SkillRepository;
import com.openjob.web.specialization.SpecializationService;
import com.openjob.web.user.UserService;
import com.openjob.web.util.JobCVUtils;
import com.openjob.web.util.NullAwareBeanUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
public class CvService {
    private final CvRepository cvRepo;
    private final SkillRepository skillRepo;
    private final SpecializationService speService;
    private final UserService userService;
    private final JobRepository jobRepo;
    private final JobCvService jobCvService;
    private final MajorService majorService;

    public Optional<CV> getById(String id) {
        return cvRepo.findById(id);
    }

    public Optional<CV> getByUserId(String userId) {
        return cvRepo.findByUserId(userId);
    }

    public CV saveUpdate(CVRequestDTO cvDto) throws InvocationTargetException, IllegalAccessException {
        CV cv = new CV();
        NullAwareBeanUtils mapper = NullAwareBeanUtils.getInstance();
        mapper.copyProperties(cv, cvDto);

        Optional<User> user = userService.get(cvDto.getUserId());
        Optional<Specialization> specialization = speService.getById(cvDto.getSpecializationId());
        Optional<Major> major = majorService.getById(cvDto.getMajorId());

        cv.setUser(user.orElseThrow());
        cv.setSpecialization(specialization.orElseThrow());
        cv.setMajor(major.orElseThrow());

        List<Skill> realListSkill = new ArrayList<>();

        for (int i = 0; i < cvDto.getListSkill().size(); i++) {
            Skill skillFromRequest = cv.getListSkill().get(i);
            Optional<Skill> skillInDB = skillRepo.findByNameAndExperience(skillFromRequest.getName(), skillFromRequest.getExperience());
            if (skillInDB.isPresent()) {
                realListSkill.add(skillInDB.get());
            } else {
                skillFromRequest.setSpecialization(cv.getSpecialization());
                skillFromRequest.setIsVerified(skillRepo.existsByName(skillFromRequest.getName()));
                Skill savedSkill = skillRepo.save(skillFromRequest);
                realListSkill.add(savedSkill);
            }

        }

        cv.setListSkill(realListSkill);
        return cvRepo.save(cv);
    }

    @Async
    public void findJobMatchCV(CV savedCV) {
        List<Job> listJob = jobRepo.findBySpecialization(savedCV.getSpecialization().getId());
        for (Job job : listJob) {
            int matchingPoint = JobCVUtils.checkCVmatchJob(job, savedCV);
            if (matchingPoint > 0) {
                Optional<JobCV> existingJobCv =  jobCvService.getByJobIdAndCvId(job.getId(), savedCV.getId());
                if (existingJobCv.isPresent()){
                    existingJobCv.get().setIsMatching(true);
                    existingJobCv.get().setPoint(matchingPoint);
                    jobCvService.save(existingJobCv.get());
                }
                else {
                    JobCV newJobCv = new JobCV();
                    newJobCv.setJob(job);
                    newJobCv.setStatus(CvStatus.NEW);
                    newJobCv.setIsMatching(true);
                    newJobCv.setPoint(matchingPoint);
                    newJobCv.setCv(savedCV);
                    newJobCv.setApplyDate(null);
                    newJobCv.setIsApplied(false);
                    jobCvService.save(newJobCv);
                }
            }
        }
    }

    public Page<CV> getByJobId(Integer page, Integer size, String jobId) {
        Pageable pageable = PageRequest.of(page, size);
        return cvRepo.findByJobId(jobId, pageable);
    }

    public Page<CV> getCvAppliedByJobId(Integer page, Integer size, String jobId) {
        Pageable pageable = PageRequest.of(page, size);
        return cvRepo.findCvAppliedByJobId(jobId, pageable);
    }
}
