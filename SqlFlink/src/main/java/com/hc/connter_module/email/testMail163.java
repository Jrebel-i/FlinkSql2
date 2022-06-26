package com.hc.connter_module.email;

import com.sun.mail.imap.IMAPStore;

import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.event.MessageCountAdapter;
import javax.mail.event.MessageCountEvent;
import java.util.HashMap;
import java.util.Properties;

public class testMail163 {
    public static void main(String[] args) throws Exception {
        // 准备连接服务器的会话信息
        Properties props = new Properties();
        props.setProperty("mail.store.protocol", "imap");
        props.setProperty("mail.imap.host", "imap.163.com");
        props.setProperty("mail.imap.port", "143");
        HashMap<String, String> IAM = new HashMap<String, String>();
        //带上IMAP ID信息，由key和value组成，例如name，version，vendor，support-email等。
        IAM.put("name", "myname");
        IAM.put("version", "1.0.0");
        IAM.put("vendor", "myclient");
        IAM.put("support-email", "testmail@test.com");
        Session session1 = Session.getInstance(props);
        IMAPStore store = (IMAPStore) session1.getStore("imap");
        store.connect("jrebel_i@163.com", "SRBKZFVQCHFLCZJP");//账号 //授权码
        store.id(IAM);
        // 获得收件箱
        Folder folder = store.getFolder("INBOX");
        // 以读写模式打开收件箱
        folder.open(Folder.READ_ONLY);

        // 获得收件箱的邮件列表
        Message[] messages = folder.getMessages();
        // 打印不同状态的邮件数量
        System.out.println("收件箱中共" + messages.length + "封邮件!");
        System.out.println("收件箱中共" + folder.getUnreadMessageCount() + "封未读邮件!");
        System.out.println("收件箱中共" + folder.getNewMessageCount() + "封新邮件!");
        System.out.println("收件箱中共" + folder.getDeletedMessageCount() + "封已删除邮件!");

    }
}
