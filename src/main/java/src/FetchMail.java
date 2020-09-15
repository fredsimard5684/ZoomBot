package src;

import com.sun.mail.imap.IMAPFolder;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import javax.mail.*;
import javax.mail.internet.MimeMultipart;
import java.io.*;
import java.util.Properties;

public class FetchMail {

    private String m_host, m_user, m_password;

    public FetchMail(final String t_host, final String t_user, final String t_password) {
        m_host = t_host;
        m_user = t_user;
        m_password = t_password;
    }

    public String fetch(String messageContent, int day) {
        try {
            Properties properties = new Properties();

            //Met les imaps propeties
            properties.put("mail.imap.host", m_host);
            properties.put("mail.imap.port", " 993");
            properties.put("mail.imap.starttls.enable", "true");
            Session emailSession = Session.getDefaultInstance(properties);

            Store store = emailSession.getStore("imap");
            store.connect(m_host, m_user, m_password);
            Folder emailFolder = (IMAPFolder) store.getFolder("INBOX");
            emailFolder.open(Folder.READ_ONLY);

            Message messages[] = emailFolder.getMessages();
            System.out.println("msg lenght: " + messages.length);

            //Permet d'avoir le lien pour le cours de la journee
            final String[] enseignant = teacherInfo(day);

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

                if (messageFrom.contains(enseignant[0]) || messageFrom.contains(enseignant[1]))
                    System.out.println("Enseignant trouve");
                else
                    continue;
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

                if (!messageContent.startsWith(baseLink))
                    continue;
                else
                    break;
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
        return messageContent;
    }

    private String getTextFromMimeMessage(Message message) {
        String messageContentResult = "";
        try {
            if (message.isMimeType("multipart/*")) {
                MimeMultipart mimeMultipart = (MimeMultipart) message.getContent();
                messageContentResult = getTextFromMimeMultipart(mimeMultipart);
            }
        } catch (MessagingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return messageContentResult;
    }

    private String getTextFromMimeMultipart(MimeMultipart mimeMultipart) {
        String result = "";
        try {
            int count = mimeMultipart.getCount();
            for (int i = 0; i < count; i++) {
                BodyPart bodyPart = mimeMultipart.getBodyPart(i);
                if (bodyPart.isMimeType("text/plain")) {
                    result = result + "\n" + bodyPart.getContent();
                    break; //Maybe??
                } else if (bodyPart.isMimeType("text/html")) {
                    String htmlContent = (String) bodyPart.getContent();
                    //If a part of the body is a mime multipart, recurse the funtion
                } else if (bodyPart.getContent() instanceof MimeMultipart) {
                    result = result + getTextFromMimeMultipart((MimeMultipart) bodyPart.getContent());
                }
            }
        } catch (MessagingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    private String[] teacherInfo(int day) {
        JSONParser jsonParser = new JSONParser();
        String[] teacherInfos = new String[2];
        try {
            InputStream inputStream = Main.class.getClassLoader().getResourceAsStream("teachers.json");
            Reader reader = new InputStreamReader(inputStream);
            JSONArray a = (JSONArray) jsonParser.parse(reader);
            reader.close();

            for (Object o : a) {
                JSONObject prof = (JSONObject) o;
                String dayFetch = prof.get("jour").toString();
                int dayNumber = Integer.parseInt(dayFetch);
                if (dayNumber == day) {
                    teacherInfos[0] = (String) prof.get("nom");
                    teacherInfos[1] = (String) prof.get("mail");
                    break;
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return teacherInfos;
    }
}
