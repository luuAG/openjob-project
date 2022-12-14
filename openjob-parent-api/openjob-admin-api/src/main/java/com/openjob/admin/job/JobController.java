package com.openjob.admin.job;

import com.openjob.admin.dto.JobPaginationDTO;
import com.openjob.common.model.Job;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
@RequiredArgsConstructor
public class JobController {
    private final JobService jobService;

    @GetMapping(path = "/jobs", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<JobPaginationDTO> getAll(@RequestParam("page")Integer page,
                                                   @RequestParam("size")Integer size,
                                                   @RequestParam(value = "keyword", required = false)String keyword,
                                                   @RequestParam(value = "majorId", required = false)Integer majorId,
                                                   @RequestParam(value = "specializationId", required = false)Integer specializationId) {
        Page<Job> jobPage = jobService.getAll(page, size, keyword, majorId, specializationId);
        return ResponseEntity.ok(new JobPaginationDTO(
                jobPage.getContent(),
                jobPage.getTotalPages(),
                jobPage.getTotalElements()));
    }


//    @GetMapping(path = "/jobs", produces = MediaType.APPLICATION_JSON_VALUE)
//    public ResponseEntity<JobPaginationDTO> getAll(@RequestParam("page")Integer page,
//                                                   @RequestParam("size")Integer size,
//                                                   @RequestParam(value = "keyword", required = false)String keyword) {
//        Page<Job> jobPage = jobService.getAll(page, size, keyword);
//        return ResponseEntity.ok(new JobPaginationDTO(
//                jobPage.getContent(),
//                jobPage.getTotalPages(),
//                jobPage.getTotalElements()));
//    }

    @GetMapping(path = "/job/by-company/{companyId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<JobPaginationDTO> getAll(@PathVariable("companyId") String companyId,
                                                    @RequestParam("page")Integer page,
                                                   @RequestParam("size")Integer size,
                                                   @RequestParam(value = "keyword", required = false)String keyword) {
        Page<Job> jobPage = jobService.getAllByCompanyId(page, size, keyword, companyId);
        return ResponseEntity.ok(new JobPaginationDTO(
                jobPage.getContent(),
                jobPage.getTotalPages(),
                jobPage.getTotalElements()));
    }

    @GetMapping(path = "/job/{jobId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Job> getById(@PathVariable("jobId") String jobId) {
        Optional<Job> job = jobService.getById(jobId);
        if (job.isPresent()){
            return ResponseEntity.ok(job.get());
        }else
            return ResponseEntity.notFound().build();
    }
}
