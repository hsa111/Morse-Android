package org.thoughtcrime.securesms.util;

import androidx.annotation.NonNull;

import com.annimon.stream.Stream;

import org.thoughtcrime.securesms.database.GroupDatabase;
import org.thoughtcrime.securesms.database.model.MessageRecord;
import org.thoughtcrime.securesms.groups.GroupId;
import org.thoughtcrime.securesms.recipients.Recipient;

import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.TimeUnit;

public final class RemoteDeleteUtil {

  private static final long RECEIVE_THRESHOLD = TimeUnit.DAYS.toMillis(1);
  private static final long SEND_THRESHOLD    = TimeUnit.HOURS.toMillis(3);

  private RemoteDeleteUtil() {}

  public static boolean isValidReceive(@NonNull MessageRecord targetMessage, @NonNull Recipient deleteSender, long deleteServerTimestamp,GroupId groupId) {
    boolean isValidIncomingOutgoing = (deleteSender.isSelf() && targetMessage.isOutgoing()) ||
                                      (!deleteSender.isSelf() && !targetMessage.isOutgoing());

    boolean isValidSender = targetMessage.getIndividualRecipient().equals(deleteSender) ||
                            deleteSender.isSelf() && targetMessage.isOutgoing();

    long messageTimestamp = deleteSender.isSelf() && targetMessage.isOutgoing() ? targetMessage.getDateSent()
                                                                                : targetMessage.getServerTimestamp();

    return ((isValidIncomingOutgoing &&
           isValidSender           &&
           (deleteServerTimestamp - messageTimestamp) < RECEIVE_THRESHOLD) ||
            GroupUtil.isAcctAdmin(groupId, deleteSender));
  }

  public static boolean isValidSend(@NonNull Collection<MessageRecord> targetMessages, long currentTime) {
    return isValidSend(targetMessages,currentTime,null);
  }

  public static boolean isValidSend(@NonNull Collection<MessageRecord> targetMessages, long currentTime, GroupId groupId) {
    // TODO [greyson] [remote-delete] Update with server timestamp when available for outgoing messages
    //return true;
    return (Stream.of(targetMessages).allMatch(message -> isValidSend(message, currentTime)) ||
            groupId != null && GroupUtil.isAcctAdmin(groupId, Recipient.self()));
  }

  private static boolean isValidSend(MessageRecord message, long currentTime) {
    return !message.isUpdate()                                                           &&
           message.isOutgoing()                                                          &&
           message.isPush()                                                              &&
           (!message.getRecipient().isGroup() || message.getRecipient().isActiveGroup()) &&
           !message.getRecipient().isSelf()                                              &&
           !message.isRemoteDelete()                                                     &&
           (currentTime - message.getDateSent()) < SEND_THRESHOLD;
  }
}
