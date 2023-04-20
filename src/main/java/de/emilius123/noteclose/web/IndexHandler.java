package de.emilius123.noteclose.web;

import de.emilius123.noteclose.db.DBUtil;
import de.emilius123.noteclose.osm.OAuthUser;
import io.javalin.http.Handler;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Handles rendering of the basic pages
 */
public class IndexHandler {
    private DBUtil dbUtil;

    public IndexHandler(DBUtil dbUtil) {
        this.dbUtil = dbUtil;
    }

    public Handler handleIndex = ctx -> {
        Map<String, Object> indexModel = new HashMap<>();

        // User info
        OAuthUser user = ctx.sessionAttribute("user");
        indexModel.put("user", user);

        // User notes
        if(user != null) {
            // Only query user notes if user is logged in;
            indexModel.put("userNotes", dbUtil.getUserNotes(user.id(), 111));
        }

        ctx.render("index.jte", indexModel);
    };
}
