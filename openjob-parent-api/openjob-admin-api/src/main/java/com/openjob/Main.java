package com.openjob;


import com.openjob.admin.setting.SettingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;

import java.util.Properties;

@SpringBootApplication
@EnableJpaRepositories(basePackages = "com.openjob.admin.*")
@ComponentScan(basePackages = "com.openjob.admin.*")
public class Main {
    @Autowired
    private SettingService settingService;

    public static void main(String[] args) {
        SpringApplication.run(Main.class, args);
    }

    @Bean
    public BCryptPasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CommonsMultipartResolver multipartResolver(){
        CommonsMultipartResolver multipartResolver = new CommonsMultipartResolver();
        multipartResolver.setDefaultEncoding("UTF-8");
        return multipartResolver;
    }

    @Bean
    public JavaMailSender getJavaMailSender() {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost(settingService.getValue("MAIL_HOST"));
        mailSender.setPort(Integer.parseInt(settingService.getValue("MAIL_PORT")));

        mailSender.setUsername(settingService.getValue("MAIL_USERNAME"));
        mailSender.setPassword(settingService.getValue("MAIL_PASSWORD"));

        Properties props = mailSender.getJavaMailProperties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.debug", "true");
        props.put("mail.smtp.allow8bitmime", "true");
        props.put("mail.smtps.allow8bitmime", "true");

        return mailSender;
    }

}
