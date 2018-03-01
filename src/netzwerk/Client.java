package netzwerk;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.json.JSONObject;

import entscheidungen.Bewerter;
import entscheidungen.Bieter;
import entscheidungen.Entscheider;
import entscheidungen.EntscheiderMDFFMN;
import spiellogik.Karte;
import spiellogik.Spielzustand;

class Client {

	// Per Kommandozeile übergebene Parameter
	static String host;
	static int port;
	static long spielID;
	static int spielerID;

	final static String TEAM = "xXx Players xXx";
	final static String PASSWORT = "123";

	public static void main(final String[] args) throws IOException {

		if (args.length == 3) {
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
			spielID = Long.parseLong(cmd.getOptionValue("spielID"));
		} else {
			// Testeinstellungen
			host = "localhost";
			port = 9911;
			
			System.out.print("Spiel-ID eingeben: ");
			BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
			spielID = Long.parseLong(reader.readLine());
		}

		try {
			// Sockets erstellen
			@SuppressWarnings("resource")
			final Socket socket = new Socket(host, port);
			final PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
			final BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

			// Einloggen
			System.out.println("Einloggen ...");
			out.println(login().toString() + "\n\n");
			final JSONObject spiel = naechsteNachricht(in);
			spielID = spiel.getLong("spielID");
			spielerID = spiel.getInt("spielerID");
			System.out.println("Eingeloggt in Spiel " + spielID + " als Spieler " + spielerID);

			// Auf alle Spieler warten, dann ersten Spielzustand erhalten
			final Entscheider entscheider = new EntscheiderMDFFMN(new Bewerter());
			final JSONObject ersteRunde = naechsteNachricht(in);
			Spielzustand zustand = Parser.ersteRunde(ersteRunde, spielerID);
			List<Karte> bietoptionen = Parser.bietoptionen(ersteRunde.getJSONArray("bietoptionen"));

			while (true) {

				// -> Gebote schicken
				final JSONObject gebote = Serialisierer.gebote(Bieter.gebote(zustand, bietoptionen));
				out.println(datenVerpacken(gebote));

				// <- Auktionsergebnis
				zustand = Parser.auktionsergebnisAuswerten(naechsteNachricht(in).getJSONArray("auktionsergebnis"),
						zustand, spielerID);

				// -> Programm
				zustand.handkartenSortieren();
				final JSONObject programm = Serialisierer.programm(entscheider.entscheiden(zustand));
				out.println(datenVerpacken(programm));

				// <- Gespielte Runde (Programme, ..., Sieger)
				zustand = Parser.nteRunde(naechsteNachricht(in), spielerID, zustand);

				// -> Powerdown? (nein, obv)
				out.println(datenVerpacken(Serialisierer.powerdown(entscheider.powerdown(zustand))));

				// <- Powerdowns, Handkarten, Bietbares
				final JSONObject naechsteRunde = naechsteNachricht(in);
				zustand = Parser.powerdowns(naechsteRunde.getJSONArray("powerDowns"), spielerID, zustand);
				zustand = Parser.handkarten(naechsteRunde.getJSONArray("handkarten"), spielerID, zustand);
				bietoptionen = Parser.bietoptionen(naechsteRunde.getJSONArray("bietoptionen"));

			}

		} finally {
		}

	}

	static JSONObject naechsteNachricht(final BufferedReader in) throws IOException {
		String nachricht;
		do {
			nachricht = in.readLine();
		} while (nachricht.isEmpty());
		System.out.println("<  " + (new JSONObject(nachricht)).toString());
		return new JSONObject(nachricht);
	}

	private static JSONObject login() {
		final JSONObject json = new JSONObject();
		final JSONObject loginJSON = new JSONObject();
		loginJSON.put("team", TEAM);
		loginJSON.put("pw", PASSWORT);
		json.put("login", loginJSON);
		json.put("spielbeitritt", spielID);
		json.put("spielerID", 0);
		return json;
	}

	private static String datenVerpacken(final JSONObject daten) {

		final JSONObject result = new JSONObject();
		result.put("spielID", spielID);
		result.put("spielerID", spielerID);
		result.put("daten", daten);
		result.put("message", "");

		System.out.println(" > " + result.toString());

		return result.toString() + "\n\n";

	}

}