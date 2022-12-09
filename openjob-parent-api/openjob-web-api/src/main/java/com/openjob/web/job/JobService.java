package com.openjob.web.job;

import com.openjob.common.enums.CvStatus;
import com.openjob.common.model.*;
import com.openjob.web.company.CompanyService;
import com.openjob.web.cv.CvRepository;
import com.openjob.web.dto.JobRequestDTO;
import com.openjob.web.dto.JobSkillDTO;
import com.openjob.web.jobcv.JobCvRepository;
import com.openjob.web.jobcv.JobCvService;
import com.openjob.web.jobskill.JobSkillRepository;
import com.openjob.web.major.MajorService;
import com.openjob.web.skill.SkillRepository;
import com.openjob.web.specialization.SpecializationService;
import com.openjob.web.util.JobCVUtils;
import com.openjob.web.util.NullAwareBeanUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.InvocationTargetException;
import java.util.*;

@Service
@Transactional
@RequiredArgsConstructor
public class JobService {
    private final JobRepository jobRepo;
    private final SkillRepository skillRepo;
    private final JobCvService jobCvService;
    private final JobCvRepository jobCvRepo;
    private final CvRepository cvRepo;
    private final CompanyService companyService;
    private final SpecializationService speService;
    private final JobSkillRepository jobSkillRepo;
    private final MajorService majorService;


    public Optional<Job> getById(String id) {
        return jobRepo.findById(id);
    }

    public Job saveNewJob(JobRequestDTO jobDTO) throws InvocationTargetException, IllegalAccessException {
        NullAwareBeanUtils beanCopier = NullAwareBeanUtils.getInstance();
        Job job = new Job();
        beanCopier.copyProperties(job, jobDTO);

        Company company = companyService.getById(jobDTO.getCompanyId());
        Optional<Specialization> specialization = speService.getById(jobDTO.getSpecializationId());
        Optional<Major> major = majorService.getById(jobDTO.getMajorId());
        if (Objects.isNull(company) || specialization.isEmpty() || major.isEmpty())
            throw new IllegalArgumentException("Company/Major/Specialization not found!");
        job.setCompany(company);
        job.setMajor(major.get());
        job.setSpecialization(specialization.get());

        Job savedJob = jobRepo.save(job);

        List<JobSkill> realListJobSkill = new ArrayList<>();

        // detect new skill
        for (int i = 0; i < jobDTO.getListJobSkillDTO().size(); i++) {
            JobSkillDTO JSfromRequest = jobDTO.getListJobSkillDTO().get(i);
            JobSkill jobSkill = new JobSkill();

            Skill skillFromRequest = JSfromRequest.getSkill();
            Optional<Skill> skillInDB = skillRepo.findByNameAndExperience(skillFromRequest.getName(), skillFromRequest.getExperience());
            if (skillInDB.isPresent()){
                jobSkill.setRequired(JSfromRequest.getIsRequired());
                jobSkill.setSkill(skillInDB.get());
                jobSkill.setJob(savedJob);
                realListJobSkill.add(jobSkillRepo.save(jobSkill));
            } else {
                skillFromRequest.setSpecialization(savedJob.getSpecialization());
                skillFromRequest.setIsVerified(skillRepo.existsByName(skillFromRequest.getName()));
                Skill savedSkill = skillRepo.save(skillFromRequest);
                jobSkill.setRequired(JSfromRequest.getIsRequired());
                jobSkill.setSkill(savedSkill);
                jobSkill.setJob(savedJob);
                realListJobSkill.add(jobSkillRepo.save(jobSkill));
            }

        }


        job.setJobSkills(realListJobSkill);
        job.setCreatedAt(new Date());
        return jobRepo.save(job);
    }

    public Page<Job> searchByKeywordAndLocationAndCompany(Integer size, Integer page, String keyword, String location, String companyId) {
        Page<Job> pageJob;
        Pageable pageable = PageRequest.of(page, size);
        if (Objects.isNull(location) || location.isBlank()) {
            if (Objects.isNull(keyword) || keyword.isBlank())
                pageJob = jobRepo.findAll(pageable);
            else
                pageJob = jobRepo.findByKeyword(keyword, pageable);
        } else {
            if (Objects.isNull(keyword) || keyword.isBlank())
                pageJob = jobRepo.findByLocation(location, pageable);
            else
                pageJob = jobRepo.findByKeywordAndLocation(keyword, location, pageable);
        }
        if (Objects.nonNull(companyId) && !companyId.isBlank()) {
            List<Job> jobs = new ArrayList<>(pageJob.getContent());
            for (int i = 0; i < jobs.size(); i++)
                if (!jobs.get(i).getCompany().getId().equals(companyId))
                    jobs.remove(jobs.get(i));

            return new PageImpl<>(jobs);
        }
        return pageJob;
    }

    public void deleteById(String jobId) {
        jobRepo.deleteById(jobId);
    }

    @Async
    public void findCVmatchJob(Job savedJob) {
        List<CV> listCV = cvRepo.findBySpecialization(savedJob.getSpecialization().getId());

        for (CV cv : listCV) {
            int matchingPoint = JobCVUtils.checkCVmatchJob(savedJob, cv);
            if (matchingPoint > 0) {
                Optional<JobCV> existingJobCv =  jobCvService.getByJobIdAndCvId(savedJob.getId(), cv.getId());
                if (existingJobCv.isPresent()){
                    existingJobCv.get().setIsMatching(true);
                    existingJobCv.get().setPoint(matchingPoint);
                    jobCvService.save(existingJobCv.get());
                }
                else {
                    JobCV newJobCv = new JobCV();
                    newJobCv.setJob(savedJob);
                    newJobCv.setStatus(CvStatus.NEW);
                    newJobCv.setIsMatching(true);
                    newJobCv.setPoint(matchingPoint);
                    newJobCv.setCv(cv);
                    newJobCv.setApplyDate(null);
                    newJobCv.setIsApplied(false);
                    jobCvService.save(newJobCv);
                }
            }
        }

    }

    public List<Job> getByCompanyId(String cId) {
        return jobRepo.findByCompanyId(cId);
    }

    public Page<Job> getJobAppliedByUser(Integer page, Integer size, String userId) {
        Pageable pageable = PageRequest.of(page, size);
        Page<JobCV> jobCVPage = jobCvRepo.findByUserId(userId, pageable);
        List<Job> jobs = new ArrayList<>();
        jobCVPage.getContent().forEach(jobCV -> jobs.add(jobCV.getJob()));
        return new PageImpl<>(jobs);
    }
}
