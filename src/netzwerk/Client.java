package netzwerk;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Arrays;

import org.json.JSONObject;

import entscheidungen.Bewerter;
import entscheidungen.Entscheider;

class Client {

	// Per Kommandozeile übergebene Parameter
	static String SERVER;
	static int SOCKET_PORT;
	static int GAME_ID;
	static int SPIELER_ID;

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

			out.println(login().toString() + "\n\n");

			String answer = in.readLine();
			System.out.println(answer);
			answer = answer.replaceAll("[^0-9]+", " ");
			String[] IDs = answer.trim().split(" ");
			System.out.println(Arrays.asList(answer.trim().split(" ")));
			System.out.println("echo: " + answer);
			
			GAME_ID = Integer.parseInt(IDs[0]);
			SPIELER_ID = Integer.parseInt(IDs[1]);
			
			Entscheider entscheider = new Entscheider(new Bewerter());
			
			
			// -> Login
			// <- Login ok
			// <- Spielzustand
			
			for (int phase = 0; phase <= 3; phase = (phase + 1) % 3) {
				
				// -> Gebote schicken
				// <- wer wie geboten hat
				
				// -> Programm
				
				// <- Programme, Brett, Roboter [, Sieger]
				
				// -> Powerdown? (nein, obv)
				// <- Powerdowns, Handkarten, Bietbares
				
			}
			

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

	private static JSONObject newJSONPackage(JSONObject daten) {

		JSONObject result = new JSONObject();
		result.put("spielID", GAME_ID);
		result.put("spielerID", SPIELER_ID);
		result.put("daten", daten);
		result.put("message", "");

		return result;

	}
}