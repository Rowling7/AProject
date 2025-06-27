package com.example.simplebrowser.scrpit;

import org.json.JSONException;
import org.json.JSONObject;

public class UserScript {
    private String name;
    private String code;
    private String matchPattern;
    private boolean enabled;

    public UserScript(String name, String code, String matchPattern, boolean enabled) {
        this.name = name;
        this.code = code;
        this.matchPattern = matchPattern;
        this.enabled = enabled;
    }

    // Getters
    public String getName() { return name; }
    public String getCode() { return code; }
    public String getMatchPattern() { return matchPattern; }
    public boolean isEnabled() { return enabled; }

    // Setters
    public void setEnabled(boolean enabled) { this.enabled = enabled; }

    // 从JSON对象创建UserScript
    public static UserScript fromJson(JSONObject json) throws JSONException {
        return new UserScript(
            json.getString("name"),
            json.getString("code"),
            json.getString("matchPattern"),
            json.getBoolean("enabled")
        );
    }

    // 转换为JSON对象
    public JSONObject toJson() throws JSONException {
        JSONObject json = new JSONObject();
        json.put("name", name);
        json.put("code", code);
        json.put("matchPattern", matchPattern);
        json.put("enabled", enabled);
        return json;
    }

    // URL匹配逻辑
    public boolean matchesUrl(String url) {
        if (url == null) return false;
        if (matchPattern.equals("*")) return true;
        if (matchPattern.startsWith("*.")) {
            return url.contains(matchPattern.substring(2)) ||
                   url.equals("https://" + matchPattern.substring(2)) ||
                   url.equals("http://" + matchPattern.substring(2));
        }
        return url.contains(matchPattern);
    }
    // 在UserScript.java中添加
public String getExecutableScript() {
    return "(function() { try {" + code + "} catch(e) { console.error('Script error:', e); } })();";
}

}
