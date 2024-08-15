/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */
package net.midiandmore.mailer;

import jakarta.mail.MessagingException;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import static org.apache.commons.codec.digest.HmacAlgorithms.HMAC_MD5;
import static org.apache.commons.codec.digest.HmacAlgorithms.HMAC_SHA_256;
import org.apache.commons.codec.digest.HmacUtils;

/**
 * The newserv mailer
 *
 * @author Andreas Pschorn
 */
public class NewservMailer {

    private Config config;
    private Database db;
    private SendMail mail;

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        try {
            new NewservMailer();
        } catch (Exception e) {
        }
    }

    protected NewservMailer() {
        init();
    }

    private void init() {
        try {
            setConfig(new Config(this, "config.json"));
            setDb(new Database(this));
            setMail(new SendMail(this));
            System.out.printf("Started newserv-Mailer:\n");
            while (true) {
                var arr = getDb().getMail();
                for (var rows : arr) {
                    email(rows[1], rows[2], rows[3]);
                    getDb().transcation();
                    getDb().delete(rows[0]);
                    getDb().commit();
                }
                Thread.sleep(5000);
            }
        } catch (Exception ex) {
            Logger.getLogger(NewservMailer.class.getName()).log(Level.SEVERE, null, ex);
            System.exit(1);
        }
    }

    private void email(String userid, String emailtype, String prevemail) {
        var one = getDb().getData(userid);
        for (var i = 0; i < one.length; i++) {
            if (one[i] == null) {
                return;
            }
        }
        if (prevemail == null) {
            prevemail = "";
        }
        var email = one[12];
        var template = (String[]) getConfig().getTemplateFile().get(emailtype);
        var msg = parseTemplate(template[1], one, prevemail);
        var subject = parseTemplate(template[0], one, prevemail);
        try {
            getMail().sendEmail(msg, subject, email);
            System.out.printf("Email with subject: %s, sended to : %s\n", subject, email);
            System.out.flush();
        } catch (MessagingException ex) {
            Logger.getLogger(NewservMailer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private String[] generateResetcode(String[] obj) {
        var arr = new String[3];
        if (obj[10].equals("0")) {
            arr[0] = "LOCK UNTIL NOT SET. STAFF ACCOUNT'S CAN'T USE RESET";
            arr[1] = "never";
            arr[2] = "";
        } else {
            arr[1] = new HmacUtils(HMAC_SHA_256, obj[1]).hmacHex(obj[10]);
            arr[0] = new Date(Long.parseLong(obj[10]) * 1000).toString();
            arr[2] = "/MSG %(config.bot)s RESET #%(user.username)s %(resetcode)s";
        }
        return arr;
    }

    private String parseTemplate(String content, String[] one, String prevemail) {
        var code = generateResetcode(one);
        content = content.replace("%(resetline)s", code[2]);
        content = content.replace("%(resetcode)s", code[1]);
        content = content.replace("%(lockuntil)s", code[0]);
        content = content.replace("%(config.bot)s", getConfig().getConfigFile().getProperty("bot"));
        content = content.replace("%(user.email)s", one[12]);
        content = content.replace("%(user.username)s", one[1]);
        content = content.replace("%(prevemail)s", prevemail);
        content = content.replace("%(user.password)s", one[11]);
        content = content.replace("%(config.siteurl)s", getConfig().getConfigFile().getProperty("siteurl"));
        content = content.replace("%(config.server)s", getConfig().getConfigFile().getProperty("server"));
        content = content.replace("%(config.network)s", getConfig().getConfigFile().getProperty("network"));
        content = content.replace("%(config.securityurl)s", getConfig().getConfigFile().getProperty("securityurl"));
        return content;
    }

    /**
     * @return the config
     */
    public Config getConfig() {
        return config;
    }

    /**
     * @param config the config to set
     */
    public void setConfig(Config config) {
        this.config = config;
    }

    /**
     * @return the db
     */
    public Database getDb() {
        return db;
    }

    /**
     * @param db the db to set
     */
    public void setDb(Database db) {
        this.db = db;
    }

    /**
     * @return the mail
     */
    public SendMail getMail() {
        return mail;
    }

    /**
     * @param mail the mail to set
     */
    public void setMail(SendMail mail) {
        this.mail = mail;
    }
}
