package com.openjob.admin.user;

import com.openjob.common.model.HR;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface HrRepository extends JpaRepository<HR, String> {


}
