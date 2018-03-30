
import java.net.URL;
import java.net.URLConnection;

public class OpBot extends MyBot {

    public OpBot() throws Exception {
        super("OTinput.txt");
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
        this.sendMessage(input.get("botName"), "KNOCK " + input.get("announceChannel") + " " + input.get("siteNick") + " " + input.get("ircKey"));
    }

    @Override
    protected URLConnection getTorrentUrlConnection(String announce) throws Exception {
        int id_index = announce.indexOf("download&id=") + 12;
        int space_index = announce.indexOf(" ", id_index);
        int id;
        if (space_index != -1) {
            id = Integer.parseInt(announce.substring(id_index, space_index));
        } else {
            id = Integer.parseInt(announce.substring(id_index));
        }
        URL website = new URL("https://oppaiti.me/torrents.php?action=download&id=" + id + "&authkey=" + input.get("authKey") + "&torrent_pass=" + input.get("torrentPass"));
        return website.openConnection();
    }
}
