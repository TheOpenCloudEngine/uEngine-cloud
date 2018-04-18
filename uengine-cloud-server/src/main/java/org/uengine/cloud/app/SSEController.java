package org.uengine.cloud.app;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import org.uengine.cloud.tenant.TenantContext;
import org.uengine.iam.client.model.OauthUser;
import org.uengine.iam.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@RestController
public class SSEController {

    private final CopyOnWriteArrayList<OauthSseEmitter> userBaseEmitters = new CopyOnWriteArrayList<>();

    @GetMapping("/emitter")
    public SseEmitter handle(HttpServletRequest request, HttpServletResponse response) throws Exception {
        OauthUser oauthUser = TenantContext.getThreadLocalInstance().getUser();
        if (oauthUser == null) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
        }

        //emitters 등록시 토큰으로부터 사용자 정보와 함께 저장.
        OauthSseEmitter emitter = new OauthSseEmitter(360_000L, oauthUser); //360 sec.
        userBaseEmitters.add(emitter);

        emitter.onCompletion(() -> this.userBaseEmitters.remove(emitter));
        emitter.onTimeout(() -> this.userBaseEmitters.remove(emitter));

        return emitter;
    }

    public void gitlabBaseEmitterSend(AppEntity appEntity, String message) {
        List<Integer> gitlabIds = new ArrayList<>();
        if (appEntity != null) {
            String memberIds = appEntity.getMemberIds();
            if (!StringUtils.isEmpty(memberIds)) {
                String[] split = memberIds.split(",");
                for (String memberId : split) {
                    int gitlabId = Integer.parseInt(memberId.replaceAll("m", ""));
                    gitlabIds.add(gitlabId);
                }
            }
        }

        List<SseEmitter> deadEmitters = new ArrayList<>();
        this.userBaseEmitters.forEach(emitter -> {
            try {
                if ("admin".equals(emitter.getAcl())) {
                    emitter.send(message);
                } else if (gitlabIds.contains(emitter.getGitlabId())) {
                    emitter.send(message);
                }
            } catch (Exception e) {
                deadEmitters.add(emitter);
            }
        });
    }
}
