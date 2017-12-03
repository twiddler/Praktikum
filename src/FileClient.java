import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import org.json.JSONObject;
import org.apache.commons.cli.*;

class FileClient {

	// Per Kommandozeile ¸bergebene Parameter
	static String SERVER;
	static int SOCKET_PORT;
	static int GAME_ID;

	final static String FILE_TO_RECEIVE = "c:/temp/source-downloaded.json"; // die
																			// JSON-File
																			// die
																			// wir
																			// uebergeben
																			// bekommen
	final static int FILE_SIZE = 1400; // TCP max 1500kB Nutzdaten, daher
										// sicherheitshalber auf 1400kB
										// beschraenken
	final static String TEAM_NAME = "xXx Players xXx";
	final static String PASSWORD = "hallo1234";

	public static void main(String[] args) throws IOException {

		// Kommandozeilenparameter parsen
		Options options = new Options();
		Option[] os = { new Option("r", "remote", true, "IPv4 des Spielservers"),
				new Option("p", "localPort", true, "Port bei uns"),
				new Option("i", "spielID", true, "ID des Spiels dem beigetreten werden soll") };
		for (Option o : os) {
			o.setRequired(true);
			options.addOption(o);
		}

		CommandLineParser parser = new DefaultParser();
		HelpFormatter formatter = new HelpFormatter();
		CommandLine cmd;
		try {
			cmd = parser.parse(options, args);
		} catch (ParseException e) {
			System.out.println(e.getMessage());
			formatter.printHelp("utility-name", options);

			System.exit(1);
			return;
		}
		SERVER = cmd.getOptionValue("remote");
		SOCKET_PORT = Integer.parseInt(cmd.getOptionValue("localPort").split(" ")[0]);
		GAME_ID = Integer.parseInt(cmd.getOptionValue("spielID"));

		int bytesRead;
		int current = 0;
		FileOutputStream fos = null;
		BufferedOutputStream bos = null;
		Socket sock = null;
		try {
			sock = new Socket(SERVER, SOCKET_PORT);
			System.out.println("Connecting...");

			// receive file
			byte[] mybytearray = new byte[FILE_SIZE];
			InputStream is = sock.getInputStream();
			fos = new FileOutputStream(FILE_TO_RECEIVE);
			bos = new BufferedOutputStream(fos);
			bytesRead = is.read(mybytearray, 0, mybytearray.length);
			current = bytesRead;

			do {
				bytesRead = // gibt die gesamte Anzahl an Bytes zurueck, die wir
							// empfangen haben
				is.read(mybytearray, current, (mybytearray.length - current));
				if (bytesRead >= 0)
					current += bytesRead;
			} while (bytesRead > -1); // -1 wird zurueckgegeben, wenn wir am
										// Ende des Streams angekommen sind

			bos.write(mybytearray, 0, current);
			bos.flush();
			System.out.println("File " + FILE_TO_RECEIVE + " downloaded (" + current + " bytes read)");
		} finally { // Streams und Socket schlieﬂen, selbst wenn eine Exception
					// geworfen wurde
			if (fos != null)
				fos.close();
			if (bos != null)
				bos.close();
			if (sock != null)
				sock.close();
		}
	}

	private String loginString() {
		JSONObject json = new JSONObject();
		JSONObject loginJSON = new JSONObject();
		loginJSON.put("team", TEAM_NAME);
		loginJSON.put("pw", PASSWORD);
		json.put("login", loginJSON);
		json.put("spielbeitritt", GAME_ID);
		json.put("spielerID", 0);
		return json.toString();
	}
}