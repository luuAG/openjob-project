package com.openjob.admin.hr;

import com.openjob.admin.base.AbstractBaseService;
import com.openjob.admin.exception.UserNotFoundException;
import com.openjob.common.model.HR;
import lombok.RequiredArgsConstructor;
import org.springframework.core.NestedExceptionUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.SQLException;
import java.util.Objects;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class HrService extends AbstractBaseService<HR>  {
    private final HrRepository hrRepo;
    private final BCryptPasswordEncoder passwordEncoder;

    public Boolean isExisting(String id){
        if (Objects.nonNull(id) && !id.isBlank()){
            return get(id).isPresent();
        }
        return false;
    }


    @Override
    public Optional<HR> get(String id) {
        return hrRepo.findById(id);
    }

    @Override
    public HR save(HR object) throws SQLException {
        object.setPassword(passwordEncoder.encode(object.getPassword()));
        try {
            return hrRepo.save(object);
        } catch (Exception ex){
            throw new SQLException(NestedExceptionUtils.getMostSpecificCause(ex).getMessage());
        }
    }

    @Override
    public void delete(String id) throws UserNotFoundException {
        Optional<HR> hr = hrRepo.findById(id);
        if (hr.isPresent())
            hrRepo.delete(hr.get());
        else
            throw new UserNotFoundException("User not found for ID: " + id);
    }



    public Page<HR> searchByKeyword(Integer page, Integer size, String keyword) {
        if (Objects.isNull(keyword) || keyword.isEmpty())
            return getAll(page, size);
        Pageable pageable = PageRequest.of(page - 1, size);
        Page<HR> pageHr = hrRepo.search(keyword, pageable);
        pageHr.getContent().forEach(admin -> admin.setPassword("đã che"));
        return pageHr;
    }

    private Page<HR> getAll(Integer page, Integer size) {
        Pageable pageable = PageRequest.of(page - 1, size);
        Page<HR> pageHr = hrRepo.findAll(pageable);
        pageHr.getContent().forEach(admin -> admin.setPassword("đã che"));
        return pageHr;
    }

    public Page<HR> searchByCompany(Integer page, Integer size, String keyword) {
        Pageable pageable = PageRequest.of(page - 1, size);
        if (Objects.isNull(keyword) || keyword.isBlank())
            keyword = "";
        Page<HR> pageHr = hrRepo.searchByCompany(keyword ,pageable);
        pageHr.getContent().forEach(admin -> admin.setPassword("đã che"));
        return pageHr;
    }
}
