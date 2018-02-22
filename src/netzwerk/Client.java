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
			JSONObject antwort = new JSONObject(in.readLine());
			spielID = antwort.getInt("spielID");
			spielerID = antwort.getInt("spielerID");
			System.out.println("Eingeloggt in Spiel " + spielID + " als Spieler " + spielerID);

			// Auf alle Spieler warten, dann ersten Spielzustand erhalten
			String ersteRunde;
			do {
				ersteRunde = in.readLine();
			} while (ersteRunde.isEmpty());

			Entscheider entscheider = new Entscheider(new Bewerter());
			Spielzustand zustand = Parser.ersteRunde(new JSONObject(ersteRunde), spielerID);

			while (true) {

				// -> Gebote schicken
				// <- wer wie geboten hat

				// -> Programm
				JSONObject programm = Serialisierer.programm(entscheider.entscheiden(zustand));
				out.println(datenVerpacken(programm));

				
				// <- gespielte Runde
				// <- programme, spielbrett, spieler, roboter, sieger
				String nteRunde;
				do {
					nteRunde = in.readLine();
				} while (nteRunde.isEmpty());
				zustand = Parser.nteRunde(new JSONObject(nteRunde), spielerID);

				// -> Powerdown? (nein, obv)
				// <- Powerdowns, Handkarten, Bietbares

			}

		} finally {
		}

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

	private static JSONObject datenVerpacken(JSONObject daten) {

		JSONObject result = new JSONObject();
		result.put("spielID", spielID);
		result.put("spielerID", spielerID);
		result.put("daten", daten);
		result.put("message", "");

		return result;

	}
}