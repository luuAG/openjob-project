package com.openjob.admin.company;

import com.openjob.admin.dto.CompanyHeadhunterRequestDTO;
import com.openjob.admin.dto.CompanyHeadhunterResponseDTO;
import com.openjob.admin.dto.CompanyPaginationDTO;
import com.openjob.admin.dto.UserPaginationDTO;
import com.openjob.admin.exception.UserNotFoundException;
import com.openjob.common.model.Company;
import com.openjob.common.model.Role;
import com.openjob.common.model.User;
import com.openjob.common.response.MessageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.mail.javamail.MimeMessagePreparator;
import org.springframework.web.bind.annotation.*;

import java.sql.SQLException;
import java.util.Objects;
import java.util.Optional;


@RestController
@RequiredArgsConstructor
public class CompanyHrController {
    private final HrService hrService;
    private final CompanyService companyService;
    private final JavaMailSender mailSender;

    @GetMapping(path = "/hr/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getHr(@PathVariable("id") String id) {
        if (Objects.isNull(id)){
            throw new IllegalArgumentException("ID is null");
        }
        Optional<User> hrOptional = hrService.get(id);
        if (hrOptional.isPresent())
            return ResponseEntity.ok(hrOptional.get());
        return ResponseEntity.badRequest().body(null);
    }

    @GetMapping(path = "/hrs", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<UserPaginationDTO> getHrs(
            @RequestParam Integer page,
            @RequestParam Integer size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Boolean byCompany){
        Page<User> pageHr;
        if (Objects.nonNull(byCompany) && byCompany)
            pageHr = hrService.searchByCompany(page, size, keyword);
        else
            pageHr = hrService.searchByKeyword(page, size, keyword);
        return ResponseEntity.ok(new UserPaginationDTO(
                pageHr.getContent(),
                pageHr.getTotalPages(),
                pageHr.getTotalElements()
        ));
    }

    @PostMapping(path = "/hr/activate/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<MessageResponse> activateHr(@PathVariable String id) throws SQLException, UserNotFoundException {
            Optional<User> optionalHR = hrService.get(id);
            if (optionalHR.isPresent()){
                User existingHr = optionalHR.get();
                existingHr.setIsActive(true);
                hrService.saveWithoutPassword(existingHr);
            } else {
                throw new UserNotFoundException("HR user not found with ID: " + id);
            }
            return ResponseEntity.ok(new MessageResponse("HR user is activated, ID: " + id));
    }

    @DeleteMapping(path = "/hr/deactivate/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<MessageResponse> deactivateHr(@PathVariable String id) throws SQLException, UserNotFoundException {
        Optional<User> optionalHR = hrService.get(id);
        if (optionalHR.isPresent()){
            User existingHr = optionalHR.get();
            existingHr.setIsActive(false);
            hrService.saveWithoutPassword(existingHr);
        } else {
            throw new UserNotFoundException("HR user not found with ID: " + id);
        }
        return ResponseEntity.ok(new MessageResponse("HR user is deactivated, ID: " + id));
    }


    @PostMapping(path = "/company/create-head-hunter", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CompanyHeadhunterResponseDTO> createHeadHunter(@RequestBody CompanyHeadhunterRequestDTO body) throws SQLException {
        User hr = body.getHeadHunter();
        Company company = new Company();
        company.setName(body.getCompanyName());

        if (Objects.isNull(hr)){
            throw new IllegalArgumentException("Head hunter is null");
        }
        if (Objects.isNull(hr.getEmail()) || hr.getEmail().isBlank()){
            throw new IllegalArgumentException("Head hunter's email is null or blank");
        }
        if (Objects.isNull(company.getName()) || company.getName().isBlank()){
            throw new IllegalArgumentException("Company name is null or blank");
        }

        company.setHeadHunter(hr);

        hr.setCompany(company);
        hr.setRole(Role.HEAD_HUNTER);
        hr.setPassword("12345678");
        User savedHr = hrService.save(hr);

        if (Objects.nonNull(savedHr)){
            MimeMessagePreparator message = mimeMessage -> {
                MimeMessageHelper message1 = new MimeMessageHelper(mimeMessage, true, "UTF-8");
                message1.setFrom("duongvannam2001@gmail.com");
                message1.setTo(savedHr.getEmail());
                message1.setSubject("Tài khoản OpenJob đã được tạo");
                message1.setText("Kính gửi Phòng tuyển dụng công ty "+company.getName()+", \n" +
                        "Tài khoản cho Quản lý bộ phận tuyển dụng ở OpenJob: \n" +
                        "Username: "+ savedHr.getEmail() + "\n" +
                        "Password: 12345678\n" +
                        "Vui lòng đổi mật khẩu sau khi đăng nhập lần đầu!", true);
            };
            mailSender.send(message);
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(
                new CompanyHeadhunterResponseDTO(savedHr.getId(), savedHr.getCompany().getId())
        );
    }

    @GetMapping(path = "/companies", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CompanyPaginationDTO> getCompanies(
            @RequestParam Integer page,
            @RequestParam Integer size,
            @RequestParam(required = false) String keyword) {
        Page<Company> pageCompany = companyService.search(page, size, keyword);
        return ResponseEntity.ok(new CompanyPaginationDTO(
                pageCompany.getContent(),
                pageCompany.getTotalPages(),
                pageCompany.getTotalElements())
        );
    }
    @GetMapping(path = "/company/{companyId}/hrs", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<UserPaginationDTO> getHrsOfCompany(
            @RequestParam Integer page,
            @RequestParam Integer size,
            @PathVariable String companyId) {
        Page<User> pageHr = hrService.findByCompanyId(page, size, companyId);
        return ResponseEntity.ok(new UserPaginationDTO(
                pageHr.getContent(),
                pageHr.getTotalPages(),
                pageHr.getTotalElements())
        );
    }

}