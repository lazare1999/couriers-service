package com.lazo.couriers.utils;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import org.apache.commons.lang3.StringUtils;

/**
 * Created by Lazo on 2022-05-24
 */

public class MessagingUtils {

    public static final String COURIER_APPROVED_JOB = "COURIER_APPROVED_JOB";
    public static final String HAND_OVER_JOB = "HAND_OVER_JOB";
    public static final String JOBS_DONE = "JOBS_DONE";
    public static final String PARCEL_UNSUCCESSFUL = "PARCEL_UNSUCCESSFUL";
    public static final String COURIER_TOOK_PARCEL = "COURIER_TOOK_PARCEL";

    public static void sendCustomMessage(String title, String body, String topic) {
        if (StringUtils.isEmpty(title) || StringUtils.isEmpty(body) || StringUtils.isEmpty(topic))
            return;

        Message message = Message.builder()
                .putData("title", title)
                .putData("body", body)
                .setTopic(topic)
                .build();

        try {
            String response = FirebaseMessaging.getInstance().send(message);
            System.out.println("Successfully sent message: " + response);
        } catch (FirebaseMessagingException e) {
            throw new RuntimeException(e);
        }
    }

}
