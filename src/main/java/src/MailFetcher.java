package src;

import com.sun.mail.imap.IMAPFolder;

import javax.mail.*;
import javax.mail.internet.MimeMultipart;
import java.io.*;
import java.util.Properties;

public class MailFetcher {

    private final LoginInformation loginInformation;
    private final Teachers teachers;

    public MailFetcher(LoginInformation loginInformation, Teachers teachers) {
        this.loginInformation = loginInformation;
        this.teachers = teachers;
    }

    public String gatheringCorrectLink() {

        String messageURL = "";
        messageURL = fetch(messageURL, teachers);

        System.out.println(messageURL);

        //Remove the &amp; after pwd
        if (messageURL.contains("amp;")) {
            messageURL = messageURL.replace("amp;", "");
        }
        System.out.println(messageURL);
        return messageURL;
    }

    private String fetch(String messageContent, Teachers teachers) {
        try {
            if (!teachers.getWeeklyLink().equals("")) return teachers.getWeeklyLink();

            Properties properties = new Properties();

            //Met les imaps propeties
            properties.put("mail.imap.host", loginInformation.getEmailHost());
            properties.put("mail.imap.port", " " + loginInformation.getPort());
            properties.put("mail.imap.starttls.enable", "true");
            Session emailSession = Session.getDefaultInstance(properties);

            Store store = emailSession.getStore("imap");
            store.connect(loginInformation.getEmailHost(), loginInformation.getEmail(), loginInformation.getPass());
            Folder emailFolder = (IMAPFolder) store.getFolder("INBOX");
            emailFolder.open(Folder.READ_ONLY);

            Message[] messages = emailFolder.getMessages();
            System.out.println("msg lenght: " + messages.length);

            //Permet d'avoir le lien pour le cours de la journee
            //final String[] enseignant = teacherInfo();

            for (int i = messages.length - 1; i > 0; i--) {
                Message message = messages[i];
                System.out.println("---------------------------------");
                System.out.println("Email Number " + (i - 1));
                System.out.println("Subject: " + message.getSubject());
                System.out.println("From: " + message.getFrom()[0].toString());

                String messageFrom = message.getFrom()[0].toString();
                messageContent = message.getContent().toString();

                if (messageContent.startsWith("javax.mail")) {
                    messageContent = getTextFromMimeMessage(message);
                }

                if (messageFrom.contains(teachers.getName()) || messageFrom.contains(teachers.getMail()))
                    System.out.println("Enseignant trouve");
                else continue;
                //URL de base pour zoom

                final String baseLink = "https://uqtr.zoom.us/";
                if (!messageContent.contains(baseLink))
                    continue;

                if (messageContent.contains("<a href=\"")) {
                    messageContent = messageContent.replace("<a href=\"", " ");
                    messageContent = messageContent.replace("\">", " ");
                }
                messageContent = messageContent.replaceAll("\\<.*?\\>", " ");

                String[] possibleLinks = messageContent.split("\\s+");

                //Recherche le bon lien
                for (String link : possibleLinks) {
                    if (link.startsWith(baseLink)) {
                        if (link.startsWith(baseLink + "meeting") || link.startsWith(baseLink + "rec"))
                            continue;
                        else {
                            messageContent = link;
                            break;
                        }
                    }
                }

                if (!messageContent.startsWith(baseLink)) continue;
                else break;
            }

            emailFolder.close(true);
            store.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return messageContent;
    }

    private String getTextFromMimeMessage(Message message) {
        String messageContentResult = "";
        try {
            if (message.isMimeType("multipart/*")) {
                MimeMultipart mimeMultipart = (MimeMultipart) message.getContent();
                messageContentResult = getTextFromMimeMultipart(mimeMultipart);
            }
        } catch (MessagingException | IOException e) {
            e.printStackTrace();
        }
        return messageContentResult;
    }

    private String getTextFromMimeMultipart(MimeMultipart mimeMultipart) {
        StringBuilder result = new StringBuilder();
        try {
            int count = mimeMultipart.getCount();
            for (int i = 0; i < count; i++) {
                BodyPart bodyPart = mimeMultipart.getBodyPart(i);
                if (bodyPart.isMimeType("text/plain")) {
                    result.append("\n").append(bodyPart.getContent());
                    break; //Maybe??
                } else if (bodyPart.isMimeType("text/html")) {
                    String htmlContent = (String) bodyPart.getContent();
                    //If a part of the body is a mime multipart, recurse the funtion
                } else if (bodyPart.getContent() instanceof MimeMultipart) {
                    result.append(getTextFromMimeMultipart((MimeMultipart) bodyPart.getContent()));
                }
            }
        } catch (MessagingException | IOException e) {
            e.printStackTrace();
        }
        return result.toString();
    }
}
