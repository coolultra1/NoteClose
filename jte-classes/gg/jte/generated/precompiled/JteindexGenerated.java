package gg.jte.generated.precompiled;
public final class JteindexGenerated {
	public static final String JTE_NAME = "index.jte";
	public static final int[] JTE_LINE_INFO = {0,0,0,0,10,10,10,10,11};
	public static void render(gg.jte.html.HtmlTemplateOutput jteOutput, gg.jte.html.HtmlInterceptor jteHtmlInterceptor, String username) {
		jteOutput.writeContent("<head>\r\n    <meta charset=\"UTF-8\">\r\n    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\r\n    <meta http-equiv=\"X-UA-Compatible\" content=\"ie=edge\">\r\n    <meta name=\"description\" content=\"Automatically close OpenStreetMap notes after a certain period of inactivity.\">\r\n    <title>NoteClose</title>\r\n    <link rel=\"stylesheet\" href=\"assets/noteclose.css\">\r\n</head>\r\n<body>\r\n        <p id=\"login_info\">You are logged in as ");
		jteOutput.setContext("p", null);
		jteOutput.writeUserContent(username);
		jteOutput.writeContent(". <a href=\"/logout\">Logout</a></p>\r\n</body>");
	}
	public static void renderMap(gg.jte.html.HtmlTemplateOutput jteOutput, gg.jte.html.HtmlInterceptor jteHtmlInterceptor, java.util.Map<String, Object> params) {
		String username = (String)params.get("username");
		render(jteOutput, jteHtmlInterceptor, username);
	}
}
