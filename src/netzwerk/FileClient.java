package netzwerk;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import org.json.JSONObject;

class FileClient {

	// Per Kommandozeile übergebene Parameter
	static String SERVER;
	static int SOCKET_PORT;
	static int GAME_ID;

	final static String FILE_TO_RECEIVE = "C:/Users/serge/Downloads/source-downloaded.json"; // change
																								// this
	// JSON-File
	// die
	// wir
	// uebergeben
	// bekommen
	final static int FILE_SIZE = 1400000; // TCP max 1500kB Nutzdaten, daher
											// sicherheitshalber auf 1400kB
											// beschraenken
	final static String TEAM_NAME = "xXx Players xXx";
	final static String PASSWORD = "123";

	public static void main(String[] args) throws IOException {

		// Kommandozeilenparameter parsen
		// Options options = new Options();
		// Option[] os = { new Option("r", "remote", true, "IPv4 des
		// Spielservers"),
		// new Option("p", "localPort", true, "Port bei uns"),
		// new Option("i", "spielID", true, "ID des Spiels dem beigetreten
		// werden soll")
		// };
		// for (Option o : os) {
		// o.setRequired(true);
		// options.addOption(o);
		// }
		//
		// CommandLineParser parser = new DefaultParser();
		// HelpFormatter formatter = new HelpFormatter();
		// CommandLine cmd;
		// try {
		// cmd = parser.parse(options, args);
		// } catch (ParseException e) {
		// System.out.println(e.getMessage());
		// formatter.printHelp("utility-name", options);
		//
		// System.exit(1);
		// return;
		// }
		// SERVER = cmd.getOptionValue("remote");
		// SOCKET_PORT =Integer.parseInt(cmd.getOptionValue("localPort").split("
		// ")[0]);
		// GAME_ID = Integer.parseInt(cmd.getOptionValue("spielID"));

		SERVER = "127.0.0.1";
		SOCKET_PORT = 9911;
		GAME_ID = 12;

		try {
			@SuppressWarnings("resource")
			Socket socket = new Socket(SERVER, SOCKET_PORT);
			PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
			BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

			System.out.println("Connecting...");

			JSONObject login = login();
			String login2 = login.toString();
			login2 = login2 + "\n\n";
			System.out.println(login2);
			out.println(login2);
			System.out.println("echo: " + in.readLine());
			System.out.println("Connecting...");

		} finally {

		}

	}

	private static JSONObject login() {
		JSONObject json = new JSONObject();
		JSONObject loginJSON = new JSONObject();
		loginJSON.put("team", TEAM_NAME);
		loginJSON.put("pw", PASSWORD);
		json.put("login", loginJSON);
		json.put("spielbeitritt", GAME_ID);
		json.put("spielerID", 0);
		return json;
	}
}