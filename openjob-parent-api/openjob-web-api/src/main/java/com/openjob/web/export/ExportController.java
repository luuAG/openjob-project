package com.openjob.web.export;

import com.openjob.common.model.Job;
import com.openjob.common.response.MessageResponse;
import com.openjob.web.dto.ExportCvDTO;
import com.openjob.web.dto.ExportDTO;
import com.openjob.web.job.JobService;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
@RequestMapping("/export")
public class ExportController {
    private final JobService jobService;

    @PostMapping("/accepted-cv")
    public ResponseEntity<MessageResponse> exportAcceptedCv(@Valid @RequestBody ExportDTO body) throws IOException {
        Optional<Job> jobOptional = jobService.getById(body.getJobId());
        if (jobOptional.isEmpty())
            throw new IllegalArgumentException("Job not found for ID: " + body.getJobId());
        Job job = jobOptional.get();
        String cvFrom;
        String jobTitle = job.getTitle();
        String specialization = job.getSpecialization().getName();
        String date = job.getCreatedAt().toString();
        List<ExportCvDTO> listCvDto;

        if (Objects.nonNull(body.getAppliedCVs())){
            cvFrom = "Applied CVs";
            listCvDto = body.getAppliedCVs();
        } else {
            cvFrom = "Matched CVs";
            listCvDto = body.getMatchedCVs();
        }
        // Write to file
        try (InputStream is = new FileInputStream(new File("").getAbsolutePath() + "/openjob-web-api/src/main/resources/template/export_cv_template.xlsx")){
            Workbook wb = WorkbookFactory.create(is);
            Sheet sheet = wb.getSheetAt(0);
            //Write job info
            Row row = sheet.getRow(0);
            Cell cell = row.createCell(2);
            cell.setCellValue(cvFrom);
            row = sheet.getRow(1);
            cell = row.createCell(2);
            cell.setCellValue(jobTitle);
            row = sheet.getRow(2);
            cell = row.createCell(2);
            cell.setCellValue(specialization);
            row = sheet.getRow(3);
            cell = row.createCell(2);
            cell.setCellValue(date);
            // write list cv info
            int rowCount = 6;
            for (ExportCvDTO obj : listCvDto){
                row = sheet.createRow(rowCount++);
                cell = row.createCell(0);
                cell.setCellValue(obj.getFirstName());
                cell = row.createCell(1);
                cell.setCellValue(obj.getLastName());
                cell = row.createCell(2);
                cell.setCellValue(obj.getEmail());
                cell = row.createCell(3);
                cell.setCellValue(obj.getPhone());
                cell = row.createCell(4);
                cell.setCellValue(obj.getGender());
                cell = row.createCell(5);
                cell.setCellValue(obj.getUrl());
            }

            for (int i=0; i<6; i++){
                sheet.autoSizeColumn(i);
            }
            // Save the Workbook
            String filename = "/exported-cv/Accepted_CV_" + new SimpleDateFormat("dd-mm-yyyy").format(new Date()) + ".xlsx" ;
            OutputStream os = new FileOutputStream(new File("..").getAbsolutePath() +filename);
            wb.write(os);
            os.close();
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return ResponseEntity.internalServerError().body(new MessageResponse("Error occured when exporting data"));
        }



        return ResponseEntity.ok(new MessageResponse("Data exported"));
    }
}