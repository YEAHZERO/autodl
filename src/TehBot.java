
import java.net.URL;
import java.net.URLConnection;

public class TehBot extends MyBot {

    public TehBot() throws Exception {
        super("TEHinput.txt");
    }
    
    @Override
    protected void addAdditionalInput() {
        input.put("authKey", "");
        input.put("torrentPass", "");
        input.put("siteNick", "");
        input.put("ircKey", "");
        input.put("ircAuth", "");
        input.put("maxSize", "");
    }
    
    @Override
    protected void handleOnConnect() {
        this.identify(input.get("ircAuth"));
        this.sendMessage(input.get("botName"), "auth " + input.get("siteNick") + " " + input.get("ircKey"));
    }
    
    @Override
    protected URLConnection getTorrentUrlConnection(String announce) throws Exception {
        int sizeStartIndex = announce.indexOf("::") + 3;
        int sizeEndIndex = announce.indexOf("B", sizeStartIndex) - 2;
        double maxSize = Double.parseDouble(input.get("maxSize"));
        if (announce.substring(sizeStartIndex, sizeEndIndex + 3).contains("MB")) {
            maxSize *= 1024;
        }
        if (Double.valueOf(announce.substring(sizeStartIndex, sizeEndIndex)) <= maxSize) {
            URL website = new URL("https://tehconnection.eu/torrents.php?action=download&authkey=" + input.get("authKey") + "&torrent_pass=" + input.get("torrentPass") + "&id=" + announce.substring(announce.indexOf("tid=") + 4));
            return website.openConnection();
        } else {
            return null;
        }
    }
}
