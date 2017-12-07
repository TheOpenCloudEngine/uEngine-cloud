package org.uengine.cloud.scheduler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.uengine.cloud.app.DcosApi;
import org.uengine.cloud.ssh.SshService;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Created by uengine on 2017. 12. 4..
 */
@Component
public class CronTable {

    @Autowired
    private SshService sshService;

    @Autowired
    private DcosApi dcosApi;

    // 애플리케이션 시작 후 10초 후에 첫 실행, 그 후 매 10초마다 주기적으로 실행한다.
    @Scheduled(initialDelay = 10000, fixedDelay = 10000)
    public void removeSshContainer() throws Exception {
        Date currentDate = new Date();
        Map groups = dcosApi.getGroups();
        List<Map> apps = (List<Map>) groups.get("apps");
        for (int i = 0; i < apps.size(); i++) {
            Map marathonApp = apps.get(i);
            String marathonAppId = marathonApp.get("id").toString();
            if (marathonAppId.startsWith("/ssh-")) {
                String taskId = marathonAppId.replace("/ssh-", "");

                //허트비트 매퍼에 마라톤아이디가 없으면 삭제.
                if (!sshService.heartbeatMapper.containsKey(taskId)) {
                    sshService.deleteSshContainer(taskId);
                }
                //허트비트 매퍼에, 10초 이상된 taskId 삭제.
                else {
                    Date lastDate = sshService.heartbeatMapper.get(taskId);
                    long diff = currentDate.getTime() - lastDate.getTime();
                    if (diff > 1000 * 10) {
                        sshService.deleteSshContainer(taskId);
                    }
                }
            }
        }
    }
}
