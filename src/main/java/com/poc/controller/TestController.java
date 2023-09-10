package com.poc.controller;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.ResourceUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/test")
@Slf4j
public class TestController {

    @Value("${app.file-path}")
    private String scriptFilepath;

    @SneakyThrows
    @GetMapping("/run/{arg}")
    public String runScript(@PathVariable String arg) {
        Instant startTime = Instant.now();
        log.info("Entered into TestController");
        try {
            File file = ResourceUtils.getFile(scriptFilepath);
            Runtime.getRuntime().exec("chmod -R 777 " + file.getAbsolutePath()).waitFor();
            log.info("Given Permission to file {}", file.getAbsolutePath());
            String[] commands = new String[]{file.getAbsolutePath(), arg};
            log.info("Running command with {}", Arrays.deepToString(commands));
            Process process = Runtime.getRuntime().exec(commands);
            int resultStatus = process.waitFor();
            Instant endTime = Instant.now();
            String status = logAndReturnStatus(process, resultStatus, file.getName());
            log.info("Shell script execution completed with status : {} in {} millis", status, Duration.between(startTime, endTime).toMillis());
            return status;
        } catch (Exception ex) {
            log.error(ex.getMessage());
            return "Status: FAILURE";
        }

    }

    private String logAndReturnStatus(Process process, int resultStatus, String fileName) throws IOException {
        Logger logger = LoggerFactory.getLogger(fileName);
        if (resultStatus == 0) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            logger.info(reader.readLine());
            return "Status: SUCCESS";
        } else {
            BufferedReader errReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
            logger.error(errReader.readLine());
            return "Status: FAILURE";
        }
    }
}
