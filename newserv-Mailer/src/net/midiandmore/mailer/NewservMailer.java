/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */
package net.midiandmore.mailer;

import jakarta.mail.MessagingException;
import java.nio.charset.Charset;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import org.apache.commons.codec.binary.Hex;
import static org.apache.commons.codec.digest.DigestUtils.sha256Hex;
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

    private String generateUrl(String[] obj) {
        var r = String.valueOf(new Random(4).nextInt()).getBytes();
        var uname = obj[1];
        var password = obj[11];
        var key = getConfig().getConfigFile().getProperty("urlkey");
        var a = MD5("%s %s".formatted(r, key));
        try {
            a = Hex.encodeHexString(RC4(a, password));
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException | BadPaddingException | InvalidAlgorithmParameterException ex) {
            Logger.getLogger(NewservMailer.class.getName()).log(Level.SEVERE, null, ex);
        }
        var b = MD5(MD5("%s %s %s %s".formatted(getConfig().getConfigFile().getProperty("urlsecret"), uname, a, r)));
        return "%s?m=%s&h=%s&u=%s&r=%s".formatted(getConfig().getConfigFile().getProperty("url"), a, b, Hex.encodeHexString(uname.getBytes()), Hex.encodeHexString(r));
    }

    private String MD5(String text) {
        MessageDigest messageDigest = null;
        try {
            messageDigest = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException ex) {
            Logger.getLogger(NewservMailer.class.getName()).log(Level.SEVERE, null, ex);
        }
        messageDigest.reset();
        messageDigest.update(text.getBytes(Charset.forName("UTF8")));
        var resultByte = messageDigest.digest();
        return Hex.encodeHexString(resultByte);
    }

    private String generateActivationUrl(String[] obj) {
        var r = Hex.encodeHexString(String.valueOf(new Random(16).nextInt()).getBytes());
        var uid = Integer.parseInt(obj[0]);
        var uname = obj[1];
        var password = obj[11];
        var key = getConfig().getConfigFile().getProperty("activationkey");
        String a = null;
        var hex = sha256Hex("%s %s %s".formatted(r, key, password));
        var rc4 = Hex.encodeHexString(password.getBytes());
        try {
            a = Hex.encodeHexString(RC4(hex, rc4));
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException | BadPaddingException | InvalidAlgorithmParameterException ex) {
            Logger.getLogger(NewservMailer.class.getName()).log(Level.SEVERE, null, ex);
        }
        var hd = new HmacUtils(HMAC_SHA_256, "%s %s".formatted(r, key))
                .hmacHex("%d %s %s".formatted(uid, uname, a));
        return "%s?id=%d&h=%s&r=%s&u=%s&p=%s".formatted(getConfig().getConfigFile().getProperty("activationurl"), uid, hd, r, Hex.encodeHexString(uname.getBytes()), a);
    }

    private byte[] RC4(String text, String part2) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException {
        byte[] part = text.getBytes();
        SecretKey key = new SecretKeySpec(part, "RC4");
        // Create Cipher instance and initialize it to encrytion mode
        Cipher cipher = Cipher.getInstance("RC4");  // Transformation of the algorithm
        cipher.init(Cipher.ENCRYPT_MODE, key);
        byte[] cipherBytes = cipher.doFinal(part2.getBytes());
        return cipherBytes;
    }

    private void email(String userid, String emailtype, String prevemail) {
        var one = getDb().getData(userid);
        for (String one1 : one) {
            if (one1 == null) {
                return;
            }
        }
        if (prevemail == null) {
            prevemail = "";
        }
        var email = one[12];
        var template = (String[]) getConfig().getTemplateFile().get(emailtype);
        var msg = parseTemplate(template[1], one, prevemail, Integer.parseInt(emailtype));
        var subject = parseTemplate(template[0], one, prevemail, Integer.parseInt(emailtype));
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
            arr[1] = new HmacUtils(HMAC_SHA_256, "%s:codegenerator".formatted(getConfig().getConfigFile().getProperty("q9secret")))
                    .hmacHex("%s:%s".formatted(obj[1], obj[10]));
            arr[0] = new Date(Long.parseLong(obj[10]) * 1000).toString();
            arr[2] = "/MSG %(config.bot)s RESET #%(user.username)s %(resetcode)s";
        }
        return arr;
    }

    private String parseTemplate(String content, String[] one, String prevemail, int mailtype) {
        String[] code = {"", "", ""};
        var url = "";
        if (mailtype == 1) {
            url = generateUrl(one);
        }
        if (mailtype == 3) {
            code = generateResetcode(one);
        }
        if (mailtype == 5) {
            code = generateResetcode(one);
        }
        if (mailtype == 6) {
            url = generateActivationUrl(one);
        }
        content = content.replace("%(url)s", url);
        content = content.replace("%(resetline)s", code[2]);
        content = content.replace("%(resetcode)s", code[1]);
        content = content.replace("%(lockuntil)s", code[0]);
        content = content.replace("%(config.cleanup)d", getConfig().getConfigFile().getProperty("cleanup"));
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
