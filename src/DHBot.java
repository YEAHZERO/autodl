
import java.net.URL;
import java.net.URLConnection;

public class DHBot extends MyBot {

    public DHBot() throws Exception {
        super("DHinput.txt");
    }

    @Override
    protected void handleOnConnect() {
        this.joinChannel(input.get("announceChannel"));
    }

    @Override
    protected URLConnection getTorrentUrlConnection(String announce) throws Exception {
        System.out.println(announce);
        if (announce.contains("MP3")) {
            int i = announce.indexOf("- ");
            int i2 = announce.indexOf(" 14(");
            String name = announce.substring(i + 5, i2) + ".torrent";
            int i3 = announce.indexOf("?id=");
            int id = Integer.parseInt(announce.substring(i3 + 4, announce.length() - 1));
            URL url = new URL("https://www.digitalhive.org/download.php?id=" + id + "&https=yes&name=" + name);
            URLConnection con = url.openConnection();
            con.addRequestProperty("Cookie", "uid=159357");
            con.addRequestProperty("Cookie", "pass=e04a41fb9b78b3c6fa9370969578305c");
            return con;
        }
        return null;
    }
}
