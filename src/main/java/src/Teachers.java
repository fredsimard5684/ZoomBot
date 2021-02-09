package src;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.time.Duration;
import java.time.LocalTime;
import java.util.Calendar;

public class Teachers {

    private String name = "";
    private String mail = "";
    private String folder = "";
    private String weeklyLink = "";
    private String driveFolderID = "";

    public Teachers() {
        teacherInfo();
    }


    public String getName() {
        return name;
    }

    public String getMail() {
        return mail;
    }

    public String getFolder() {
        return folder;
    }

    public String getWeeklyLink() {
        return weeklyLink;
    }

    public String getDriveFolderID() {
        return driveFolderID;
    }

    private void teacherInfo() {
        Calendar calendar = Calendar.getInstance();
        System.out.println(calendar.getTime().toString());

        int day = calendar.get(Calendar.DAY_OF_WEEK);

        JSONParser jsonParser = new JSONParser();

        try {
            URL url = Main.class.getClassLoader().getResource("teachers.json");
            InputStream inputStream = url.openStream();
            Reader reader = new InputStreamReader(inputStream);
            JSONArray a = (JSONArray) jsonParser.parse(reader);
            reader.close();

            for (Object o : a) {
                JSONObject prof = (JSONObject) o;
                String dayFetch = prof.get("jour").toString();
                String timeFetch = prof.get("heure").toString();

                int dayNumber = Integer.parseInt(dayFetch);

                if (dayNumber == day) {
                    long timeElapsed = timeElapsed(timeFetch);
                    if (timeElapsed > 5 || timeElapsed < -2) {
                        System.out.println("Rien a cette heure");
                    } else {
                        name = (String) prof.get("nom");
                        mail = (String) prof.get("mail");
                        folder = (String) prof.get("folder");
                        weeklyLink = (String) prof.get("weeklyLink");
                        driveFolderID = (String) prof.get("driveFolderID");
                        break;
                    }
                }
            }

        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }
    }

    private long timeElapsed(String time) {
        String t = time.replace("h", ":");
        LocalTime l1 = LocalTime.parse(t);
        LocalTime l2 = LocalTime.now();
        long elapsedMinutes = Duration.between(l2, l1).toMinutes();
        System.out.println(elapsedMinutes);
        return elapsedMinutes;
    }

}
