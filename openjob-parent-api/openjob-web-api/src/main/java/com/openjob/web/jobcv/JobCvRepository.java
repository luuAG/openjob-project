package com.openjob.web.jobcv;

import com.openjob.common.model.JobCV;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface JobCvRepository extends JpaRepository<JobCV, Integer> {

    @Modifying
    @Query("delete from JobCV jc where jc.cv.id=?1 and jc.job.id=?2 and jc.isMatching=false")
    void deleteByCvIdAndJobId(String cvId, String jobId);

    @Query("select jc from JobCV jc where jc.job.id=?1 and jc.cv.id=?2")
    Optional<JobCV> findByJobIdAndCvId(String jobId, String cvId);
}