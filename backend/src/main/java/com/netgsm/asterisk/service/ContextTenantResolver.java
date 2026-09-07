package com.netgsm.asterisk.service;
import com.netgsm.asterisk.dto.request.CdrInput;
import com.netgsm.asterisk.exception.PlatformException;
import com.netgsm.asterisk.repository.TenantRepository;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
@Service
@RequiredArgsConstructor
public class ContextTenantResolver implements TenantResolver {
    private final TenantRepository tenants;
    private static final Pattern CONTEXT = Pattern.compile("^tenant_([1-9][0-9]*)_.+$");
    private static final Pattern CHANNEL = Pattern.compile("^(?:PJSIP|SIP)/tenant([1-9][0-9]*)_.+$");
    public Long resolveTenant(CdrInput input) {
        Set<Long> candidates = new HashSet<>();
        extract(CONTEXT, input.context(), candidates);
        extract(CHANNEL, input.channel(), candidates);
        extract(CHANNEL, input.dstChannel(), candidates);
        if (candidates.size() != 1) throw rejected();
        Long id = candidates.iterator().next();
        if (!tenants.existsById(id)) throw rejected();
        return id;
    }
    private void extract(Pattern pattern, String value, Set<Long> candidates) {
        if (value == null) return;
        var matcher = pattern.matcher(value.trim());
        if (matcher.matches()) {
            try { candidates.add(Long.parseLong(matcher.group(1))); }
            catch (NumberFormatException ex) { throw rejected(); }
        }
    }
    private PlatformException rejected() { return new PlatformException(422, "CDR_TENANT_UNRESOLVED", "CDR tenant could not be resolved unambiguously"); }
}
