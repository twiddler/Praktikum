package netzwerk;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Arrays;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.json.JSONObject;

import entscheidungen.Bewerter;
import entscheidungen.Entscheider;
import spiellogik.Spielzustand;

class Client {

	// Per Kommandozeile übergebene Parameter
	static String host;
	static int port;
	static int spielID;
	static int spielerID;

	final static String TEAM = "xXx Players xXx";
	final static String PASSWORT = "123";

	public static void main(String[] args) throws IOException {

		if (false) {
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
			host = cmd.getOptionValue("remote");
			port = Integer.parseInt(cmd.getOptionValue("localPort").split(" ")[0]);
			spielID = Integer.parseInt(cmd.getOptionValue("spielID"));
		}

		// Testeinstellungen
		host = "127.0.0.1";
		port = 9911;
		spielID = 12;

		try {
			// Sockets erstellen
			@SuppressWarnings("resource")
			Socket socket = new Socket(host, port);
			PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
			BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

			// Einloggen
			System.out.println("Einloggen ...");
			out.println(login().toString() + "\n\n");
			JSONObject spiel = new JSONObject(in.readLine());
			spielID = spiel.getInt("spielID");
			spielerID = spiel.getInt("spielerID");
			System.out.println("Eingeloggt in Spiel " + spielID + " als Spieler " + spielerID);

			// Auf alle Spieler warten, dann ersten Spielzustand erhalten
			Entscheider entscheider = new Entscheider(new Bewerter());
			Spielzustand zustand = Parser.ersteRunde(naechsteNachricht(in), spielerID);

			for (int phase = 0; phase < 3; ++phase) {
				
								
				// -> Gebote schicken
				// <- wer wie geboten hat

				// -> Programm
				JSONObject programm = Serialisierer.programm(entscheider.entscheiden(zustand));
				out.println(datenVerpacken(programm));

				// <- programme, spielbrett, spieler, roboter, sieger
				zustand = Parser.nteRunde(naechsteNachricht(in), spielerID);

				// -> Powerdown? (nein, obv)
				// <- Powerdowns, Handkarten, Bietbares

			}

		} finally {
		}

	}

	static JSONObject naechsteNachricht(BufferedReader in) throws IOException {
		String nachricht;
		do {
			nachricht = in.readLine();
		} while (nachricht.isEmpty());
		return new JSONObject(nachricht);
	}
	
	private static JSONObject login() {
		JSONObject json = new JSONObject();
		JSONObject loginJSON = new JSONObject();
		loginJSON.put("team", TEAM);
		loginJSON.put("pw", PASSWORT);
		json.put("login", loginJSON);
		json.put("spielbeitritt", spielID);
		json.put("spielerID", 0);
		return json;
	}

	private static String datenVerpacken(JSONObject daten) {

		JSONObject result = new JSONObject();
		result.put("spielID", spielID);
		result.put("spielerID", spielerID);
		result.put("daten", daten);
		result.put("message", "");

		return result.toString()+"\n\n";

	}
	
}