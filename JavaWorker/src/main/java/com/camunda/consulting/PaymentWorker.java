package com.camunda.consulting;

import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.client.spring.annotation.ExternalTaskSubscription;
import org.camunda.bpm.client.task.ExternalTaskHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Configuration
@Slf4j
public class PaymentWorker {

    final String baseUrl = "http://localhost:8080/engine-rest/message";

    @Bean
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
            boolean shouldFail = false;
            //externalTask.getVariable("shouldFail");

            if(shouldFail){
                externalTaskService.handleFailure(externalTask, "ErrorMessage", "ErrorDetails have to be here. Something is wrong", retries-1, 60000L);
            }else{
                externalTaskService.complete(externalTask, variables);
            }
            log.info("ExternalTask {} has been completed.", externalTask.getId());
        };
    }

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

    @Bean
    @ExternalTaskSubscription("finishPayment")
    public ExternalTaskHandler finishPayment() {
        return (externalTask, externalTaskService) -> {

            String paymentType = "Balance";
            if(externalTask.getActivityInstanceId() != "PaymentEndEvent_Balance"){
                paymentType = "CreditCard";
            }

            HashMap<String, Object> paymentTypeMap = new HashMap<>();
            paymentTypeMap.put("value", paymentType);
            paymentTypeMap.put("type", "String");

            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            Map<String, Object> map = new HashMap<>();
            map.put("messageName", "paymentReceived");
            map.put("businessKey", externalTask.getBusinessKey());
            //Map.of only works on Java >= 9
            map.put("processVariables", Collections.singletonMap("paymentType", paymentTypeMap));

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(map, headers);

            ResponseEntity<String> responseEntity = restTemplate.postForEntity("http://localhost:8080/engine-rest/message", entity, String.class);

            if (responseEntity.getStatusCode() != HttpStatus.NO_CONTENT) {
                externalTaskService.handleFailure(externalTask, "RestFailure", "Exptected Response 204, but receiveid " + responseEntity.getStatusCode(), 0, 0);
            }else{
                externalTaskService.complete(externalTask);
            }
        };
    }
}
