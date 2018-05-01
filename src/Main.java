
public class Main {
    public static void main(String[] args) throws Exception { 
        /*System.setProperty("http.agent", "Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10.4; en-US; rv:1.9.2.2) Gecko/20100316 Firefox/3.6.2");
        RedBot redBot = new RedBot();
        redBot.setVerbose(true);
        redBot.start();*/
        DHBot dhBot = new DHBot();
        dhBot.setVerbose(true);
        dhBot.start();
    }
}
