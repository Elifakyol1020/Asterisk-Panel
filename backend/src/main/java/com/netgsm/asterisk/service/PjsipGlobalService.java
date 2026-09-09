package com.netgsm.asterisk.service;

import com.netgsm.asterisk.exception.PlatformException;
import jakarta.validation.constraints.*;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.*;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@PreAuthorize("hasRole('SUPER_ADMIN')")
public class PjsipGlobalService {
    private final JdbcTemplate jdbc;
    private static final String COLUMNS = "id, user_agent, max_forwards, default_from_user, default_realm, keep_alive_interval, endpoint_identifier_order, contact_expiration_check_interval, default_voicemail_extension";

    public record Settings(
        @Size(max=40) @Pattern(regexp="[^\\r\\n\\x00]*") String userAgent,
        @Min(1) @Max(255) Integer maxForwards,
        @Size(max=40) @Pattern(regexp="[a-zA-Z0-9_.!~*'()%+\\-]*") String defaultFromUser,
        @Size(max=40) @Pattern(regexp="[a-zA-Z0-9_.\\-]*") String defaultRealm,
        @Min(0) @Max(2147483647) Integer keepAliveInterval,
        @Size(max=40) @Pattern(regexp="(?:[a-z_]+(?:,[a-z_]+)*)?") String endpointIdentifierOrder,
        @Min(1) @Max(2147483647) Integer contactExpirationCheckInterval,
        @Size(max=40) @Pattern(regexp="[a-zA-Z0-9_*#+.\\-]*") String defaultVoicemailExtension) { }
    public record View(String id, String revision, Settings settings) { }
    public record Update(@NotBlank String revision, @NotNull @jakarta.validation.Valid Settings settings) { }

    @Transactional(readOnly=true)
    public View get() { requireTable(); return read(); }

    @Transactional
    public View save(Update request) {
        requireTable();
        // Also serializes the first INSERT when the table is empty.
        jdbc.execute("LOCK TABLE public.ps_globals IN SHARE ROW EXCLUSIVE MODE");
        View before = read();
        if (!before.revision().equals(request.revision()))
            throw new PlatformException(409,"GLOBAL_SETTINGS_CHANGED","Ayarlar başka bir işlemde değişti. Yeniden yükleyip tekrar deneyin.");
        var s = request.settings();
        if (before.id() == null) jdbc.update("INSERT INTO public.ps_globals (id) VALUES (?)", "global");
        String id = before.id() == null ? "global" : before.id();
        jdbc.update("""
            UPDATE public.ps_globals SET user_agent=?, max_forwards=?, default_from_user=?, default_realm=?,
            keep_alive_interval=?, endpoint_identifier_order=?, contact_expiration_check_interval=?, default_voicemail_extension=? WHERE id=?
            """, blank(s.userAgent()), s.maxForwards(), blank(s.defaultFromUser()), blank(s.defaultRealm()),
            s.keepAliveInterval(), blank(s.endpointIdentifierOrder()), s.contactExpirationCheckInterval(), blank(s.defaultVoicemailExtension()), id);
        return read();
    }

    private void requireTable() {
        if (jdbc.queryForObject("SELECT to_regclass('public.ps_globals') IS NOT NULL", Boolean.class) != Boolean.TRUE)
            throw new PlatformException(503,"GLOBALS_SCHEMA_MISSING","public.ps_globals bulunamadı. Asterisk Alembic migration işlemini tamamlayın.");
    }
    private View read() {
        var rows = jdbc.queryForList("SELECT " + COLUMNS + " FROM public.ps_globals ORDER BY id LIMIT 2");
        if (rows.size() > 1) throw new PlatformException(409,"MULTIPLE_GLOBALS","ps_globals tablosunda birden fazla kayıt var. Asterisk için tek global kayıt bırakılmalıdır.");
        if (rows.isEmpty()) return new View(null,"empty",new Settings(null,null,null,null,null,null,null,null));
        var r = rows.getFirst();
        var s = new Settings((String)r.get("user_agent"), integer(r.get("max_forwards")), (String)r.get("default_from_user"),
            (String)r.get("default_realm"), integer(r.get("keep_alive_interval")), (String)r.get("endpoint_identifier_order"),
            integer(r.get("contact_expiration_check_interval")), (String)r.get("default_voicemail_extension"));
        try {
            var revision = HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(r.toString().getBytes(StandardCharsets.UTF_8)));
            return new View((String)r.get("id"), revision, s);
        } catch (java.security.NoSuchAlgorithmException ex) { throw new IllegalStateException(ex); }
    }
    private static Integer integer(Object value) { return value == null ? null : ((Number)value).intValue(); }
    private static String blank(String value) { return value == null || value.isBlank() ? null : value; }
}
