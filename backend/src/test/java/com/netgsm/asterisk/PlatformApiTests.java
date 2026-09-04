package com.netgsm.asterisk;

import com.netgsm.asterisk.service.JwtService;
import com.netgsm.asterisk.config.JwtProperties;
import com.netgsm.asterisk.entity.Endpoint;
import com.netgsm.asterisk.repository.EndpointRepository;
import com.netgsm.asterisk.entity.Ivr;
import com.netgsm.asterisk.repository.IvrOptionRepository;
import com.netgsm.asterisk.repository.IvrRepository;
import com.netgsm.asterisk.entity.Queue;
import com.netgsm.asterisk.repository.QueueMemberRepository;
import com.netgsm.asterisk.repository.QueueRepository;
import com.netgsm.asterisk.repository.ExtensionRepository;
import com.netgsm.asterisk.entity.Tenant;
import com.netgsm.asterisk.enums.TenantStatus;
import com.netgsm.asterisk.repository.TenantRepository;
import com.netgsm.asterisk.enums.Role;
import com.netgsm.asterisk.entity.User;
import com.netgsm.asterisk.repository.UserRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = {
        "spring.config.import=",
        "spring.datasource.url=jdbc:h2:mem:api;MODE=PostgreSQL;DB_CLOSE_DELAY=-1",
        "spring.datasource.driver-class-name=org.h2.Driver", "spring.datasource.username=sa", "spring.datasource.password=",
        "spring.sql.init.mode=always", "spring.sql.init.schema-locations=classpath:schema.sql,classpath:test-realtime-schema.sql",
        "spring.jpa.hibernate.ddl-auto=validate",
        "app.jwt.secret=test-only-secret-that-is-at-least-32-bytes", "app.jwt.expiration=60000",
        "app.cors.allowed-origins=https://test.invalid"
})
@AutoConfigureMockMvc
class PlatformApiTests {
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;
    @Autowired JwtService jwt;
    @Autowired PasswordEncoder passwords;
    @Autowired TenantRepository tenants;
    @Autowired UserRepository users;
    @Autowired EndpointRepository endpoints;
    @Autowired QueueRepository queues;
    @Autowired QueueMemberRepository members;
    @Autowired IvrRepository ivrs;
    @Autowired IvrOptionRepository options;
    @Autowired ExtensionRepository extensions;
    @Autowired com.netgsm.asterisk.repository.TrunkRepository trunks;
    @Autowired com.netgsm.asterisk.repository.DialplanRepository dialplans;
    @Autowired org.springframework.context.ApplicationContext context;
    @Autowired com.netgsm.asterisk.asterisk.realtime.repository.PsEndpointRepository psEndpoints;
    @Autowired com.netgsm.asterisk.asterisk.realtime.repository.PsAuthRepository psAuths;
    @Autowired com.netgsm.asterisk.asterisk.realtime.repository.PsAorRepository psAors;
    @Autowired com.netgsm.asterisk.asterisk.realtime.repository.PsEndpointIdIpRepository psIdentifies;
    @Autowired com.netgsm.asterisk.asterisk.realtime.repository.AsteriskQueueRepository realtimeQueues;
    @Autowired com.netgsm.asterisk.asterisk.realtime.repository.AsteriskQueueMemberRepository realtimeMembers;
    @Autowired com.netgsm.asterisk.asterisk.realtime.repository.AsteriskExtensionRepository realtimeExtensions;
    Tenant first, second;
    User admin, firstAdmin, secondAdmin;
    String passwordHash;

    @BeforeEach void fixtures() {
        members.deleteAll(); options.deleteAll(); extensions.deleteAll(); queues.deleteAll();
        ivrs.deleteAll(); endpoints.deleteAll(); trunks.deleteAll(); dialplans.deleteAll(); users.deleteAll(); tenants.deleteAll();
        realtimeMembers.deleteAll(); realtimeQueues.deleteAll(); realtimeExtensions.deleteAll();
        psIdentifies.deleteAll(); psEndpoints.deleteAll(); psAuths.deleteAll(); psAors.deleteAll();
        first = tenant("first"); second = tenant("second");
        passwordHash = passwords.encode("test-password-123");
        admin = user("root", null, Role.SUPER_ADMIN);
        firstAdmin = user("first-admin", first.getId(), Role.TENANT_ADMIN);
        secondAdmin = user("second-admin", second.getId(), Role.TENANT_ADMIN);
    }
    @Test void loginIssuesTenantClaimsAndNeverExposesPasswordHash() throws Exception {
        var result = mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content(json.writeValueAsString(Map.of("email", "FIRST-ADMIN@test.invalid", "password", "test-password-123"))))
                .andExpect(status().isOk()).andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.user").doesNotExist())
                .andExpect(jsonPath("$.user.passwordHash").doesNotExist()).andReturn();
        String token = json.readTree(result.getResponse().getContentAsString()).get("accessToken").asString();
        assertThat(json.readTree(result.getResponse().getContentAsString()).size()).isEqualTo(2);
        assertThat(jwt.decode(token).getClaimAsString("role")).isEqualTo("TENANT_ADMIN");
        assertThat(jwt.decode(token).getClaimAsString("userId")).isEqualTo(firstAdmin.getId().toString());
        assertThat(jwt.decode(token).getClaimAsString("tenantId")).isEqualTo(first.getId().toString());
    }
    @Test void usernameCannotBeUsedForLogin() throws Exception {
        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content(json.writeValueAsString(Map.of("username", "root", "password", "test-password-123"))))
                .andExpect(status().isBadRequest());
        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content(json.writeValueAsString(Map.of("email", "root", "password", "test-password-123"))))
                .andExpect(status().isBadRequest());
    }
    @Test void emailIsUniqueAcrossTenantsOnCreateAndUpdate() throws Exception {
        var duplicateEmail = firstAdmin.getEmail().toUpperCase(java.util.Locale.ROOT);
        var body = json.writeValueAsString(Map.of("username", "another-admin", "email", duplicateEmail,
                "password", "test-password-123", "enabled", true));
        mvc.perform(post("/api/admin/tenants/" + second.getId() + "/users")
                .header("Authorization", bearer(admin)).contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isConflict());
        mvc.perform(put("/api/admin/users/" + secondAdmin.getId())
                .header("Authorization", bearer(admin)).contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isConflict());
        assertThat(users.findById(secondAdmin.getId()).orElseThrow().getEmail()).isEqualTo("second-admin@test.invalid");
    }
    @Test void wrongPasswordAndMissingUserHaveSameResponse() throws Exception {
        for (String email : new String[]{"first-admin@test.invalid", "missing@test.invalid"})
            mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                    .content(json.writeValueAsString(Map.of("email", email, "password", "wrong"))))
                    .andExpect(status().isUnauthorized()).andExpect(jsonPath("$.message").value("Invalid credentials"));
    }
    @Test void anonymousAndTenantAdminCannotManageTenants() throws Exception {
        mvc.perform(get("/api/admin/tenants")).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/admin/tenants").header("Authorization", bearer(firstAdmin))).andExpect(status().isForbidden());
        mvc.perform(get("/api/admin/tenants").header("Authorization", bearer(admin))).andExpect(status().isOk());
    }
    @Test void tenantSortingRejectsSwaggerPlaceholderAndAcceptsValidFields() throws Exception {
        for (String sort : new String[]{"string", "[\"string\"]", "[\"name,asc\"]", "name,wrong"}) {
            mvc.perform(get("/api/admin/tenants").param("sort", sort).header("Authorization", bearer(admin)))
                    .andExpect(status().isBadRequest()).andExpect(jsonPath("$.error").value("INVALID_SORT"));
        }
        mvc.perform(get("/api/admin/tenants").param("sort", "name,desc").header("Authorization", bearer(admin)))
                .andExpect(status().isOk()).andExpect(jsonPath("$.content[0].name").value("second"));
    }
    @Test void tenantCodeIsNormalizedFromFriendlyInput() throws Exception {
        var body = json.writeValueAsString(Map.of("name", "Net GSM A.Ş.", "code", "Net GSM A.Ş.", "status", "ACTIVE"));
        mvc.perform(post("/api/admin/tenants").header("Authorization", bearer(admin))
                .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Net GSM A.Ş."))
                .andExpect(jsonPath("$.code").value("net_gsm_a_s"));
        mvc.perform(post("/api/admin/tenants").header("Authorization", bearer(admin))
                .contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsString(Map.of("name", "Invalid", "code", "!", "status", "ACTIVE"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("INVALID_TENANT_CODE"));
    }
    @Test void methodSecurityRejectsUnassignedRolesForPbxServices() {
        var security = org.springframework.security.core.context.SecurityContextHolder.createEmptyContext();
        security.setAuthentication(new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                "unassigned", null, java.util.List.of(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_VIEWER"))));
        org.springframework.security.core.context.SecurityContextHolder.setContext(security);
        var page = org.springframework.data.domain.PageRequest.of(0, 20);
        try {
            java.util.List<Runnable> calls = java.util.List.of(
                () -> context.getBean(com.netgsm.asterisk.service.EndpointService.class).list(null, page),
                () -> context.getBean(com.netgsm.asterisk.service.TrunkService.class).list(null, page),
                () -> context.getBean(com.netgsm.asterisk.service.QueueService.class).list(null, page),
                () -> context.getBean(com.netgsm.asterisk.service.QueueMemberService.class).list(1L, page),
                () -> context.getBean(com.netgsm.asterisk.service.IvrService.class).list(null, page),
                () -> context.getBean(com.netgsm.asterisk.service.IvrOptionService.class).list(1L, page),
                () -> context.getBean(com.netgsm.asterisk.service.ExtensionService.class).list(null, page),
                () -> context.getBean(com.netgsm.asterisk.service.DialplanService.class).list(null, page),
                () -> context.getBean(com.netgsm.asterisk.service.EndpointService.class).create(null),
                () -> context.getBean(com.netgsm.asterisk.service.EndpointService.class).update(1L, null),
                () -> context.getBean(com.netgsm.asterisk.service.EndpointService.class).delete(1L));
            calls.forEach(call -> org.assertj.core.api.Assertions.assertThatThrownBy(call::run)
                    .isInstanceOf(org.springframework.security.access.AccessDeniedException.class));
        } finally {
            org.springframework.security.core.context.SecurityContextHolder.clearContext();
        }
    }
    @Test void tenantAdminCannotCallAdminServicesDirectly() {
        var security = org.springframework.security.core.context.SecurityContextHolder.createEmptyContext();
        security.setAuthentication(new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                "tenant-admin", null, java.util.List.of(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_TENANT_ADMIN"))));
        org.springframework.security.core.context.SecurityContextHolder.setContext(security);
        try {
            org.assertj.core.api.Assertions.assertThatThrownBy(() -> context.getBean(com.netgsm.asterisk.service.TenantService.class).get(first.getId()))
                    .isInstanceOf(org.springframework.security.access.AccessDeniedException.class);
            org.assertj.core.api.Assertions.assertThatThrownBy(() -> context.getBean(com.netgsm.asterisk.service.UserService.class).get(firstAdmin.getId()))
                    .isInstanceOf(org.springframework.security.access.AccessDeniedException.class);
        } finally {
            org.springframework.security.core.context.SecurityContextHolder.clearContext();
        }
    }
    @Test void tokenCannotBeTamperedWithOrExpired() throws Exception {
        String token = jwt.issue(firstAdmin);
        int signature = token.lastIndexOf('.') + 1;
        token = token.substring(0, signature) + (token.charAt(signature) == 'A' ? 'B' : 'A') + token.substring(signature + 1);
        mvc.perform(get("/api/ivrs").header("Authorization", "Bearer " + token)).andExpect(status().isUnauthorized());
        var key = new javax.crypto.spec.SecretKeySpec("test-only-secret-that-is-at-least-32-bytes".getBytes(java.nio.charset.StandardCharsets.UTF_8), "HmacSHA256");
        var encoder = new org.springframework.security.oauth2.jwt.NimbusJwtEncoder(new com.nimbusds.jose.jwk.source.ImmutableSecret<>(key));
        var claims = org.springframework.security.oauth2.jwt.JwtClaimsSet.builder().issuer("asterisk-platform")
                .subject(firstAdmin.getId().toString()).issuedAt(java.time.Instant.now().minusSeconds(60))
                .expiresAt(java.time.Instant.now().minusSeconds(30)).build();
        String expired = encoder.encode(org.springframework.security.oauth2.jwt.JwtEncoderParameters.from(
                org.springframework.security.oauth2.jwt.JwsHeader.with(org.springframework.security.oauth2.jose.jws.MacAlgorithm.HS256).build(), claims)).getTokenValue();
        mvc.perform(get("/api/ivrs").header("Authorization", "Bearer " + expired)).andExpect(status().isUnauthorized());
    }
    @Test void disabledUserAndInactiveTenantInvalidateExistingTokens() throws Exception {
        String token = bearer(firstAdmin);
        firstAdmin.setEnabled(false); firstAdmin = users.saveAndFlush(firstAdmin);
        mvc.perform(get("/api/ivrs").header("Authorization", token)).andExpect(status().isUnauthorized());
        firstAdmin.setEnabled(true); users.saveAndFlush(firstAdmin);
        first.setStatus(TenantStatus.INACTIVE); tenants.saveAndFlush(first);
        mvc.perform(get("/api/ivrs").header("Authorization", token)).andExpect(status().isUnauthorized());
    }
    @Test void tenantCannotReadUpdateOrDeleteAnotherTenantEndpoint() throws Exception {
        Endpoint other = endpoint(second.getId());
        mvc.perform(get("/api/endpoints/" + other.getId()).header("Authorization", bearer(firstAdmin))).andExpect(status().isNotFound());
        mvc.perform(put("/api/endpoints/" + other.getId()).header("Authorization", bearer(firstAdmin))
                .contentType(MediaType.APPLICATION_JSON).content(endpointRequest(first.getId())))
                .andExpect(status().isNotFound());
        mvc.perform(delete("/api/endpoints/" + other.getId()).header("Authorization", bearer(firstAdmin))).andExpect(status().isNotFound());
        assertThat(endpoints.existsById(other.getId())).isTrue();
    }
    @Test void tenantListIgnoresSpoofedTenantFilterButSuperAdminCanFilter() throws Exception {
        endpoint(first.getId()); endpoint(second.getId());
        mvc.perform(get("/api/endpoints").param("tenantId", second.getId().toString()).header("Authorization", bearer(firstAdmin)))
                .andExpect(status().isOk()).andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].tenantId").value(first.getId()));
        mvc.perform(get("/api/endpoints").header("Authorization", bearer(admin)))
                .andExpect(status().isOk()).andExpect(jsonPath("$.totalElements").value(2));
    }
    @Test void createIgnoresSpoofedBodyTenantAndUpdateDoesNotTransferOwnership() throws Exception {
        String body = ivrRequest(second.getId(), "menu");
        var result = mvc.perform(post("/api/ivrs").header("Authorization", bearer(firstAdmin)).contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.tenantId").value(first.getId())).andReturn();
        long id = json.readTree(result.getResponse().getContentAsString()).get("id").asLong();
        mvc.perform(put("/api/ivrs/" + id).header("Authorization", bearer(firstAdmin)).contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk()).andExpect(jsonPath("$.tenantId").value(first.getId()));
    }
    @Test void duplicateNamesAreScopedToTenant() throws Exception {
        String body = ivrRequest(first.getId(), "menu");
        mvc.perform(post("/api/ivrs").header("Authorization", bearer(firstAdmin)).contentType(MediaType.APPLICATION_JSON).content(body)).andExpect(status().isCreated());
        mvc.perform(post("/api/ivrs").header("Authorization", bearer(firstAdmin)).contentType(MediaType.APPLICATION_JSON).content(body)).andExpect(status().isConflict());
        mvc.perform(post("/api/ivrs").header("Authorization", bearer(secondAdmin)).contentType(MediaType.APPLICATION_JSON).content(body)).andExpect(status().isCreated());
    }
    @Test void endpointCrudPersistsInDatabase() throws Exception {
        assertDatabaseCrud("/api/endpoints", endpointRequest(second.getId()), endpoints);
    }
    @Test void trunkCrudPersistsInDatabase() throws Exception {
        assertDatabaseCrud("/api/trunks", json.writeValueAsString(Map.of(
                "name", "provider", "host", "192.0.2.10", "port", 5060,
                "username", "sip-user", "transport", "transport-udp", "enabled", true,
                "password", "sip-password-123")), trunks);
    }
    @Test void queueCrudPersistsInDatabase() throws Exception {
        assertDatabaseCrud("/api/queues", json.writeValueAsString(Map.of(
                "name", "support", "strategy", "ringall", "timeout", 20, "retry", 5,
                "wrapupTime", 0, "maxLength", 0, "musicOnHold", "default", "enabled", true)), queues);
    }
    @Test void dialplanCrudPersistsInDatabase() throws Exception {
        assertDatabaseCrud("/api/dialplans", json.writeValueAsString(Map.of(
                "extension", "2001", "priority", 1, "application", "Answer",
                "applicationData", "", "enabled", true)), dialplans);
    }
    @Test void asteriskRealtimeProjectionIsWrittenForPbxCrud() throws Exception {
        mvc.perform(post("/api/endpoints").header("Authorization", bearer(firstAdmin))
                .contentType(MediaType.APPLICATION_JSON).content(endpointRequest(first.getId())))
                .andExpect(status().isCreated());
        assertThat(psAors.findById("tenant" + first.getId() + "_1001")).isPresent();
        assertThat(psAuths.findById("tenant" + first.getId() + "_1001_auth").orElseThrow().getUsername()).isEqualTo("tenant" + first.getId() + "_1001");
        assertThat(psAuths.findById("tenant" + first.getId() + "_1001_auth").orElseThrow().getPassword()).isEqualTo("sip-password-123");
        var realtimeEndpoint = psEndpoints.findById("tenant" + first.getId() + "_1001").orElseThrow();
        assertThat(realtimeEndpoint.getAors()).isEqualTo("tenant" + first.getId() + "_1001");
        assertThat(realtimeEndpoint.getContext()).isEqualTo("tenant_" + first.getId() + "_internal");
        assertThat(realtimeExtensions.findAllByContextAndExtenOrderByPriorityAsc("tenant_" + first.getId() + "_internal", "1001"))
                .extracting("app").containsExactly("NoOp", "Dial", "Hangup");

        mvc.perform(post("/api/trunks").header("Authorization", bearer(firstAdmin))
                .contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsString(Map.of(
                        "name", "eren", "host", "192.0.2.20", "port", 5060, "username", "sip-user",
                        "transport", "transport-udp", "enabled", true, "password", "sip-password-123"))))
                .andExpect(status().isCreated());
        assertThat(psEndpoints.findById("tenant" + first.getId() + "_trunk_eren").orElseThrow().getContext())
                .isEqualTo("tenant_" + first.getId() + "_internal");
        assertThat(psAors.findById("tenant" + first.getId() + "_trunk_eren").orElseThrow().getContact()).isEqualTo("sip:192.0.2.20:5060");
        assertThat(psIdentifies.findById("tenant" + first.getId() + "_trunk_eren_identify").orElseThrow().getMatchValue()).isEqualTo("192.0.2.20");

        mvc.perform(post("/api/queues").header("Authorization", bearer(firstAdmin))
                .contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsString(Map.of(
                        "name", "support", "strategy", "leastrecent", "timeout", 20, "retry", 5,
                        "wrapupTime", 15, "maxLength", 0, "musicOnHold", "default", "enabled", true))))
                .andExpect(status().isCreated());
        Queue queue = queues.findAll().stream().filter(row -> row.getTenantId().equals(first.getId())).findFirst().orElseThrow();
        var realtimeQueue = realtimeQueues.findById("tenant" + first.getId() + "_support").orElseThrow();
        assertThat(realtimeQueue.getWrapuptime()).isEqualTo(15);
        assertThat(realtimeQueue.getContext()).isEqualTo("tenant_" + first.getId() + "_internal");

        Endpoint endpoint = endpoints.findAll().stream().filter(row -> row.getTenantId().equals(first.getId())).findFirst().orElseThrow();
        mvc.perform(post("/api/queues/" + queue.getId() + "/members").header("Authorization", bearer(firstAdmin))
                .contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsString(Map.of("endpointId", endpoint.getId(), "penalty", 0, "paused", false))))
                .andExpect(status().isCreated());
        assertThat(realtimeMembers.findById(new com.netgsm.asterisk.asterisk.realtime.entity.AsteriskQueueMemberId(
                "tenant" + first.getId() + "_support", "PJSIP/tenant" + first.getId() + "_1001"))).isPresent();

        mvc.perform(post("/api/extensions").header("Authorization", bearer(firstAdmin))
                .contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsString(Map.of(
                        "extensionNumber", "2001", "name", "Support route", "targetType", "QUEUE", "targetId", queue.getId(), "enabled", true))))
                .andExpect(status().isCreated());
        assertThat(realtimeExtensions.findAllByContextAndExtenOrderByPriorityAsc("tenant_" + first.getId() + "_internal", "2001"))
                .extracting("app", "appdata").contains(org.assertj.core.groups.Tuple.tuple("Queue", "tenant" + first.getId() + "_support"));

        mvc.perform(post("/api/dialplans").header("Authorization", bearer(firstAdmin))
                .contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsString(Map.of(
                        "extension", "3001", "priority", 1, "application", "Playback", "applicationData", "welcome", "enabled", true))))
                .andExpect(status().isCreated());
        assertThat(realtimeExtensions.findAllByContextAndExtenOrderByPriorityAsc("tenant_" + first.getId() + "_internal", "3001"))
                .extracting("appdata").containsExactly("welcome");

        mvc.perform(post("/api/ivrs").header("Authorization", bearer(firstAdmin))
                .contentType(MediaType.APPLICATION_JSON).content(ivrRequest(first.getId(), "main")))
                .andExpect(status().isCreated());
        Ivr ivr = ivrs.findAll().stream().filter(row -> row.getTenantId().equals(first.getId())).findFirst().orElseThrow();
        mvc.perform(post("/api/ivrs/" + ivr.getId() + "/options").header("Authorization", bearer(firstAdmin))
                .contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsString(Map.of("digit", "2", "actionType", "QUEUE", "targetId", queue.getId()))))
                .andExpect(status().isCreated());
        assertThat(realtimeExtensions.findAllByContextAndExtenOrderByPriorityAsc("tenant_" + first.getId() + "_internal", "tenant" + first.getId() + "_ivr_main"))
                .extracting("app").containsExactly("Answer", "Set", "Read", "GotoIf", "Goto");
        assertThat(realtimeExtensions.findAllByContextAndExtenOrderByPriorityAsc("tenant_" + first.getId() + "_internal", "tenant" + first.getId() + "_ivr_main_2"))
                .extracting("app", "appdata").contains(
                        org.assertj.core.groups.Tuple.tuple("Queue", "tenant" + first.getId() + "_support"));
    }
    @Test void queueMemberCreateAndDeletePersistInDatabase() throws Exception {
        Queue queue = queue(first.getId()); Endpoint endpoint = endpoint(first.getId());
        String path = "/api/queues/" + queue.getId() + "/members";
        var result = mvc.perform(post(path).header("Authorization", bearer(firstAdmin))
                .contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsString(Map.of(
                        "endpointId", endpoint.getId(), "penalty", 0, "paused", false))))
                .andExpect(status().isCreated()).andReturn();
        long id = json.readTree(result.getResponse().getContentAsString()).get("id").asLong();
        var saved = members.findById(id).orElseThrow();
        assertThat(saved.getEndpointId()).isEqualTo(endpoint.getId());
        assertThat(saved.getTenantId()).isEqualTo(first.getId());
        mvc.perform(delete(path + "/" + id).header("Authorization", bearer(firstAdmin)))
                .andExpect(status().isNoContent());
        assertThat(members.existsById(id)).isFalse();
    }
    @Test void queueMemberUpdatePersistsInDatabaseAndRejectsCrossTenantEndpoint() throws Exception {
        Queue queue = queue(first.getId());
        Endpoint firstEndpoint = endpoint(first.getId());
        Endpoint secondEndpoint = endpoint(first.getId(), "1002");
        Endpoint otherTenantEndpoint = endpoint(second.getId());
        var member = new com.netgsm.asterisk.entity.QueueMember();
        member.setTenantId(first.getId()); member.setQueueId(queue.getId()); member.setEndpointId(firstEndpoint.getId());
        member.setPenalty(0); member.setPaused(false); member = members.saveAndFlush(member);
        String path = "/api/queues/" + queue.getId() + "/members/" + member.getId();
        mvc.perform(put(path).header("Authorization", bearer(firstAdmin))
                .contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsString(Map.of(
                        "endpointId", otherTenantEndpoint.getId(), "penalty", 2, "paused", true))))
                .andExpect(status().isNotFound());
        mvc.perform(put(path).header("Authorization", bearer(firstAdmin))
                .contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsString(Map.of(
                        "endpointId", secondEndpoint.getId(), "penalty", 2, "paused", true))))
                .andExpect(status().isOk()).andExpect(jsonPath("$.endpointId").value(secondEndpoint.getId()))
                .andExpect(jsonPath("$.penalty").value(2)).andExpect(jsonPath("$.paused").value(true));
        var saved = members.findById(member.getId()).orElseThrow();
        assertThat(saved.getEndpointId()).isEqualTo(secondEndpoint.getId());
        assertThat(saved.getPenalty()).isEqualTo(2);
        assertThat(saved.getPaused()).isTrue();
    }
    @Test void crossTenantQueueMemberIsRejectedBeforeDatabaseWrite() throws Exception {
        Queue queue = queue(first.getId()); Endpoint other = endpoint(second.getId());
        mvc.perform(post("/api/queues/" + queue.getId() + "/members").header("Authorization", bearer(firstAdmin))
                .contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsString(Map.of("endpointId", other.getId(), "penalty", 0, "paused", false))))
                .andExpect(status().isNotFound());
        assertThat(members.count()).isZero();
    }
    @Test void crossTenantIvrTargetAndExtensionTargetAreRejected() throws Exception {
        Ivr ivr = ivr(first.getId()); Queue other = queue(second.getId());
        mvc.perform(post("/api/ivrs/" + ivr.getId() + "/options").header("Authorization", bearer(firstAdmin))
                .contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsString(Map.of("digit", "1", "actionType", "QUEUE", "targetId", other.getId()))))
                .andExpect(status().isNotFound());
        mvc.perform(post("/api/extensions").header("Authorization", bearer(firstAdmin))
                .contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsString(Map.of("name", "target", "extensionNumber", "2001", "targetType", "QUEUE", "targetId", other.getId(), "enabled", true))))
                .andExpect(status().isNotFound());
        assertThat(options.count()).isZero(); assertThat(extensions.count()).isZero();
    }
    @Test void validationErrorsDoNotEchoSensitiveInput() throws Exception {
        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON).content("{\"email\":\"\",\"password\":\"\"}"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.error").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.errors.email").exists());
    }
    @Test void userPasswordIsHashedAndUpdatesRevokeTokens() throws Exception {
        String oldToken = bearer(firstAdmin);
        mvc.perform(put("/api/admin/users/" + firstAdmin.getId()).header("Authorization", bearer(admin))
                .contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsString(Map.of("username", firstAdmin.getUsername(), "email", firstAdmin.getEmail(), "password", "new-password-123", "enabled", true))))
                .andExpect(status().isOk()).andExpect(jsonPath("$.passwordHash").doesNotExist());
        assertThat(passwords.matches("new-password-123", users.findById(firstAdmin.getId()).orElseThrow().getPasswordHash())).isTrue();
        mvc.perform(get("/api/ivrs").header("Authorization", oldToken)).andExpect(status().isUnauthorized());
    }
    @Test void corsAndSwaggerAreAvailable() throws Exception {
        mvc.perform(options("/api/ivrs").header("Origin", "https://test.invalid").header("Access-Control-Request-Method", "POST"))
                .andExpect(status().isOk()).andExpect(header().string("Access-Control-Allow-Origin", "https://test.invalid"));
        mvc.perform(get("/v3/api-docs")).andExpect(status().isOk()).andExpect(jsonPath("$.components.securitySchemes.bearerAuth.scheme").value("bearer"));
    }
    private void assertDatabaseCrud(String path, String body,
            org.springframework.data.jpa.repository.JpaRepository<?, Long> repository) throws Exception {
        var result = mvc.perform(post(path).header("Authorization", bearer(firstAdmin))
                .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.tenantId").value(first.getId()))
                .andExpect(jsonPath("$.passwordHash").doesNotExist()).andReturn();
        long id = json.readTree(result.getResponse().getContentAsString()).get("id").asLong();
        assertThat(repository.existsById(id)).isTrue();
        var updated = (tools.jackson.databind.node.ObjectNode) json.readTree(body);
        updated.put("enabled", false);
        mvc.perform(put(path + "/" + id).header("Authorization", bearer(firstAdmin))
                .contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsString(updated)))
                .andExpect(status().isOk());
        mvc.perform(get(path + "/" + id).header("Authorization", bearer(firstAdmin)))
                .andExpect(status().isOk()).andExpect(jsonPath("$.enabled").value(false));
        mvc.perform(delete(path + "/" + id).header("Authorization", bearer(firstAdmin)))
                .andExpect(status().isNoContent());
        assertThat(repository.existsById(id)).isFalse();
    }
    private String bearer(User user) { return "Bearer " + jwt.issue(user); }
    private Tenant tenant(String code) {
        Tenant tenant = new Tenant(); tenant.setName(code); tenant.setCode(code); tenant.setStatus(TenantStatus.ACTIVE); return tenants.saveAndFlush(tenant);
    }
    private User user(String name, Long tenantId, Role role) {
        User user = new User(); user.setUsername(name); user.setEmail(name + "@test.invalid"); user.setTenantId(tenantId);
        user.setRole(role); user.setEnabled(true); user.setPasswordHash(passwordHash); return users.saveAndFlush(user);
    }
    private Endpoint endpoint(Long tenantId) {
        return endpoint(tenantId, "1001");
    }
    private Endpoint endpoint(Long tenantId, String extensionNumber) {
        Endpoint endpoint = new Endpoint(); endpoint.setTenantId(tenantId); endpoint.setExtension("1001"); endpoint.setDisplayName("test");
        endpoint.setExtension(extensionNumber);
        endpoint.setContext("tenant_" + tenantId + "_internal"); endpoint.setTransport("transport-udp"); endpoint.setCodecs("ulaw");
        endpoint.setEnabled(true); endpoint.setPasswordHash(passwordHash); return endpoints.saveAndFlush(endpoint);
    }
    private Queue queue(Long tenantId) {
        Queue queue = new Queue(); queue.setTenantId(tenantId); queue.setName("support"); queue.setStrategy("ringall"); queue.setTimeout(20);
        queue.setRetry(5); queue.setWrapupTime(0); queue.setMaxLength(0); queue.setMusicOnHold("default"); queue.setEnabled(true); return queues.saveAndFlush(queue);
    }
    private Ivr ivr(Long tenantId) {
        Ivr ivr = new Ivr(); ivr.setTenantId(tenantId); ivr.setName("menu"); ivr.setAudioFile("welcome"); ivr.setTimeout(5); ivr.setMaxAttempts(3); ivr.setEnabled(true);
        return ivrs.saveAndFlush(ivr);
    }
    private String ivrRequest(Long tenantId, String name) throws Exception {
        return json.writeValueAsString(Map.of("tenantId", tenantId, "name", name, "audioFile", "welcome", "timeout", 5, "maxAttempts", 3, "enabled", true));
    }
    private String endpointRequest(Long tenantId) throws Exception {
        return json.writeValueAsString(Map.of("tenantId", tenantId, "extension", "1001", "displayName", "test", "transport", "transport-udp", "codecs", "ulaw", "enabled", true, "password", "sip-password-123"));
    }
}
