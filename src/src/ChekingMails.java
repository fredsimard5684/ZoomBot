package src;

import com.sun.mail.imap.IMAPFolder;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.NoSuchProviderException;
import javax.mail.Session;
import javax.mail.Store;

import org.json.simple.JSONObject;
import org.json.simple.parser.*;

public class ChekingMails {

    public static void chek(String host, String user, String password) {
        try {
            Properties properties = new Properties();

            properties.put("mail.imap.host", host);
            properties.put("mail.imap.port", " 993");
            properties.put("mail.imap.starttls.enable", "true");
            Session emailSession = Session.getDefaultInstance(properties);

            Store store = emailSession.getStore("imap");
            store.connect(host, user, password);
            Folder emailFolder = (IMAPFolder) store.getFolder("INBOX");
            emailFolder.open(Folder.READ_ONLY);

            Message messages[] = emailFolder.getMessages();
            System.out.println("msg lenght: " + messages.length);

            for (int i = messages.length - 1; i > messages.length - 20; i--) {
                Message message = messages[i];
                System.out.println("---------------------------------");
                System.out.println("Email Number " + (i - 1));
                System.out.println("Subject: " + message.getSubject());
                System.out.println("From: " + message.getFrom()[0]);
                System.out.println("Text: " + message.getContent().toString());

            }

            emailFolder.close();
            store.close();
        } catch (NoSuchProviderException e) {
            e.printStackTrace();
        } catch (MessagingException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String[] getMailInfo() {
        JSONParser jsonParser = new JSONParser();
        String info[] = new String[2];
        try {
            Object obj = jsonParser.parse(new FileReader("./.idea/config.json"));

            JSONObject jsonObject = (JSONObject) obj;

            String email = ((JSONObject) obj).get("mail").toString();
            String pass = ((JSONObject) obj).get("pass").toString();

            info[0] = email;
            info[1] = pass;

        } catch (ParseException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return info;
    }

    public static void main(String[] args) {
        String[] emailCredentials = getMailInfo();
        String host = "outlook.office365.com";
        String username = emailCredentials[0];
        String pass = emailCredentials[1];

        chek(host, username, pass);

    }
}
