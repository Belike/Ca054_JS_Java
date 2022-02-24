package com.camunda.consulting;

import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.client.spring.annotation.ExternalTaskSubscription;
import org.camunda.bpm.client.task.ExternalTaskHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.util.HashMap;

@Configuration
@Slf4j
public class PaymentWorker {

    final String baseUrl = "http://localhost:8080/engine-rest/message";

    @Primary
    @Bean(name = "deductCredit")
    @ExternalTaskSubscription("deductCredit")
    public ExternalTaskHandler deductCredit(){
        return (externalTask, externalTaskService) -> {
            Integer retries = externalTask.getRetries();
            if(retries == null) retries = 3;
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
            boolean shouldFail = externalTask.getVariable("shouldFail");

            if(shouldFail){
                externalTaskService.handleFailure(externalTask, "ErrorMessage", "ErrorDetails have to be here. Something is wrong", retries-1, 60000L);
            }else{
                externalTaskService.complete(externalTask, variables);
            }
            log.info("ExternalTask {} has been completed.", externalTask.getId());
        };
    }
}
