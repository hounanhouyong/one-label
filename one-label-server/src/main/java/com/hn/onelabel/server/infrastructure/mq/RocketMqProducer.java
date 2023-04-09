package com.hn.onelabel.server.infrastructure.mq;

import com.aliyun.openservices.ons.api.Message;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class RocketMqProducer {

    public String syncSend(Message message, Object object) {
        try {
            return "";
        } catch (Exception e) {
            log.error("[RocketMqProducer] - sync send failure.", e);
        }
        return null;
    }

    public String delaySend(Message message, Object object, long delayMilliseconds) {
        try {
            return "";
        } catch (Exception e) {
            log.error("[RocketMqProducer] - delay send failure.", e);
        }
        return null;
    }
}
