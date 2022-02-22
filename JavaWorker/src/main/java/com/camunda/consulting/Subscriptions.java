package com.camunda.consulting;

import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.client.spring.SpringTopicSubscription;
import org.camunda.bpm.client.spring.event.SubscriptionInitializedEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
@Slf4j
public class Subscriptions implements ApplicationListener<SubscriptionInitializedEvent> {

    @Autowired
    public SpringTopicSubscription deductCredit;

    @PostConstruct
    public void listSubscriptionBeans() {
        log.info("Subscription bean 'invoiceCreatorHandlerSubscription' has topic name: {} ",
                deductCredit.getTopicName());
    }

    @Override
    public void onApplicationEvent(SubscriptionInitializedEvent event) {
        SpringTopicSubscription springTopicSubscription = event.getSource();
        String topicName = springTopicSubscription.getTopicName();
        log.info("Subscription with topic name '{}' initialized", topicName);

        if (!springTopicSubscription.isOpen()) {
            log.info("Subscription with topic name '{}' not yet opened", topicName);

            // do something before subscription is opened

            springTopicSubscription.open();

            log.info("Subscription with topic name '{}' opened", topicName);

            springTopicSubscription.close();

            log.info("Subscription with topic name '{}' temporarily closed", topicName);

            // do something with subscription closed

            springTopicSubscription.open();

            log.info("Subscription with topic name '{}' reopened again", topicName);
        }
    }
}
