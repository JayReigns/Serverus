package com.jr.serverus;

public class MimeType
{
    public static final String MIME_TYPE_7Z = "application/7z";
    public static final String MIME_TYPE_APP = "application/vnd.android.package-archive";
    public static final String MIME_TYPE_AUDIO = "audio/*";
    public static final String MIME_TYPE_BMP = "image/bmp";
    public static final String MIME_TYPE_GIF = "image/gif";
    public static final String MIME_TYPE_HTML = "text/html";
    public static final String MIME_TYPE_JPG = "image/jpg";
    public static final String MIME_TYPE_OGG = "application/ogg";
    public static final String MIME_TYPE_PDF = "application/pdf";
    public static final String MIME_TYPE_PNG = "image/png";
    public static final String MIME_TYPE_RAR = "application/rar";
    public static final String MIME_TYPE_TEXT = "text/plain";
    public static final String MIME_TYPE_UNKNOWN = "*/*";
    public static final String MIME_TYPE_VIDEO = "video/*";
    public static final String MIME_TYPE_WEBP = "image/webp";
    public static final String MIME_TYPE_ZIP = "application/zip";
	public static final String MIME_TYPE_SVG = "image/svg+xml";

    public static String getMimeFromName(String str) {
        return getMimeFromExt(getExt(str));
    }

    public static String getExt(String str) {
		String ext = "";
        int lastIndexOf = str.lastIndexOf(".");
        if (lastIndexOf >= 0) {
            ext = str.substring(lastIndexOf + 1).toLowerCase();
        }
        return ext;
    }

    public static String getMimeFromExt(String str) {
        if (str.equals("jpg")) {
            return MIME_TYPE_JPG;
        }
        if (str.equals("png")) {
            return MIME_TYPE_PNG;
        }
        if (str.equals("bmp")) {
            return MIME_TYPE_BMP;
        }
        if (str.equals("webp")) {
            return MIME_TYPE_WEBP;
        }
		if (str.equals("svg")) {
            return MIME_TYPE_SVG;
        }
        if (str.equals("txt")) {
            return MIME_TYPE_TEXT;
        }
        if (str.equals("html")) {
            return MIME_TYPE_HTML;
        }
        if (str.equals("js")) {
            return MIME_TYPE_TEXT;
        }
        if (str.equals("java")) {
            return MIME_TYPE_TEXT;
        }
        if (str.equals("css")) {
            return MIME_TYPE_TEXT;
        }
        if (str.equals("xml")) {
            return MIME_TYPE_HTML;
        }
        if (str.equals("c")) {
            return MIME_TYPE_HTML;
        }
        if (str.equals("mht")) {
            return MIME_TYPE_HTML;
        }
        if (str.equals("mp3")) {
            return MIME_TYPE_AUDIO;
        }
        if (str.equals("aac")) {
            return MIME_TYPE_AUDIO;
        }
        if (str.equals("3gpp")) {
            return MIME_TYPE_AUDIO;
        }
        if (str.equals("mp4")) {
            return MIME_TYPE_VIDEO;
        }
        if (str.equals("3gp")) {
            return MIME_TYPE_VIDEO;
        }
        if (str.equals("wmv")) {
            return MIME_TYPE_VIDEO;
        }
        if (str.equals("gif")) {
            return MIME_TYPE_GIF;
        }
        if (str.equals("apk")) {
            return MIME_TYPE_APP;
        }
        if (str.equals("ogg")) {
            return MIME_TYPE_OGG;
        }
        if (str.equals("rar")) {
            return MIME_TYPE_RAR;
        }
        if (str.equals("zip")) {
            return MIME_TYPE_ZIP;
        }
        if (str.equals("7z")) {
            return MIME_TYPE_7Z;
        }
        if (str.equals("pdf")) {
            return MIME_TYPE_PDF;
        }
        if (str.equals(MIME_TYPE_UNKNOWN)) {
            return MIME_TYPE_UNKNOWN;
        }
        return "unknown";
    }
}
