package org.uengine.cloud.app.emitter;

import com.nimbusds.jose.JWSObject;
import com.nimbusds.jwt.JWTClaimsSet;
import net.minidev.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import org.uengine.cloud.app.AppEntity;
import org.uengine.iam.client.model.OauthUser;
import org.uengine.iam.util.JsonUtils;
import org.uengine.iam.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

@RestController
public class SSEController {

    private static final Logger LOGGER = LoggerFactory.getLogger(SSEController.class);

    private final CopyOnWriteArrayList<OauthSseEmitter> userBaseEmitters = new CopyOnWriteArrayList<>();

    @GetMapping("/emitter")
    public SseEmitter handle(HttpServletRequest request,
                             HttpServletResponse response,
                             @RequestParam(value = "token") String token
    ) throws Exception {
        try {
            JWSObject jwsObject = JWSObject.parse(token);
            JSONObject jsonPayload = jwsObject.getPayload().toJSONObject();
            JWTClaimsSet jwtClaimsSet = JWTClaimsSet.parse(jsonPayload);

            JSONObject contexts = (JSONObject) jwtClaimsSet.getClaim("context");
            OauthUser oauthUser = JsonUtils.convertValue((Map) contexts.get("user"), OauthUser.class);
            if (oauthUser == null) {
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
            }

            //emitters 등록시 토큰으로부터 사용자 정보와 함께 저장.
            OauthSseEmitter emitter = new OauthSseEmitter(360_000L, oauthUser); //360 sec.
            userBaseEmitters.add(emitter);

            emitter.onCompletion(() -> this.userBaseEmitters.remove(emitter));
            emitter.onTimeout(() -> this.userBaseEmitters.remove(emitter));

            LOGGER.info("emitter counts: {}", userBaseEmitters.size());

            return emitter;

        } catch (Exception e) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
            return null;
        }
    }

    public void appEntityBaseEmitterSend(AppEntityBaseMessage appEntityBaseMessage) throws Exception {
        List<Integer> gitlabIds = new ArrayList<>();
        AppEntity appEntity = appEntityBaseMessage.getAppEntity();
        String message = JsonUtils.marshal(appEntityBaseMessage);

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
        userBaseEmitters.removeAll(deadEmitters);
    }
}
