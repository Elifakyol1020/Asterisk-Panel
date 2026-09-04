package com.netgsm.asterisk.service.provisioning;

import com.netgsm.asterisk.asterisk.realtime.entity.AsteriskQueue;
import com.netgsm.asterisk.asterisk.realtime.entity.AsteriskQueueMember;
import com.netgsm.asterisk.asterisk.realtime.entity.PsAor;
import com.netgsm.asterisk.asterisk.realtime.entity.PsAuth;
import com.netgsm.asterisk.asterisk.realtime.entity.PsEndpoint;
import com.netgsm.asterisk.asterisk.realtime.entity.PsEndpointIdIp;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.SQLException;

@Component
@RequiredArgsConstructor
public class AsteriskRealtimeWriter {
    private final JdbcTemplate jdbc;
    private final DataSource dataSource;

    public void upsertAor(PsAor aor) {
        if (!isPostgres()) {
            jdbc.update("""
                    merge into ps_aors (id, contact, max_contacts, remove_existing, qualify_frequency)
                    key(id) values (?, ?, ?, ?, ?)
                    """, aor.getId(), aor.getContact(), aor.getMaxContacts(),
                    aor.getRemoveExisting(), aor.getQualifyFrequency());
            return;
        }
        String bool = astBoolPlaceholder();
        jdbc.update("""
                insert into ps_aors (id, contact, max_contacts, remove_existing, qualify_frequency)
                values (?, ?, ?, %s, ?)
                on conflict (id) do update set
                  contact = excluded.contact,
                  max_contacts = excluded.max_contacts,
                  remove_existing = excluded.remove_existing,
                  qualify_frequency = excluded.qualify_frequency
                """.formatted(bool),
                aor.getId(), aor.getContact(), aor.getMaxContacts(), aor.getRemoveExisting(), aor.getQualifyFrequency());
    }

    public void upsertAuth(PsAuth auth) {
        if (!isPostgres()) {
            jdbc.update("""
                    merge into ps_auths (id, auth_type, username, password)
                    key(id) values (?, ?, ?, ?)
                    """, auth.getId(), auth.getAuthType(), auth.getUsername(), auth.getPassword());
            return;
        }
        jdbc.update("""
                insert into ps_auths (id, auth_type, username, password)
                values (?, cast(? as pjsip_auth_type_values_v2), ?, ?)
                on conflict (id) do update set
                  auth_type = excluded.auth_type,
                  username = excluded.username,
                  password = excluded.password
                """, auth.getId(), auth.getAuthType(), auth.getUsername(), auth.getPassword());
    }

    public void upsertEndpoint(PsEndpoint endpoint) {
        if (!isPostgres()) {
            jdbc.update("""
                    merge into ps_endpoints (
                      id, transport, aors, auth, outbound_auth, context, disallow, allow,
                      direct_media, force_rport, rewrite_contact, rtp_symmetric,
                      from_user, from_domain, callerid, set_var
                    ) key(id) values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                    """,
                    endpoint.getId(), endpoint.getTransport(), endpoint.getAors(), endpoint.getAuth(),
                    endpoint.getOutboundAuth(), endpoint.getContext(), endpoint.getDisallow(), endpoint.getAllow(),
                    endpoint.getDirectMedia(), endpoint.getForceRport(), endpoint.getRewriteContact(),
                    endpoint.getRtpSymmetric(), endpoint.getFromUser(), endpoint.getFromDomain(),
                    endpoint.getCallerid(), endpoint.getSetVar());
            return;
        }
        String bool = astBoolPlaceholder();
        jdbc.update("""
                insert into ps_endpoints (
                  id, transport, aors, auth, outbound_auth, context, disallow, allow,
                  direct_media, force_rport, rewrite_contact, rtp_symmetric,
                  from_user, from_domain, callerid, set_var
                ) values (?, ?, ?, ?, ?, ?, ?, ?, %s, %s, %s, %s, ?, ?, ?, ?)
                on conflict (id) do update set
                  transport = excluded.transport,
                  aors = excluded.aors,
                  auth = excluded.auth,
                  outbound_auth = excluded.outbound_auth,
                  context = excluded.context,
                  disallow = excluded.disallow,
                  allow = excluded.allow,
                  direct_media = excluded.direct_media,
                  force_rport = excluded.force_rport,
                  rewrite_contact = excluded.rewrite_contact,
                  rtp_symmetric = excluded.rtp_symmetric,
                  from_user = excluded.from_user,
                  from_domain = excluded.from_domain,
                  callerid = excluded.callerid,
                  set_var = excluded.set_var
                """.formatted(bool, bool, bool, bool),
                endpoint.getId(), endpoint.getTransport(), endpoint.getAors(), endpoint.getAuth(),
                endpoint.getOutboundAuth(), endpoint.getContext(), endpoint.getDisallow(), endpoint.getAllow(),
                endpoint.getDirectMedia(), endpoint.getForceRport(), endpoint.getRewriteContact(),
                endpoint.getRtpSymmetric(), endpoint.getFromUser(), endpoint.getFromDomain(),
                endpoint.getCallerid(), endpoint.getSetVar());
    }

    public void upsertIdentify(PsEndpointIdIp identify) {
        if (!isPostgres()) {
            jdbc.update("""
                    merge into ps_endpoint_id_ips (id, endpoint, match)
                    key(id) values (?, ?, ?)
                    """, identify.getId(), identify.getEndpoint(), identify.getMatchValue());
            return;
        }
        jdbc.update("""
                insert into ps_endpoint_id_ips (id, endpoint, match)
                values (?, ?, ?)
                on conflict (id) do update set
                  endpoint = excluded.endpoint,
                  match = excluded.match
                """, identify.getId(), identify.getEndpoint(), identify.getMatchValue());
    }

    public void upsertQueue(AsteriskQueue queue) {
        if (!isPostgres()) {
            jdbc.update("""
                    merge into queues (
                      name, musiconhold, context, timeout, retry, wrapuptime, maxlen, strategy,
                      ringinuse, autofill, announce_frequency, announce_position,
                      periodic_announce_frequency, joinempty, leavewhenempty, monitor_format
                    ) key(name) values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                    """,
                    queue.getName(), queue.getMusicOnHold(), queue.getContext(), queue.getTimeout(), queue.getRetry(),
                    queue.getWrapuptime(), queue.getMaxlen(), queue.getStrategy(), queue.getRinginuse(),
                    queue.getAutofill(), queue.getAnnounceFrequency(), queue.getAnnouncePosition(),
                    queue.getPeriodicAnnounceFrequency(), queue.getJoinempty(), queue.getLeavewhenempty(),
                    queue.getMonitorFormat());
            return;
        }
        jdbc.update("""
                insert into queues (
                  name, musiconhold, context, timeout, retry, wrapuptime, maxlen, strategy,
                  ringinuse, autofill, announce_frequency, announce_position,
                  periodic_announce_frequency, joinempty, leavewhenempty, monitor_format
                ) values (?, ?, ?, ?, ?, ?, ?, cast(? as queue_strategy_values),
                  cast(? as yesno_values), cast(? as yesno_values), ?, ?, ?, ?, ?, ?)
                on conflict (name) do update set
                  musiconhold = excluded.musiconhold,
                  context = excluded.context,
                  timeout = excluded.timeout,
                  retry = excluded.retry,
                  wrapuptime = excluded.wrapuptime,
                  maxlen = excluded.maxlen,
                  strategy = excluded.strategy,
                  ringinuse = excluded.ringinuse,
                  autofill = excluded.autofill,
                  announce_frequency = excluded.announce_frequency,
                  announce_position = excluded.announce_position,
                  periodic_announce_frequency = excluded.periodic_announce_frequency,
                  joinempty = excluded.joinempty,
                  leavewhenempty = excluded.leavewhenempty,
                  monitor_format = excluded.monitor_format
                """,
                queue.getName(), queue.getMusicOnHold(), queue.getContext(), queue.getTimeout(), queue.getRetry(),
                queue.getWrapuptime(), queue.getMaxlen(), queue.getStrategy(), queue.getRinginuse(),
                queue.getAutofill(), queue.getAnnounceFrequency(), queue.getAnnouncePosition(),
                queue.getPeriodicAnnounceFrequency(), queue.getJoinempty(), queue.getLeavewhenempty(),
                queue.getMonitorFormat());
    }

    public void upsertQueueMember(AsteriskQueueMember member) {
        if (!isPostgres()) {
            jdbc.update("""
                    merge into queue_members (
                      queue_name, interface, membername, state_interface, penalty,
                      paused, uniqueid, wrapuptime, ringinuse
                    ) key(queue_name, interface) values (?, ?, ?, ?, ?, ?, ?, ?, ?)
                    """,
                    member.getQueueName(), member.getInterfaceName(), member.getMembername(), member.getStateInterface(),
                    member.getPenalty(), member.getPaused(), member.getUniqueid(), member.getWrapuptime(),
                    member.getRinginuse());
            return;
        }
        String bool = astBoolPlaceholder();
        jdbc.update("""
                insert into queue_members (
                  queue_name, interface, membername, state_interface, penalty,
                  paused, uniqueid, wrapuptime, ringinuse
                ) values (?, ?, ?, ?, ?, ?, ?, ?, %s)
                on conflict (queue_name, interface) do update set
                  membername = excluded.membername,
                  state_interface = excluded.state_interface,
                  penalty = excluded.penalty,
                  paused = excluded.paused,
                  uniqueid = excluded.uniqueid,
                  wrapuptime = excluded.wrapuptime,
                  ringinuse = excluded.ringinuse
                """.formatted(bool),
                member.getQueueName(), member.getInterfaceName(), member.getMembername(), member.getStateInterface(),
                member.getPenalty(), member.getPaused(), member.getUniqueid(), member.getWrapuptime(),
                member.getRinginuse());
    }

    private String astBoolPlaceholder() {
        return isPostgres() ? "cast(? as ast_bool_values)" : "?";
    }

    private boolean isPostgres() {
        try (var connection = dataSource.getConnection()) {
            return connection.getMetaData().getDatabaseProductName().toLowerCase().contains("postgres");
        } catch (SQLException ex) {
            throw new IllegalStateException("Cannot inspect database type", ex);
        }
    }
}
