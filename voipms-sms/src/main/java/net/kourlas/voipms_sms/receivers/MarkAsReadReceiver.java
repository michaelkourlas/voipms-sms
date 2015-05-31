/*
 * VoIP.ms SMS
 * Copyright (C) 2015 Michael Kourlas and other contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.kourlas.voipms_sms.receivers;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import net.kourlas.voipms_sms.Database;
import net.kourlas.voipms_sms.Notifications;
import net.kourlas.voipms_sms.model.Sms;

public class MarkAsReadReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String contact = intent.getExtras().getString("contact");

        NotificationManager manager = (NotificationManager) context.getApplicationContext().getSystemService(
                Context.NOTIFICATION_SERVICE);
        Integer notificationId = Notifications.getInstance(context.getApplicationContext()).getNotificationIds().get(
                contact);
        if (notificationId != null) {
            manager.cancel(notificationId);
        }

        for (Sms sms : Database.getInstance(context.getApplicationContext()).getConversation(contact).getAllSms()) {
            sms.setUnread(false);
            Database.getInstance(context.getApplicationContext()).replaceSms(sms);
        }
    }
}
