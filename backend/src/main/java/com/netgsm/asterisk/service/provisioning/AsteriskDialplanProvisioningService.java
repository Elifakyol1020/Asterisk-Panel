package com.netgsm.asterisk.service.provisioning;

import com.netgsm.asterisk.asterisk.realtime.entity.AsteriskExtension;
import com.netgsm.asterisk.asterisk.realtime.repository.AsteriskExtensionRepository;
import com.netgsm.asterisk.entity.Dialplan;
import com.netgsm.asterisk.entity.Endpoint;
import com.netgsm.asterisk.entity.Extension;
import com.netgsm.asterisk.entity.Ivr;
import com.netgsm.asterisk.entity.IvrOption;
import com.netgsm.asterisk.entity.Queue;
import com.netgsm.asterisk.entity.Trunk;
import com.netgsm.asterisk.exception.ResourceNotFoundException;
import com.netgsm.asterisk.repository.EndpointRepository;
import com.netgsm.asterisk.repository.IvrOptionRepository;
import com.netgsm.asterisk.repository.IvrRepository;
import com.netgsm.asterisk.repository.QueueRepository;
import com.netgsm.asterisk.repository.TrunkRepository;
import com.netgsm.asterisk.repository.ExtensionRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AsteriskDialplanProvisioningService {
    private final AsteriskNaming naming;
    private final AsteriskExtensionRepository realtime;
    private final EndpointRepository endpoints;
    private final QueueRepository queues;
    private final IvrRepository ivrs;
    private final TrunkRepository trunks;
    private final ExtensionRepository extensions;
    private final IvrOptionRepository options;

    public void upsertDialplan(Dialplan dialplan) {
        String exten = naming.dialplanExtension(dialplan.getTenantId(), dialplan.getExtension());
        realtime.deleteAllByContextAndExtenAndPriority(naming.realtimeContext(), exten, dialplan.getPriority());
        realtime.flush();
        if (Boolean.TRUE.equals(dialplan.getEnabled())) {
            save(naming.realtimeContext(), exten, dialplan.getPriority(), dialplan.getApplication(), dialplan.getApplicationData());
        }
    }

    public void deleteDialplan(Dialplan dialplan) {
        realtime.deleteAllByContextAndExtenAndPriority(naming.realtimeContext(),
                naming.dialplanExtension(dialplan.getTenantId(), dialplan.getExtension()), dialplan.getPriority());
        realtime.flush();
    }

    public void upsertExtensionRoute(Extension extension) {
        String exten = naming.dialplanExtension(extension.getTenantId(), extension.getExtensionNumber());
        realtime.deleteAllByContextAndExten(naming.realtimeContext(), exten);
        realtime.flush();
        if (!Boolean.TRUE.equals(extension.getEnabled())) return;
        log.info("Creating Asterisk route: {}/{}", naming.realtimeContext(), exten);
        save(naming.realtimeContext(), exten, 1, "NoOp", "Calling route " + extension.getExtensionNumber());
        Target target = target(extension.getTenantId(), extension.getTargetType(), extension.getTargetId());
        int targetPriority = 2;
        if ("QUEUE".equals(extension.getTargetType())) {
            save(naming.realtimeContext(), exten, targetPriority++, "MixMonitor",
                    "tenant" + extension.getTenantId() + "-${UNIQUEID}-${CALLERID(num)}-" + extension.getExtensionNumber() + ".wav,b");
        }
        save(naming.realtimeContext(), exten, targetPriority, target.app(), target.appdata());
        if (!"Goto".equals(target.app())) save(naming.realtimeContext(), exten, targetPriority + 1, "Hangup", "");
    }

    public void deleteExtensionRoute(Extension extension) {
        realtime.deleteAllByContextAndExten(naming.realtimeContext(),
                naming.dialplanExtension(extension.getTenantId(), extension.getExtensionNumber()));
        realtime.flush();
    }

    public void recompileIvr(Ivr ivr) {
        String context = naming.realtimeContext();
        String ivrExten = naming.ivrContext(ivr.getTenantId(), ivr.getName());
        realtime.deleteAllByContextAndExten(context, ivrExten);
        realtime.deleteAllByContextAndExtenStartingWith(context, ivrExten + "_");
        realtime.deleteAllByContextAndExten(context, "_" + ivrExten + "_.");
        realtime.flush();
        if (!Boolean.TRUE.equals(ivr.getEnabled())) return;
        log.info("Creating Asterisk IVR dialplan: {}/{}", context, ivrExten);
        save(context, ivrExten, 1, "Answer", "");
        save(context, ivrExten, 2, "Set", "IVR_ATTEMPTS=0");
        save(context, ivrExten, 3, "Read", "IVR_DIGIT," + ivr.getAudioFile() + ",1,,1," + ivr.getTimeout());
        save(context, ivrExten, 4, "GotoIf", "$[\"${IVR_DIGIT}\"=\"\"]?" + context + "," + ivrExten + "_invalid,1");
        save(context, ivrExten, 5, "Goto", context + "," + ivrExten + "_${IVR_DIGIT},1");

        List<IvrOption> rows = options.findAllByIvrIdAndTenantIdOrderByDigitAsc(ivr.getId(), ivr.getTenantId());
        for (IvrOption option : rows) {
            Target target = target(ivr.getTenantId(), option.getActionType(), option.getTargetId());
            String optionExten = ivrExten + "_" + option.getDigit();
            int targetPriority = 1;
            if ("QUEUE".equals(option.getActionType())) {
                save(context, optionExten, targetPriority++, "MixMonitor",
                        "tenant" + ivr.getTenantId() + "-${UNIQUEID}-${CALLERID(num)}-ivr.wav,b");
            }
            save(context, optionExten, targetPriority, target.app(), target.appdata());
            if (!"Goto".equals(target.app())) save(context, optionExten, targetPriority + 1, "Hangup", "");
        }

        compileInvalidIvrChoice(context, ivrExten + "_invalid", ivrExten, ivr.getMaxAttempts());
        compileInvalidIvrChoice(context, "_" + ivrExten + "_.", ivrExten, ivr.getMaxAttempts());
        save(context, ivrExten + "_max", 1, "Playback", "vm-goodbye");
        save(context, ivrExten + "_max", 2, "Hangup", "");
    }

    public void deleteIvr(Ivr ivr) {
        String ivrExten = naming.ivrContext(ivr.getTenantId(), ivr.getName());
        realtime.deleteAllByContextAndExten(naming.realtimeContext(), ivrExten);
        realtime.deleteAllByContextAndExtenStartingWith(naming.realtimeContext(), ivrExten + "_");
        realtime.deleteAllByContextAndExten(naming.realtimeContext(), "_" + ivrExten + "_.");
        realtime.flush();
    }

    private Target target(Long tenantId, String type, Long id) {
        return switch (type) {
            case "ENDPOINT" -> {
                Endpoint endpoint = endpoints.findByIdAndTenantId(id, tenantId).orElseThrow(() -> new ResourceNotFoundException("Endpoint"));
                yield new Target("Dial", "PJSIP/" + naming.endpoint(tenantId, endpoint.getExtension()) + ",30");
            }
            case "EXTENSION" -> {
                Extension extension = extensions.findByIdAndTenantId(id, tenantId)
                        .orElseThrow(() -> new ResourceNotFoundException("Extension"));
                yield new Target("Goto", naming.realtimeContext() + ","
                        + naming.dialplanExtension(tenantId, extension.getExtensionNumber()) + ",1");
            }
            case "QUEUE" -> {
                Queue queue = queues.findByIdAndTenantId(id, tenantId).orElseThrow(() -> new ResourceNotFoundException("Queue"));
                yield new Target("Queue", naming.queue(tenantId, queue.getName()));
            }
            case "IVR" -> {
                Ivr ivr = ivrs.findByIdAndTenantId(id, tenantId).orElseThrow(() -> new ResourceNotFoundException("Ivr"));
                yield new Target("Goto", naming.realtimeContext() + "," + naming.ivrContext(tenantId, ivr.getName()) + ",1");
            }
            case "TRUNK" -> {
                Trunk trunk = trunks.findByIdAndTenantId(id, tenantId).orElseThrow(() -> new ResourceNotFoundException("Trunk"));
                yield new Target("Dial", "PJSIP/${EXTEN}@" + naming.trunk(tenantId, trunk.getName()) + ",30");
            }
            case "HANGUP" -> new Target("Hangup", "");
            default -> throw new ResourceNotFoundException("Target");
        };
    }

    private void save(String context, String exten, int priority, String app, String appdata) {
        AsteriskExtension row = new AsteriskExtension();
        row.setContext(context);
        row.setExten(exten);
        row.setPriority(priority);
        row.setApp(app);
        row.setAppdata(appdata);
        realtime.save(row);
    }

    private void compileInvalidIvrChoice(String context, String exten, String ivrExten, int maxAttempts) {
        save(context, exten, 1, "Set", "IVR_ATTEMPTS=$[${IVR_ATTEMPTS}+1]");
        save(context, exten, 2, "GotoIf", "$[${IVR_ATTEMPTS} >= " + maxAttempts + "]?" + context + "," + ivrExten + "_max,1");
        save(context, exten, 3, "Playback", "pbx-invalid");
        save(context, exten, 4, "Goto", context + "," + ivrExten + ",3");
    }

    private record Target(String app, String appdata) { }
}
