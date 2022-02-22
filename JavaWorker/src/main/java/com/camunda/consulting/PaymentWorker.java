package com.camunda.consulting;

import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.client.spring.annotation.ExternalTaskSubscription;
import org.camunda.bpm.client.task.ExternalTaskHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;

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
            HashMap<String, Object> variables = new HashMap<>();

            log.info("Starting deducting credit");
            log.info("Retrieving amount to be deducted");

            //Mimic a call to retrieve Balance -> can also be a start parameter?
            final double balance = 20.00;
            double amount = (double) externalTask.getVariable("amount");

            if(amount > balance){
                variables.put("remaining", amount-balance);
                variables.put("creditSufficient", false);
                variables.put("balance", 0.0);
            }else{
                variables.put("remaining", 0.0);
                variables.put("creditSufficient", true);
                variables.put("balance", balance-amount);
            }


            externalTaskService.complete(externalTask, variables);
            log.info("ExternalTask {} has been completed.", externalTask.getId());
        }));
    }
}
