package io.vibe.llm.common.attach.service;

import io.vibe.llm.common.attach.domain.Attach;
import io.vibe.llm.common.attach.exception.AttachException;
import jakarta.annotation.PostConstruct;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.FileSystemUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.Normalizer;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.function.Predicate;

/**
 * @since       2026.10.01
 * @author      preah
 * @description attach service
 **********************************************************************************************************************/
@Service
public class AttachService {

    @Value("${spring.servlet.multipart.location}")
    private String tempLocation;

    @Value("${property.attach.store-location}")
    private String storeLocation;

    private Path tmpPath;
    private Path documentPath;

    // docling 이 변환 가능한 확장자만 허용한다.
    private final List<String> allowedExtensions = Arrays.asList(
            "pdf", "docx", "pptx", "xlsx", "md", "adoc", "html", "htm",
            "csv", "png", "jpg", "jpeg", "tiff", "bmp", "webp");

    @PostConstruct
    @SneakyThrows
    public void initialize() {
        tmpPath      = Paths.get(tempLocation);
        documentPath = Paths.get(storeLocation).resolve("document");

        Files.createDirectories(tmpPath);
        Files.createDirectories(documentPath);

        FileSystemUtils.deleteRecursively(tmpPath);
        Files.createDirectories(tmpPath);
    }

    @SneakyThrows
    private Attach add(MultipartFile multipartFile, Path storePath) {
        LocalDate now       = LocalDate.now();
        String    extension = StringUtils.substringAfterLast(multipartFile.getOriginalFilename(), ".");
        String    name      = UUID.randomUUID() + "." + extension;
        Path      path      = Paths.get(String.valueOf(now.getYear())).resolve(String.valueOf(now.getMonthValue())).resolve(String.valueOf(now.getDayOfMonth()));
        Path      childPath = storePath.resolve(path);

        Files.createDirectories(childPath);
        multipartFile.transferTo(childPath.resolve(name).toFile());

        return Attach.builder()
                .path(path.toString())
                .name(name)
                .originalName(Normalizer.normalize(multipartFile.getOriginalFilename(), Normalizer.Form.NFC))
                .size(multipartFile.getSize())
                .build();
    }

    private void remove(Attach attach, Path storePath) {
        try {
            Files.deleteIfExists(storePath.resolve(attach.getPath()).resolve(attach.getName()));
        } catch (IOException ignored) {
        }
    }

    public Attach add(MultipartFile multipartFile) {
        String fileExtension = StringUtils.substringAfterLast(multipartFile.getOriginalFilename(), ".").toLowerCase();
        if (allowedExtensions.stream().noneMatch(Predicate.isEqual(fileExtension))) {
            throw new AttachException("docling 이 처리할 수 없는 확장자입니다: " + fileExtension);
        }

        return this.add(multipartFile, documentPath);
    }

    public void remove(Attach attach) {
        this.remove(attach, documentPath);
    }
}
