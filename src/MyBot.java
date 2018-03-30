
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.Iterator;
import java.util.LinkedHashMap;
import org.apache.http.HttpEntity;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.HttpClientBuilder;
import org.jibble.pircbot.PircBot;

public abstract class MyBot extends PircBot {

    private CredentialsProvider provider = new BasicCredentialsProvider();
    private UsernamePasswordCredentials credentials;
    private HttpClient client;
    private HttpPost httpPost;
    private ContentType ct = ContentType.create("application/x-bittorrent");
    protected LinkedHashMap<String, String> input = new LinkedHashMap() {
        {
            put("ftpNick", "");
            put("ftpPass", "");
            put("ftpWatchDir", "");
            put("rutNick", "");
            put("rutPass", "");
            put("rutLink", "");
            put("localWatchDir", "");
            put("ircNick", "");
            put("ircServer", "");
            put("botName", "");
            put("announceChannel", "");
        }
    };

    public MyBot(String inputName) throws Exception {
        addAdditionalInput();
        getInput(inputName);
        handleHttpClient();
        //setVerbose(true);
        this.setName(input.get("ircNick"));
    }

    protected void addAdditionalInput() {

    }

    public void start() throws Exception {
        this.connect(input.get("ircServer"));
    }

    @Override
    public void onConnect() {
        handleOnConnect();
    }

    protected abstract void handleOnConnect();

    @Override
    public void onInvite(String targetNick, String sourceNick, String sourceLogin, String sourceHostname, String channel) {
        if (channel.equalsIgnoreCase(input.get("announceChannel"))) {
            this.joinChannel(channel);
        }
    }

    @Override
    public void onJoin(String channel, String sender, String login, String hostname) {
        if (sender.equals(this.getName())) {
            this.setMode(this.getName(), "+B");
        }
    }

    @Override
    public void onMessage(String channel, String sender,
            String login, String hostname, String message) {
        if (sender.equalsIgnoreCase(input.get("botName"))) {
            try {
                byte[] b = getTorrent(getTorrentUrlConnection(message));
                if (b != null) {
                    handleTorrentUpload(b);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    protected abstract URLConnection getTorrentUrlConnection(String announce) throws Exception;

    @Override
    public void onDisconnect() {
        while (!isConnected()) {
            try {
                Thread.sleep(15000);
                reconnect();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void getInput(String inputName) throws Exception {
        File f = new File(inputName);
        BufferedReader b = new BufferedReader(new FileReader(f));
        String readLine;
        Iterator<String> keys = input.keySet().iterator();
        while ((readLine = b.readLine()) != null) {
            if (keys.hasNext()) {
                input.put(keys.next(), readLine.substring(readLine.indexOf("=") + 1).replace(" ", ""));
            }
        }
        b.close();
    }

    private void handleHttpClient() {
        if (getOutputMode().equals("rutLink")) {
            credentials = new UsernamePasswordCredentials(input.get("rutNick"), input.get("rutPass"));
            httpPost = new HttpPost(input.get("rutLink") + "/php/addtorrent.php");
            provider.setCredentials(AuthScope.ANY, credentials);
            client = HttpClientBuilder.create()
                    .setDefaultCredentialsProvider(provider)
                    .build();
        }
    }

    private byte[] getTorrent(URLConnection con) throws Exception {
        if (con != null) {
            InputStream is = con.getInputStream();
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            int nRead;
            byte[] data = new byte[16384];
            while ((nRead = is.read(data, 0, data.length)) != -1) {
                buffer.write(data, 0, nRead);
            }
            buffer.flush();
            return buffer.toByteArray();
        } else {
            return null;
        }
    }

    private void feedToRutorrent(byte[] b) throws Exception {
        HttpEntity entity = MultipartEntityBuilder
                .create()
                .addBinaryBody("torrent_file", b, ct, System.currentTimeMillis() + ".torrent")
                .build();
        httpPost.setEntity(entity);
        client.execute(httpPost);
    }

    private void sendToWatch(byte[] b) throws Exception {
        FileOutputStream fos = new FileOutputStream(input.get("localWatchDir") + "/" + System.currentTimeMillis() + ".torrent");
        fos.write(b);
        fos.close();
    }

    private void sendToRemoteWatch(byte[] b) throws Exception {
        int i = input.get("ftpWatchDir").indexOf("ftp://");
        String ftpUrl,
                user = URLEncoder.encode(input.get("ftpNick"), "UTF-8"),
                pass = URLEncoder.encode(input.get("ftpPass"), "UTF-8");
        ftpUrl = "ftp://" + user + ":" + pass + "@";
        if (i > -1) {
            ftpUrl += input.get("ftpWatchDir").substring(i + 6);
        } else {
            ftpUrl += input.get("ftpWatchDir");
        }
        ftpUrl += "/" + System.currentTimeMillis() + ".torrent;type=i";
        URL url = new URL(ftpUrl);
        URLConnection conn = url.openConnection();
        OutputStream outputStream = conn.getOutputStream();
        outputStream.write(b);
        outputStream.close();
    }

    private String getOutputMode() {
        if (!input.get("rutLink").equals("none")) {
            return "rutLink";
        } else if (!input.get("ftpWatchDir").equals("none")) {
            return "ftpWatchDir";
        } else {
            return "localWatchDir";
        }
    }

    private void handleTorrentUpload(byte[] b) throws Exception {
        switch (getOutputMode()) {
            case "rutLink":
                feedToRutorrent(b);
                break;
            case "ftpWatchDir":
                sendToRemoteWatch(b);
                break;
            default:
                sendToWatch(b);
                break;
        }
    }
}
