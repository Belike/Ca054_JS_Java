package com.camunda.consulting;

import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.client.spring.annotation.ExternalTaskSubscription;
import org.camunda.bpm.client.task.ExternalTaskHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;

@Configuration
@Slf4j

public class CompensationWorker {

    @Bean(name = "compensateBalance")
    @ExternalTaskSubscription(topicName = "compensateBalance",
    lockDuration = 10000L)
    public ExternalTaskHandler compensateBalance(){
        return (externalTask, externalTaskService) -> {
            HashMap<String, Object> variables = new HashMap<>();

            double balance = (double) externalTask.getVariable("balance");
            double remaining = (double) externalTask.getVariable("remaining");
            double amount = (double) externalTask.getVariable("amount");

            log.info("Balance before compensation is {} ", balance);
            balance = amount - remaining;
            variables.put("balance", balance);

            externalTaskService.complete(externalTask, variables);
            log.info("Balance after compensation is {}", balance);
        };
    }
}
