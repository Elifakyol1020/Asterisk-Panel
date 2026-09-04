package com.netgsm.asterisk.service;

import com.netgsm.asterisk.dto.IvrAudioResponse;
import com.netgsm.asterisk.exception.BusinessRuleException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Locale;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('SUPER_ADMIN', 'TENANT_ADMIN')")
public class IvrAudioService {
    private final CurrentUserService current;

    @Value("${app.asterisk.sounds-path:/var/lib/asterisk/sounds}")
    private String soundsPath;

    public IvrAudioResponse upload(Long requestedTenantId, MultipartFile file) {
        Long tenantId = current.tenantForCreate(requestedTenantId);
        if (file == null || file.isEmpty()) throw new BusinessRuleException("A WAV audio file is required");
        if (file.getSize() > 20L * 1024 * 1024) throw new BusinessRuleException("Audio file must not exceed 20 MB");
        String original = file.getOriginalFilename() == null ? "audio.wav" : file.getOriginalFilename();
        String lower = original.toLowerCase(Locale.ROOT);
        if (!lower.endsWith(".wav")) throw new BusinessRuleException("Only WAV audio files are supported");
        String contentType = file.getContentType();
        if (contentType != null && !contentType.equals("audio/wav") && !contentType.equals("audio/x-wav")
                && !contentType.equals("audio/wave") && !contentType.equals("application/octet-stream")) {
            throw new BusinessRuleException("Only WAV audio files are supported");
        }
        try (var headerInput = file.getInputStream()) {
            byte[] header = headerInput.readNBytes(12);
            if (header.length < 12 || header[0] != 'R' || header[1] != 'I' || header[2] != 'F' || header[3] != 'F'
                    || header[8] != 'W' || header[9] != 'A' || header[10] != 'V' || header[11] != 'E') {
                throw new BusinessRuleException("Uploaded file is not a valid WAV container");
            }
        } catch (IOException ex) {
            throw new BusinessRuleException("Audio file could not be read");
        }

        String id = UUID.randomUUID().toString().replace("-", "");
        String asteriskName = "custom/tenant" + tenantId + "/" + id;
        Path tenantDirectory = Path.of(soundsPath).toAbsolutePath().normalize()
                .resolve("custom").resolve("tenant" + tenantId).normalize();
        Path destination = tenantDirectory.resolve(id + ".wav").normalize();
        if (!destination.startsWith(tenantDirectory)) throw new BusinessRuleException("Invalid audio path");
        try {
            Files.createDirectories(tenantDirectory);
            try (var input = file.getInputStream()) {
                Files.copy(input, destination, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException ex) {
            throw new BusinessRuleException("Audio file could not be stored");
        }
        return new IvrAudioResponse(asteriskName, original);
    }
}
