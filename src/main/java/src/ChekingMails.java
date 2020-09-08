package src;

import com.sun.mail.imap.IMAPFolder;

import java.awt.*;
import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Calendar;
import java.util.Properties;
import java.util.Timer;
import java.util.TimerTask;

import javax.mail.*;
import javax.mail.internet.MimeMultipart;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.*;

public class ChekingMails {

    private static String chek(String host, String user, String password, String messageContent, int day) {
        try {
            Properties properties = new Properties();

            //Met les imaps propeties
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

            //Permet d'avoir le lien pour le cours de la journee
            String[] enseignant = teacherInfo(day);

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

                String baseLink = "https://uqtr.zoom.us/";
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

    private static String getTextFromMimeMessage(Message message) {
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

    private static String getTextFromMimeMultipart(MimeMultipart mimeMultipart) {
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

    private static String[] teacherInfo(int day) {
        JSONParser jsonParser = new JSONParser();
        String[] teacherInfos = new String[2];
        try {
            InputStream inputStream = ChekingMails.class.getClassLoader().getResourceAsStream("teachers.json");
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

    private static String[] getMailInfo() {
        JSONParser jsonParser = new JSONParser();
        String info[] = new String[2];
        try {
            InputStream inputStream = ChekingMails.class.getClassLoader().getResourceAsStream("config.json");
            Reader reader = new InputStreamReader(inputStream);
            Object obj = jsonParser.parse(reader);
            reader.close();

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

    private static void executeOBSTask() {
        //Delay the connection to obs
        new Timer().schedule(
                new TimerTask() {
                    @Override
                    public void run() {
                        OBSRemote obsRemote = new OBSRemote();
                        obsRemote.runStream();
                    }
                }, 5000
        );

        new Timer().schedule(
                new TimerTask() {
                    @Override
                    public void run() {
                        Runtime rt = Runtime.getRuntime();
                        try {
                            rt.exec("taskkill /F /IM obs64.exe");
                            rt.exec("taskkill /F /IM Zoom.exe");
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        System.exit(0);
                    }
                }, 65000
        );
    }

    public static void main(String[] args) throws URISyntaxException, IOException {
        Calendar calendar = Calendar.getInstance();
        System.out.println(calendar.getTime().toString());
        int day = calendar.get(Calendar.DAY_OF_WEEK);

        //Login to imap protocol
        String[] emailCredentials = getMailInfo();
        String host = "outlook.office365.com";
        String username = emailCredentials[0];
        String pass = emailCredentials[1];
        String messageURL = "";

        messageURL = chek(host, username, pass, messageURL, day);
        System.out.println(messageURL);

        //Open the link in the default browser
        if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
            Desktop.getDesktop().browse(new URI(messageURL));
        }

        //Open up a cmd command
        Runtime rt = Runtime.getRuntime();
        rt.exec("cmd /c start cmd.exe /K \"cd /d C:\\Program Files\\obs-studio\\bin\\64bit && start obs64.exe && exit");

        executeOBSTask();
    }
}
