/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package net.midiandmore.mailer;

import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import java.util.Properties;

/**
 *
 * @author windo
 */
public class SendMail {

    private NewservMailer mi;

    /**
     *
     * @param master
     */
    public SendMail(NewservMailer mi) {
        setMi(mi);
    }

    /**
     * Sends email
     *
     * @param msg The meassage
     * @param subject The subject
     * @param to The target
     * @throws MessagingException
     */
    protected void sendEmail(String msg, String subject, String to) throws MessagingException {
        var p = getMi().getConfig().getMailFile();
        Session session;
        if (p.get("use_auth").equals("true")) {
            session = Session.getInstance(p,
                    new jakarta.mail.Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(p.getProperty("username"), p.getProperty("password"));
                }
            });
        } else {
            session = Session.getInstance(p);
        }
        Message message = new MimeMessage(session);
        var from = p.getProperty("from-mail-address");
        if (from != null) {
            message.setFrom(new InternetAddress(from));
        }
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
        message.setSubject(subject);
        message.setText(msg);
        Transport.send(message);
    }

    /**
     * @return the mi
     */
    public NewservMailer getMi() {
        return mi;
    }

    /**
     * @param mi the mi to set
     */
    public void setMi(NewservMailer mi) {
        this.mi = mi;
    }
}
