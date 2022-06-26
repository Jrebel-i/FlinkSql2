package com.hc.connter_module.email.done;

import com.sun.mail.imap.IMAPFolder;
import com.sun.mail.imap.IMAPStore;
import org.apache.flink.streaming.api.functions.source.RichSourceFunction;
import org.apache.flink.table.data.GenericRowData;
import org.apache.flink.table.data.RowData;
import org.apache.flink.table.data.StringData;
import org.apache.flink.table.data.TimestampData;

import javax.mail.*;
import javax.mail.event.MessageCountAdapter;
import javax.mail.event.MessageCountEvent;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

public class ImapSource extends RichSourceFunction<RowData> {
    private final ImapSourceOptions options;
    private final List<String> columnNames;

    private transient IMAPStore store;
    private transient IMAPFolder folder;

    private transient volatile boolean running = false;

    public ImapSource(
        ImapSourceOptions options,
        List<String> columnNames
    ) {
        this.options = options;
        this.columnNames = columnNames.stream()
            .map(String::toUpperCase)
            .collect(Collectors.toList());
    }

    @Override
    public void run(SourceContext<RowData> ctx) throws Exception {
//        connect();
//        running = true;
//
//        folder.addMessageCountListener(new MessageCountAdapter() {
//            @Override
//            public void messagesAdded(MessageCountEvent e) {
//                collectMessages(ctx, e.getMessages());
//            }
//        });
//
//        while (running) {
//            // Trigger some IMAP request to force the server to send a notification
//            folder.getMessageCount();
//            Thread.sleep(250);
//        }
        running = true;
        while (running){
            ctx.collect(GenericRowData.of(
                    StringData.fromString("Subject 1"),
                    System.currentTimeMillis()
            ));
            Thread.sleep(600);
        }
    }

    @Override
    public void cancel() {
        running = false;
    }

    @Override
    public void close() throws Exception {
        if (folder != null) {
            folder.close();
        }

        if (store != null) {
            store.close();
        }
    }

    private void collectMessages(SourceContext<RowData> ctx, Message[] messages) {
        for (Message message : messages) {
            try {
                collectMessage(ctx, message);
            } catch (MessagingException ignored) {}
        }
    }

    private void collectMessage(SourceContext<RowData> ctx, Message message)
        throws MessagingException {
        final GenericRowData row = new GenericRowData(columnNames.size());

        for (int i = 0; i < columnNames.size(); i++) {
            switch (columnNames.get(i)) {
                case "SUBJECT":
                    row.setField(i, StringData.fromString(message.getSubject()));
                    break;
                case "SENT":
                    row.setField(i, TimestampData.fromInstant(message.getSentDate().toInstant()));
                    break;
                case "RECEIVED":
                    row.setField(i, TimestampData.fromInstant(message.getReceivedDate().toInstant()));
                    break;
                // ...
            }
        }

        ctx.collect(row);
    }

    private void connect() throws Exception {
        final Session session = Session.getInstance(getSessionProperties(), null);
        store = (IMAPStore) session.getStore("imap");
        HashMap<String, String> IAM = new HashMap<String, String>();
        //带上IMAP ID信息，由key和value组成，例如name，version，vendor，support-email等。
        IAM.put("name", "myname");
        IAM.put("version", "1.0.0");
        IAM.put("vendor", "myclient");
        IAM.put("support-email", "testmail@test.com");
        store.connect(options.getUser(), options.getPassword());
        store.id(IAM);

        final Folder genericFolder = store.getFolder("INBOX");
        folder = (IMAPFolder) genericFolder;

        if (!folder.isOpen()) {
            folder.open(Folder.READ_ONLY);
        }
    }

    private Properties getSessionProperties() {
        Properties props = new Properties();
        props.put("mail.store.protocol", "imap");
        props.put("mail.imap.auth", true);
        props.put("mail.imap.host", options.getHost());
        if (options.getPort() != null) {
            props.put("mail.imap.port", options.getPort());
        }

        return props;
    }
}
