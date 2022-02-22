package com.camunda.consulting;

import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.client.spring.annotation.ExternalTaskSubscription;
import org.camunda.bpm.client.task.ExternalTaskHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
public class PaymentWorker {

    final String baseUrl = "http://localhost:8080/engine-rest/message";

    @Bean
    @ExternalTaskSubscription(
            topicName = "deductCredit",
            lockDuration = 10000L)
    public ExternalTaskHandler deductCredit(){
        return (((externalTask, externalTaskService) -> {
            log.info("Starting deducting credit");

            externalTaskService.complete(externalTask);

            log.info("ExternalTask {} has been completed.", externalTask.getId());
        }));
    }
}
