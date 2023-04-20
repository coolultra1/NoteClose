@import de.emilius123.noteclose.osm.note.ScheduledNoteStatus
@import java.time.format.DateTimeFormatter
@param de.emilius123.noteclose.osm.OAuthUser user
@param java.util.List<de.emilius123.noteclose.osm.note.ScheduledNote> userNotes
@param String statusMessage

<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <meta http-equiv="X-UA-Compatible" content="ie=edge">
    <meta name="description" content="Automatically close OpenStreetMap notes after a certain period of inactivity.">
    <title>NoteClose</title>
    <link rel="stylesheet" href="assets/noteclose.css">
</head>
<body>
    @if(user != null)
        <%-- Only display when logged in --%>
        <p id="login_info">You are logged in as <b>${user.username()}</b>. <a href="/logout">Logout</a></p>
    @endif

    <div id="entry_box">
        <form action="/note/schedule" method="post">
            <label for="note">Note ID:</label><br>
            <input type="number" required placeholder="Note ID" id="note" name="note"><br>

            <label for="close_date">Closure date:</label><br>
            <input type="datetime-local" required id="close_date"
                   name="close_date" value="2018-06-12T19:30"
                   min="2024-04-01T00:00">

            <label for="message">Message on closure:</label><br>
            <input type="text" title="Message on closure" placeholder="Message on closure" id="message" name="message"><br>

            @if(user != null)
            <%-- Only display submit button when logged in --%>
                <input type="submit" value="Schedule note">
            @else
                <a href="/auth/login" class="button">Login with OpenStreetMap</a>
            @endif
        </form>
    </div>

    @if(userNotes != null)
        <div id="user_notes">
            @if(userNotes.size() == 0)
                <p class="negative">You haven't scheduled any notes yet.</p>
            @else
                <table>
                    <tr>
                        <th>Note</th>
                        <th>Scheduled Closure</th>
                        <th>Closure message</th>
                        <th>Status</th>
                    </tr>
                @for(de.emilius123.noteclose.osm.note.ScheduledNote currentNote : userNotes)
                    <tr>
                        <td>${currentNote.note()}</td>
                        <td>${currentNote.close_date().toLocalDateTime().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))}</td>
                        @if(currentNote.message() == null)
                            <td><p class="negative"><i>None</i></p></td>
                        @else
                            <td><p>${currentNote.message()}</p></td>
                        @endif
                        <td>${currentNote.status().toString()}</td>
                        @if(currentNote.status() == ScheduledNoteStatus.SCHEDULED)
                            <td>
                                <form action="/note/cancel" method="post">
                                    <input type="hidden" name="note" value="${currentNote.note()}">
                                    <input type="submit" name="cancel" value="Cancel schedule"/>
                                </form>
                            </td>
                            <td>
                                <form action="/note/close" method="post">
                                    <input type="hidden" name="note" value="${currentNote.note()}">
                                    <input type="submit" name="cancel" value="Close now"/>
                                </form>
                            </td>
                        @endif

                        @if(currentNote.status() == ScheduledNoteStatus.FAILED)
                            <td>
                                <form action="/note/close" method="post">
                                    <input type="hidden" name="note" value="${currentNote.note()}">
                                    <input type="submit" name="cancel" value="Close now"/>
                                </form>
                            </td>
                        @endif
                    </tr>
                @endfor
                </table>
            @endif
        </div>
    @endif

    <%-- If available, alert status message --%>
    @if(statusMessage != null)
        <script>setTimeout(()=>alert("${statusMessage}"),1);</script>
    @endif

    <script src="assets/noteclose.js"></script>
</body>