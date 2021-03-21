package src;

import com.google.gson.Gson;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.*;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class GoogleDriveUploader {

    private String getAccessToken() throws IOException, ParseException {
        JSONParser jsonParser = new JSONParser();

        URL url = Main.class.getClassLoader().getResource("configDrive.json");
        InputStream inputStream = url.openStream();
        Reader reader = new InputStreamReader(inputStream);
        Object obj = jsonParser.parse(reader);
        reader.close();

        final URL URL = new URL("https://oauth2.googleapis.com/token");
        final String client_id = ((JSONObject) obj).get("client_id").toString();
        final String client_secret = ((JSONObject) obj).get("client_secret").toString();
        final String refresh_token = ((JSONObject) obj).get("refresh_token").toString();
        final String grant_type = ((JSONObject) obj).get("grant_type").toString();

        HttpURLConnection con = (HttpURLConnection) URL.openConnection();
        con.setRequestMethod("POST");
        con.setDoOutput(true);
        con.setRequestProperty("Content-Type", "application/json; utf-8");
        con.setRequestProperty("Accept", "application/json");

        HashMap<String, String> body = new HashMap<>();
        body.put("client_id", client_id);
        body.put("client_secret", client_secret);
        body.put("refresh_token", refresh_token);
        body.put("grant_type", grant_type);

        Gson gson = new Gson();
        String json = gson.toJson(body);

        try (OutputStream os = con.getOutputStream()) {
            byte[] in = json.getBytes(StandardCharsets.UTF_8);
            os.write(in, 0, in.length);
        }
        StringBuilder response = new StringBuilder();
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(con.getInputStream(), StandardCharsets.UTF_8))) {
            String responseLine = null;
            while ((responseLine = br.readLine()) != null) {
                response.append(responseLine.trim());
            }
            System.out.println(response.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }

        JSONObject jsonObject = gson.fromJson(response.toString(), JSONObject.class);
        String token = jsonObject.get("access_token").toString();
        System.out.println(token);
        return token;
    }

    private String getLocation(File file, String parentFolderID) throws IOException, ParseException {
        String accessToken = getAccessToken();

        String s = "{\"name\":\"" + file.getName() + "\"," +
                "\"modifiedDate\":\"" + file.lastModified() + "\"," +
                "\"mimeType\":\"video/mp4\"," +
                "\"parents\":[\"" + parentFolderID + "\"]}";

        String location = "";

        HttpClient httpClient = HttpClientBuilder.create().build();
        try {
            CloseableHttpClient client = HttpClients.createDefault();
            HttpPost httpPost = new HttpPost("https://www.googleapis.com/upload/drive/v3/files?uploadType=resumable");

            StringEntity entity = new StringEntity(s);

            httpPost.setEntity(entity);
            httpPost.setHeader("Authorization", "Bearer " + accessToken);
            httpPost.setHeader("x-upload-content-type", "video/mp4");
            httpPost.setHeader("content-type", "application/json;");

            CloseableHttpResponse response = client.execute(httpPost);
            Header[] headers = response.getAllHeaders();
            Header h = Arrays.stream(headers)
                    .filter(r -> r.getName().contains("Location"))
                    .findFirst()
                    .get();

            location = h.getValue().replace("Location ", "");
            System.out.println(location);

        } catch (Exception e) {
            e.printStackTrace();
        }

        return location;
    }

    public void upload(File file, String parentFolderID) throws IOException, ParseException {
        String location = getLocation(file, parentFolderID);
        FileBody fileBody = new FileBody(file);
        HttpPut put = new HttpPut(location);

        MultipartEntityBuilder builder = MultipartEntityBuilder.create();
        builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
        builder.addPart("file", fileBody);
        //builder.addBinaryBody("userfile", file);
        HttpEntity entity = builder.build();

        put.setEntity(entity);
        CloseableHttpClient client = HttpClients.createDefault();

        try {
            HttpResponse response = client.execute(put);
            System.out.println(response);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public File getFile(String pathRecording, String folder) {
        List<Path> fileList = new ArrayList<>();
        String slash = OSValidator.isWindows() ? "\\" : "/";

        try (Stream<Path> paths = Files.walk(Paths.get(pathRecording + slash + folder))) {
            fileList = paths.filter(Files::isRegularFile).collect(Collectors.toList());
            fileList.sort(Comparator.comparingLong(o -> o.toFile().lastModified()));
            Collections.reverse(fileList);

        } catch (IOException e) {
            e.printStackTrace();
        }
        return fileList.get(0).toFile();
    }

}
