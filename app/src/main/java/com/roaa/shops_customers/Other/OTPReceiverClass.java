package com.roaa.shops_customers.Other;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.provider.Telephony;
import android.telephony.SmsMessage;

import in.aabhasjindal.otptextview.OtpTextView;

public class OTPReceiverClass extends BroadcastReceiver {
    private static OtpTextView otpTextView;

    public void setOTP(OtpTextView otpTextView)
    {
        OTPReceiverClass.otpTextView=otpTextView;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        //message will be holding complete sms that is received
        SmsMessage[] messages = Telephony.Sms.Intents.getMessagesFromIntent(intent);
        for (SmsMessage smsMessage : messages){
            String message_body = smsMessage.getMessageBody();
            String otp=message_body.replaceAll("[^0-9]","");
            //String otp = message_body.split(":")[1];
            otpTextView.setOTP(otp);
        }
    }
}
