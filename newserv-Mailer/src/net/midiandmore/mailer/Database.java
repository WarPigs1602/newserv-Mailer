/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package net.midiandmore.mailer;

import java.sql.*;
import java.util.ArrayList;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author The database class
 */
public class Database {

    private NewservMailer mi;
    private boolean connected;
    private Connection conn;

    protected Database(NewservMailer mi) {
        setMi(mi);
        setConnected(false);
        connect();
    }

    private void connect() {
        var config = getMi().getConfig().getConfigFile();
        var url = "jdbc:postgresql://%s/%s".formatted(config.get("host"), config.get("database"));
        var props = new Properties();
        props.setProperty("user", (String) config.get("user"));
        props.setProperty("password", (String) config.get("password"));
        props.setProperty("ssl", (String) config.get("ssl"));
        try {
            setConn(DriverManager.getConnection(url, props));
            setConnected(true);
        } catch (SQLException ex) {
            Logger.getLogger(Database.class.getName()).log(Level.SEVERE, null, ex);
            setConnected(false);
        }
    }

    /**
     * Fetching emails
     *
     * @return The data
     */
    protected ArrayList<String[]> getMail() {
        var dat = new ArrayList<String[]>();
        try (var statement = getConn().prepareStatement("SELECT mailid, userid, emailtype, prevemail FROM chanserv.email")) {
            try (var resultset = statement.executeQuery()) {
                while (resultset.next()) {
                    var data = new String[4];
                    data[0] = resultset.getString("mailid");
                    data[1] = resultset.getString("userid");
                    data[2] = resultset.getString("emailtype");
                    data[3] = resultset.getString("prevemail");
                    dat.add(data);
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(Database.class.getName()).log(Level.SEVERE, null, ex);
        }
        return dat;
    }

    /**
     * Commits
     */
    protected void commit() {
        try {
            try (var statement = getConn().prepareStatement("COMMIT")) {
                statement.executeUpdate();
            }
        } catch (SQLException ex) {
            Logger.getLogger(Database.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    /**
     * Begins a transaction
     */
    protected void transcation() {
        try {
            try (var statement = getConn().prepareStatement("BEGIN TRANSACTION")) {
                statement.executeUpdate();
            }
        } catch (SQLException ex) {
            Logger.getLogger(Database.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    /**
     * Deletes a mail from the database
     *
     * @param id The id
     */
    protected void delete(String id) {
        try {
            try (var statement = getConn().prepareStatement("DELETE FROM chanserv.email WHERE mailid = ?")) {
                statement.setLong(1, Long.parseLong(id));
                statement.executeUpdate();
            }
        } catch (SQLException ex) {
            Logger.getLogger(Database.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    /**
     * Fetching userdata
     *
     * @param id The id
     * @return The data
     */
    protected String[] getData(String id) {
        var dat = new String[19];
        try (var statement = getConn().prepareStatement("SELECT users.*, code FROM chanserv.users LEFT JOIN chanserv.languages ON language = languageid WHERE id = ?")) {
            statement.setLong(1, Long.parseLong(id));
            try (var resultset = statement.executeQuery()) {
                while (resultset.next()) {
                    dat[0] = resultset.getString("id");
                    dat[1] = resultset.getString("username");
                    dat[2] = resultset.getString("created");
                    dat[3] = resultset.getString("lastauth");
                    dat[4] = resultset.getString("lastemailchng");
                    dat[5] = resultset.getString("flags");
                    dat[6] = resultset.getString("language");
                    dat[7] = resultset.getString("suspendby");
                    dat[8] = resultset.getString("suspendexp");
                    dat[9] = resultset.getString("suspendtime");
                    dat[10] = resultset.getString("lockuntil");
                    dat[11] = resultset.getString("password");
                    dat[12] = resultset.getString("email");
                    dat[13] = resultset.getString("lastemail");
                    dat[14] = resultset.getString("lastuserhost");
                    dat[15] = resultset.getString("suspendreason");
                    dat[16] = resultset.getString("comment");
                    dat[17] = resultset.getString("info");
                    dat[18] = resultset.getString("lastpasschng");
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(Database.class.getName()).log(Level.SEVERE, null, ex);
        }
        return dat;
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

    /**
     * @return the connected
     */
    public boolean isConnected() {
        return connected;
    }

    /**
     * @param connected the connected to set
     */
    public void setConnected(boolean connected) {
        this.connected = connected;
    }

    /**
     * @return the conn
     */
    public Connection getConn() {
        return conn;
    }

    /**
     * @param conn the conn to set
     */
    public void setConn(Connection conn) {
        this.conn = conn;
    }
}
