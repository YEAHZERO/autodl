
import java.net.URL;
import java.net.URLConnection;

public class RedBot extends MyBot {
    
    private int counter = 0;

    public RedBot() throws Exception {
        super("REDinput.txt");
    }

    @Override
    protected void addAdditionalInput() {
        input.put("authKey", "");
        input.put("torrentPass", "");
        input.put("siteNick", "");
        input.put("ircKey", "");
    }

    @Override
    protected void handleOnConnect() {
        this.sendMessage(input.get("botName"), "enter " + input.get("announceChannel") + " " + input.get("siteNick") + " " + input.get("ircKey"));
    }

    @Override
    protected URLConnection getTorrentUrlConnection(String announce) throws Exception {
        if (counter < 37 && announce.contains("[2017]") && announce.contains("FLAC") && !announce.contains("[Single]")) {
            int id_index = announce.indexOf("download&id=") + 12;
            int space_index = announce.indexOf(" ", id_index);
            int id;
            if (space_index != -1) {
                id = Integer.parseInt(announce.substring(id_index, space_index));
            } else {
                id = Integer.parseInt(announce.substring(id_index));
            }
            URL website = new URL("https://redacted.ch/torrents.php?action=download&id=" + id + "&authkey=" + input.get("authKey") + "&torrent_pass=" + input.get("torrentPass") + "&usetoken=1");
            counter++;
            return website.openConnection();
        } else {
            return null;
        }
    }
    
}
