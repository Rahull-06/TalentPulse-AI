package com.talentpulse.gateway.config;

import java.util.HashMap;
import java.util.Map;
import org.springframework.boot.actuate.info.Info;
import org.springframework.boot.actuate.info.InfoContributor;
import org.springframework.stereotype.Component;

/** Simple /actuator/info payload for local debugging. */
@Component
public class GatewayInfoContributor implements InfoContributor {

    @Override
    public void contribute(Info.Builder builder) {
        Map<String, Object> gateway = new HashMap<>();
        gateway.put("role", "API Gateway");
        gateway.put("note", "JWT is validated by downstream services (pass-through)");
        builder.withDetail("talentpulse", gateway);
    }
}
