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
import java.util.Calendar;

public class Teachers {

    private String name;
    private String mail;
    private String folder;
    private String weeklyLink;
    private String driveFolderID;

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
        int hour = calendar.get(Calendar.HOUR_OF_DAY);

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
                timeFetch = timeFetch.substring(0, timeFetch.indexOf("h"));
                System.out.println(timeFetch);
                int dayNumber = Integer.parseInt(dayFetch);
                int timeNumber = 0;
                if (!timeFetch.equals("")) {
                    timeNumber = Integer.parseInt(timeFetch);
                    System.out.println(timeNumber);
                    System.out.println(hour);
                }

                if (dayNumber == day) {
                    if (timeNumber != 0)
                        if (!(hour == timeNumber || hour + 1 == timeNumber)) {
                            System.out.println("Rien a cette heure");
                            continue;
                        }
                    name = (String) prof.get("nom");
                    mail = (String) prof.get("mail");
                    folder = (String) prof.get("folder");
                    weeklyLink = (String) prof.get("weeklyLink");
                    driveFolderID = (String) prof.get("driveFolderID");
                    break;
                }
            }

        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }
    }
}
