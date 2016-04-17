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

package net.kourlas.voipms_sms.adapters;

import android.content.Context;
import android.net.Uri;
import android.provider.ContactsContract;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.BackgroundColorSpan;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.QuickContactBadge;
import android.widget.TextView;
import net.kourlas.voipms_sms.Database;
import net.kourlas.voipms_sms.Preferences;
import net.kourlas.voipms_sms.R;
import net.kourlas.voipms_sms.Utils;
import net.kourlas.voipms_sms.activities.ConversationActivity;
import net.kourlas.voipms_sms.model.Message;

import java.util.*;

public class ConversationRecyclerViewAdapter
        extends RecyclerView.Adapter<ConversationRecyclerViewAdapter.MessageViewHolder>
        implements Filterable {
    public static final int ITEM_LEFT_PRIMARY = 0;
    public static final int ITEM_LEFT_SECONDARY = 1;
    private static final int ITEM_RIGHT_PRIMARY = 2;
    private static final int ITEM_RIGHT_SECONDARY = 3;

    private final ConversationActivity activity;
    private final Context applicationContext;
    private final LinearLayoutManager layoutManager;
    private final String contact;
    private final List<Message> messages;
    private final List<Boolean> checkedItems;
    Preferences preferences;
    private String filterConstraint;
    private String oldFilterConstraint;

    public ConversationRecyclerViewAdapter(ConversationActivity activity, LinearLayoutManager layoutManager,
                                           String contact) {
        this.activity = activity;
        this.applicationContext = activity.getApplicationContext();
        this.layoutManager = layoutManager;
        this.preferences = Preferences.getInstance(applicationContext);

        this.contact = contact;
        this.messages = new ArrayList<>();
        this.filterConstraint = "";
        this.oldFilterConstraint = "";
        this.checkedItems = new ArrayList<>();
    }

    @Override
    public MessageViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View itemView = null;
        switch (viewType) {
            case ITEM_LEFT_PRIMARY:
                itemView = LayoutInflater.from(viewGroup.getContext()).inflate(
                        R.layout.conversation_item_incoming_primary, viewGroup, false);
                break;
            case ITEM_LEFT_SECONDARY:
                itemView = LayoutInflater.from(viewGroup.getContext()).inflate(
                        R.layout.conversation_item_incoming_secondary, viewGroup, false);
                break;
            case ITEM_RIGHT_PRIMARY:
                itemView = LayoutInflater.from(viewGroup.getContext()).inflate(
                        R.layout.conversation_item_outgoing_primary, viewGroup, false);
                break;
            case ITEM_RIGHT_SECONDARY:
                itemView = LayoutInflater.from(viewGroup.getContext()).inflate(
                        R.layout.conversation_item_outgoing_secondary, viewGroup, false);
                break;
        }
        return new MessageViewHolder(itemView, viewType);
    }

    private HashMap<String, String> photoCache = new HashMap<String, String>();

    private String fetchContactPhoto(String uri) {
        if (photoCache.containsKey(uri)) {
            return photoCache.get(uri);
        }
        String photo = Utils.getContactPhotoUri(applicationContext, uri);
        photoCache.put(uri, photo);
        return uri;
    }

    @Override
    public void onBindViewHolder(MessageViewHolder messageViewHolder, int i) {
        Message message = messages.get(i);
        int viewType = getItemViewType(i);

        if (viewType == ITEM_LEFT_PRIMARY || viewType == ITEM_RIGHT_PRIMARY) {
            QuickContactBadge contactBadge = messageViewHolder.getContactBadge();
            if (viewType == ITEM_LEFT_PRIMARY) {
                contactBadge.assignContactFromPhone(message.getContact(), true);
            }
            else {
                contactBadge.assignContactFromPhone(message.getDid(), true);
            }
            String photoUri;
            if (viewType == ITEM_LEFT_PRIMARY) {
                photoUri = fetchContactPhoto(message.getContact());
            }
            else {
                photoUri = fetchContactPhoto(ContactsContract.Profile.CONTENT_URI.toString());
                if (photoUri == null) {
                    photoUri = fetchContactPhoto(message.getDid());
                }
            }
            if (photoUri != null) {
                contactBadge.setImageURI(Uri.parse(photoUri));
            }
            else {
                contactBadge.setImageToDefault();
            }
        }

        View smsContainer = messageViewHolder.getSmsContainer();

        TextView messageText = messageViewHolder.getMessageText();
        SpannableStringBuilder messageTextBuilder = new SpannableStringBuilder();
        messageTextBuilder.append(message.getText());
        if (!filterConstraint.equals("")) {
            int index = message.getText().toLowerCase().indexOf(filterConstraint.toLowerCase());
            if (index != -1) {
                messageTextBuilder.setSpan(new BackgroundColorSpan(applicationContext.getResources().getColor(
                                R.color.highlight)), index, index + filterConstraint.length(),
                        SpannableString.SPAN_INCLUSIVE_EXCLUSIVE);
                messageTextBuilder.setSpan(new ForegroundColorSpan(applicationContext.getResources().getColor(
                                R.color.dark_gray)), index, index + filterConstraint.length(),
                        SpannableString.SPAN_INCLUSIVE_EXCLUSIVE);
            }
        }
        messageText.setText(messageTextBuilder);

        TextView dateText = messageViewHolder.getDateText();
        if (!message.isDelivered()) {
            if (!message.isDeliveryInProgress()) {
                SpannableStringBuilder dateTextBuilder = new SpannableStringBuilder();
                if (isItemChecked(i)) {
                    dateTextBuilder.append(applicationContext.getString(R.string.conversation_message_not_sent_selected));
                }
                else {
                    dateTextBuilder.append(applicationContext.getString(R.string.conversation_message_not_sent));
                }
                dateTextBuilder.setSpan(new ForegroundColorSpan(
                                isItemChecked(i) ? applicationContext.getResources().getColor(android.R.color.white) :
                                        applicationContext.getResources().getColor(android.R.color.holo_red_dark)), 0,
                        dateTextBuilder.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
                dateText.setText(dateTextBuilder);
                dateText.setVisibility(View.VISIBLE);
            }
            else {
                dateText.setText(applicationContext.getString(R.string.conversation_message_sending));
                dateText.setVisibility(View.VISIBLE);
            }
        }
        else if (i == messages.size() - 1 ||
                ((viewType == ITEM_LEFT_PRIMARY || viewType == ITEM_LEFT_SECONDARY) &&
                        getItemViewType(i + 1) != ITEM_LEFT_SECONDARY) ||
                ((viewType == ITEM_RIGHT_PRIMARY || viewType == ITEM_RIGHT_SECONDARY) &&
                        getItemViewType(i + 1) != ITEM_RIGHT_SECONDARY)) {
            dateText.setText(Utils.getFormattedDate(applicationContext, message.getDate(), false));
            dateText.setVisibility(View.VISIBLE);
        }
        else {
            dateText.setVisibility(View.GONE);
        }

        if (viewType == ITEM_LEFT_PRIMARY || viewType == ITEM_LEFT_SECONDARY) {
            smsContainer.setBackgroundResource(isItemChecked(i) ? android.R.color.holo_blue_dark : R.color.primary);
        }
        else {
            smsContainer.setBackgroundResource(isItemChecked(i) ? android.R.color.holo_blue_dark :
                    android.R.color.white);
            messageText.setTextColor(isItemChecked(i) ? applicationContext.getResources().getColor(
                    android.R.color.white) : applicationContext.getResources().getColor(R.color.dark_gray));
            messageText.setLinkTextColor(isItemChecked(i) ? applicationContext.getResources().getColor(
                    android.R.color.white) : applicationContext.getResources().getColor(R.color.dark_gray));
            dateText.setTextColor(isItemChecked(i) ? applicationContext.getResources().getColor(
                    R.color.message_translucent_white) : applicationContext.getResources().getColor(
                    R.color.message_translucent_dark_grey));
        }
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    @Override
    public Filter getFilter() {
        return new MessageFilter();
    }

    public int getItemViewType(int position) {
        Message message = messages.get(position);
        Message previousMessage = null;
        if (position > 0) {
            previousMessage = messages.get(position - 1);
        }

        if (message.getType() == Message.Type.INCOMING) {
            if (previousMessage == null || previousMessage.getType() == Message.Type.OUTGOING ||
                    message.getDateInDatabaseFormat() - previousMessage.getDateInDatabaseFormat() > 60) {
                return ITEM_LEFT_PRIMARY;
            }
            else {
                return ITEM_LEFT_SECONDARY;
            }
        }
        else {
            if (previousMessage == null || previousMessage.getType() == Message.Type.INCOMING ||
                    message.getDateInDatabaseFormat() - previousMessage.getDateInDatabaseFormat() > 60) {
                return ITEM_RIGHT_PRIMARY;
            }
            else {
                return ITEM_RIGHT_SECONDARY;
            }
        }
    }

    public Message getItem(int position) {
        return messages.get(position);
    }

    public boolean isItemChecked(int position) {
        return checkedItems.get(position);
    }

    public void setItemChecked(int position, boolean checked) {
        boolean previous = checkedItems.get(position);
        checkedItems.set(position, checked);

        if (previous && !checked) {
            notifyItemChanged(position);
        }
        else if (!previous && checked) {
            notifyItemChanged(position);
        }
    }

    public void toggleItemChecked(int position) {
        setItemChecked(position, !isItemChecked(position));
    }

    public int getCheckedItemCount() {
        int checkedItemCount = 0;
        for (Boolean checkedItem : checkedItems) {
            if (checkedItem) {
                checkedItemCount += 1;
            }
        }
        return checkedItemCount;
    }

    public void refresh() {
        getFilter().filter(filterConstraint);
        photoCache.clear();
    }

    public void refresh(String newFilterConstraint) {
        getFilter().filter(newFilterConstraint);
    }

    public class MessageViewHolder extends RecyclerView.ViewHolder {
        private final QuickContactBadge contactBadge;
        private final View smsContainer;
        private final TextView messageText;
        private final TextView dateText;

        public MessageViewHolder(View itemView, int viewType) {
            super(itemView);

            itemView.setClickable(true);
            itemView.setOnClickListener(activity);
            itemView.setLongClickable(true);
            itemView.setOnLongClickListener(activity);

            if (viewType == ITEM_LEFT_PRIMARY || viewType == ITEM_RIGHT_PRIMARY) {
                contactBadge = (QuickContactBadge) itemView.findViewById(R.id.photo);
                Utils.applyCircularMask(contactBadge);
            }
            else {
                contactBadge = null;
            }
            smsContainer = itemView.findViewById(R.id.sms_container);
            Utils.applyRoundedCornersMask(smsContainer);
            messageText = (TextView) itemView.findViewById(R.id.message);
            dateText = (TextView) itemView.findViewById(R.id.date);
        }

        public QuickContactBadge getContactBadge() {
            return contactBadge;
        }

        public View getSmsContainer() {
            return smsContainer;
        }

        public TextView getMessageText() {
            return messageText;
        }

        public TextView getDateText() {
            return dateText;
        }
    }

    class MessageFilter extends Filter {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            FilterResults results = new FilterResults();
            List<Message> messageResults = new ArrayList<>();

            oldFilterConstraint = filterConstraint;
            filterConstraint = constraint.toString().trim();

            Message[] smses = Database.getInstance(activity.getApplicationContext()).getConversation(
                    preferences.getDid(), contact).getMessages();
            for (Message message : smses) {
                if (message.getText().toLowerCase().contains(filterConstraint.toLowerCase())) {
                    messageResults.add(message);
                }
            }
            Collections.reverse(messageResults);

            results.count = messageResults.size();
            results.values = messageResults;

            return results;
        }

        @Override
        @SuppressWarnings("unchecked")
        protected void publishResults(CharSequence constraint, FilterResults results) {
            List<Message> newMessages = (List<Message>) results.values;

            List<Message> oldMessages = new LinkedList<>();
            oldMessages.addAll(messages);
            for (Message oldMessage : oldMessages) {
                boolean removed = true;
                for (Message newMessage : newMessages) {
                    if (oldMessage.getDatabaseId() != null && newMessage.getDatabaseId() != null &&
                            oldMessage.getDatabaseId().equals(newMessage.getDatabaseId())) {
                        removed = false;
                        break;
                    }
                }

                if (removed) {
                    // Message was removed
                    int index = messages.indexOf(oldMessage);
                    checkedItems.remove(index);
                    messages.remove(index);
                    notifyItemRemoved(index);
                }
            }

            for (int i = 0; i < messages.size(); i++) {
                for (Message newMessage : newMessages) {
                    if (messages.get(i).getDatabaseId() != null && newMessage.getDatabaseId() != null &&
                            messages.get(i).getDatabaseId().equals(newMessage.getDatabaseId())) {
                        if (!messages.get(i).equals(newMessage) ||
                                !oldFilterConstraint.equals(filterConstraint)) {
                            // Message was changed
                            messages.set(i, newMessage);
                            notifyItemChanged(i);
                        }
                    }
                }
            }

            List<Message> sortedMessages = new LinkedList<>();
            sortedMessages.addAll(messages);
            Collections.sort(sortedMessages);
            Collections.reverse(sortedMessages);
            for (int i = 0; i < sortedMessages.size(); i++) {
                if (sortedMessages.get(i) == messages.get(i)) {
                    continue;
                }

                int index = -1;
                for (int j = 0; j < messages.size(); j++) {
                    if (messages.get(j) == sortedMessages.get(i)) {
                        index = j;
                        break;
                    }
                }

                // Message was moved
                checkedItems.add(i, checkedItems.get(index));
                checkedItems.remove(index + 1);
                messages.add(i, messages.get(index));
                messages.remove(index + 1);
                notifyItemMoved(index, i);
            }

            for (int i = 0; i < newMessages.size(); i++) {
                if (messages.size() <= i || !newMessages.get(i).equals(messages.get(i))) {
                    // Message is new
                    checkedItems.add(i, false);
                    messages.add(i, newMessages.get(i));
                    notifyItemInserted(i);
                }
            }

            TextView emptyTextView = (TextView) activity.findViewById(R.id.empty_text);
            if (messages.size() == 0) {
                if (filterConstraint.equals("")) {
                    emptyTextView.setText(applicationContext.getString(R.string.conversation_no_messages));
                }
                else {
                    emptyTextView.setText(applicationContext.getString(R.string.conversation_no_results) + " '" +
                            filterConstraint + "'");
                }
            }
            else {
                emptyTextView.setText("");
            }

            if (messages.size() > 1) {
                if (layoutManager.findLastVisibleItemPosition() >= messages.size() - 2) {
                    layoutManager.scrollToPosition(messages.size() - 1);
                }
            }
        }
    }
}
