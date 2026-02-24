package com.grosso.chat.file;

import io.micrometer.common.util.StringUtils;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Slf4j
@NoArgsConstructor
public class FileUtils {

    public static byte[] readFileFromLocation(String fileURL) {
        if(StringUtils.isBlank(fileURL))
            return new byte[0];

        try {
            Path path = Path.of(fileURL);
            return Files.readAllBytes(path);
        } catch (IOException e) {
            log.warn("No file found in the path {}", fileURL);
        }

        return new byte[0];
    }
}
