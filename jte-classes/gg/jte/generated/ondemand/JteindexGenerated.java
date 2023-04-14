package gg.jte.generated.ondemand;
import de.emilius123.noteclose.osm.note.ScheduledNoteStatus;
import java.time.format.DateTimeFormatter;
public final class JteindexGenerated {
	public static final String JTE_NAME = "index.jte";
	public static final int[] JTE_LINE_INFO = {0,0,1,2,2,2,14,14,14,15,16,16,16,17,17,32,32,33,35,35,37,37,41,41,43,43,45,45,53,53,55,55,55,56,56,56,57,57,59,59,60,60,60,61,61,62,62,62,63,63,66,66,66,66,66,66,66,66,72,72,72,72,72,72,72,72,76,76,78,78,80,80,82,82,85};
	public static void render(gg.jte.html.HtmlTemplateOutput jteOutput, gg.jte.html.HtmlInterceptor jteHtmlInterceptor, de.emilius123.noteclose.osm.OAuthUser user, java.util.List<de.emilius123.noteclose.osm.note.ScheduledNote> userNotes) {
		jteOutput.writeContent("\r\n<head>\r\n    <meta charset=\"UTF-8\">\r\n    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\r\n    <meta http-equiv=\"X-UA-Compatible\" content=\"ie=edge\">\r\n    <meta name=\"description\" content=\"Automatically close OpenStreetMap notes after a certain period of inactivity.\">\r\n    <title>NoteClose</title>\r\n    <link rel=\"stylesheet\" href=\"assets/noteclose.css\">\r\n</head>\r\n<body>\r\n    ");
		if (user != null) {
			jteOutput.writeContent("\r\n        ");
			jteOutput.writeContent("\r\n        <p id=\"login_info\">You are logged in as <b>");
			jteOutput.setContext("b", null);
			jteOutput.writeUserContent(user.username());
			jteOutput.writeContent("</b>. <a href=\"/logout\">Logout</a></p>\r\n    ");
		}
		jteOutput.writeContent("\r\n\r\n    <div id=\"entry_box\">\r\n        <form action=\"/note/schedule\" method=\"post\">\r\n            <label for=\"note\">Note ID:</label><br>\r\n            <input type=\"number\" required placeholder=\"Note ID\" id=\"note\" name=\"note\"><br>\r\n\r\n            <label for=\"close_date\">Closure date:</label><br>\r\n            <input type=\"datetime-local\" required id=\"close_date\"\r\n                   name=\"close_date\" value=\"2018-06-12T19:30\"\r\n                   min=\"2024-04-01T00:00\">\r\n\r\n            <label for=\"message\">Message on closure:</label><br>\r\n            <input type=\"text\" title=\"Message on closure\" placeholder=\"Message on closure\" id=\"message\" name=\"message\">\r\n\r\n            ");
		if (user != null) {
			jteOutput.writeContent("\r\n            ");
			jteOutput.writeContent("\r\n                <input type=\"submit\" value=\"Schedule note\">\r\n            ");
		} else {
			jteOutput.writeContent("\r\n                <a href=\"/auth/login\" class=\"button\">Login with OpenStreetMap</a>\r\n            ");
		}
		jteOutput.writeContent("\r\n        </form>\r\n    </div>\r\n\r\n    ");
		if (userNotes != null) {
			jteOutput.writeContent("\r\n        <div id=\"user_notes\">\r\n            ");
			if (userNotes.size() == 0) {
				jteOutput.writeContent("\r\n                <p class=\"negative\">You haven't scheduled any notes yet.</p>\r\n            ");
			} else {
				jteOutput.writeContent("\r\n                <table>\r\n                    <tr>\r\n                        <th>Note</th>\r\n                        <th>Scheduled Closure</th>\r\n                        <th>Closure message</th>\r\n                        <th>Status</th>\r\n                    </tr>\r\n                ");
				for (de.emilius123.noteclose.osm.note.ScheduledNote currentNote : userNotes) {
					jteOutput.writeContent("\r\n                    <tr>\r\n                        <td>");
					jteOutput.setContext("td", null);
					jteOutput.writeUserContent(currentNote.note());
					jteOutput.writeContent("</td>\r\n                        <td>");
					jteOutput.setContext("td", null);
					jteOutput.writeUserContent(currentNote.close_date().toLocalDateTime().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
					jteOutput.writeContent("</td>\r\n                        ");
					if (currentNote.message() == null) {
						jteOutput.writeContent("\r\n                            <td><p class=\"negative\"><i>None</i></p></td>\r\n                        ");
					} else {
						jteOutput.writeContent("\r\n                            <td><p>");
						jteOutput.setContext("p", null);
						jteOutput.writeUserContent(currentNote.message());
						jteOutput.writeContent("</p></td>\r\n                        ");
					}
					jteOutput.writeContent("\r\n                        <td>");
					jteOutput.setContext("td", null);
					jteOutput.writeUserContent(currentNote.status().toString());
					jteOutput.writeContent("</td>\r\n                        ");
					if (currentNote.status() == ScheduledNoteStatus.SCHEDULED) {
						jteOutput.writeContent("\r\n                            <td>\r\n                                <form action=\"/note/cancel\" method=\"post\">\r\n                                    <input type=\"hidden\" name=\"note\"");
						if (gg.jte.runtime.TemplateUtils.isAttributeRendered(currentNote.note())) {
							jteOutput.writeContent(" value=\"");
							jteOutput.setContext("input", "value");
							jteOutput.writeUserContent(currentNote.note());
								jteOutput.setContext("input", null);
							jteOutput.writeContent("\"");
						}
						jteOutput.writeContent(">\r\n                                    <input type=\"submit\" name=\"cancel\" value=\"Cancel schedule\"/>\r\n                                </form>\r\n                            </td>\r\n                            <td>\r\n                                <form action=\"/note/close\" method=\"post\">\r\n                                    <input type=\"hidden\" name=\"note\"");
						if (gg.jte.runtime.TemplateUtils.isAttributeRendered(currentNote.note())) {
							jteOutput.writeContent(" value=\"");
							jteOutput.setContext("input", "value");
							jteOutput.writeUserContent(currentNote.note());
								jteOutput.setContext("input", null);
							jteOutput.writeContent("\"");
						}
						jteOutput.writeContent(">\r\n                                    <input type=\"submit\" name=\"cancel\" value=\"Close now\"/>\r\n                                </form>\r\n                            </td>\r\n                        ");
					}
					jteOutput.writeContent("\r\n                    </tr>\r\n                ");
				}
				jteOutput.writeContent("\r\n                </table>\r\n            ");
			}
			jteOutput.writeContent("\r\n        </div>\r\n    ");
		}
		jteOutput.writeContent("\r\n\r\n    <script src=\"assets/noteclose.js\"></script>\r\n</body>");
	}
	public static void renderMap(gg.jte.html.HtmlTemplateOutput jteOutput, gg.jte.html.HtmlInterceptor jteHtmlInterceptor, java.util.Map<String, Object> params) {
		de.emilius123.noteclose.osm.OAuthUser user = (de.emilius123.noteclose.osm.OAuthUser)params.get("user");
		java.util.List<de.emilius123.noteclose.osm.note.ScheduledNote> userNotes = (java.util.List<de.emilius123.noteclose.osm.note.ScheduledNote>)params.get("userNotes");
		render(jteOutput, jteHtmlInterceptor, user, userNotes);
	}
}
