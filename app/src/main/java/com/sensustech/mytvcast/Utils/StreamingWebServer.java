package com.sensustech.mytvcast.utils;

import android.net.Uri;
import android.util.Log;
import android.webkit.MimeTypeMap;

import org.nanohttpd.protocols.http.IHTTPSession;
import org.nanohttpd.protocols.http.NanoHTTPD;
import org.nanohttpd.protocols.http.response.Response;
import org.nanohttpd.protocols.http.response.Status;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import static org.nanohttpd.protocols.http.response.Response.newChunkedResponse;
import static org.nanohttpd.protocols.http.response.Response.newFixedLengthResponse;


public class StreamingWebServer extends NanoHTTPD {

    public static final int PORT = 8080;
    private HashMap<String,String> filePaths = new HashMap<>();

    public StreamingWebServer() throws IOException {
        super(PORT);
    }

    public void addFileForPath(String file, String path) {
        filePaths.put(file,path);
    }

    @Override
    public Response handle(IHTTPSession session) {
        String uri = session.getUri();
        for(Map.Entry<String, String> entry : filePaths.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            if (uri.equals("/" + key)){
                FileInputStream fis = null;
                try {
                    File f = new File(value);
                    fis = new FileInputStream(f);
                    String mime = getMimeType(f.getAbsolutePath());
                    if(mime.contains("video") || mime.contains("audio"))
                         return serveFile(uri,session.getHeaders(),f);
                    else
                        return newChunkedResponse(Status.OK, mime, fis);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }
        return  null;
    }

    private Response serveFile(String uri, Map<String, String> header, File file)
    {
        for (Map.Entry<String, String> entry : header.entrySet())
        {
            String key = entry.getKey().toString();
            String value = entry.getValue();
        }
        Response res;
        String mime = getMimeTypeForFile(uri);
        try {
            String etag = Integer.toHexString((file.getAbsolutePath() +
                    file.lastModified() + "" + file.length()).hashCode());
            long startFrom = 0;
            long endAt = -1;
            String range = header.get("range");
            if (range != null) {
                if (range.startsWith("bytes=")) {
                    range = range.substring("bytes=".length());
                    int minus = range.indexOf('-');
                    try {
                        if (minus > 0) {
                            startFrom = Long.parseLong(range.substring(0, minus));
                            endAt = Long.parseLong(range.substring(minus + 1));
                        }
                    } catch (NumberFormatException ignored) {
                    }
                }
            }
            long fileLen = file.length();
            if (range != null && startFrom >= 0) {
                if (startFrom >= fileLen) {
                    res = createResponse(Status.RANGE_NOT_SATISFIABLE, MIME_PLAINTEXT, "");
                    res.addHeader("Content-Range", "bytes 0-0/" + fileLen);
                    res.addHeader("ETag", etag);
                } else {
                    if (endAt < 0) {
                        endAt = fileLen - 1;
                    }
                    //endAt=startFrom+1000000;
                    long newLen = endAt - startFrom + 1;
                    if (newLen < 0) {
                        newLen = 0;
                    }

                    final long dataLen = newLen;
                    FileInputStream fis = new FileInputStream(file) {
                        @Override
                        public int available() throws IOException {
                            return (int) dataLen;
                        }
                    };
                    fis.skip(startFrom);
                    res = createResponse(Status.PARTIAL_CONTENT, mime, fis,dataLen);
                    res.addHeader("Content-Length", "" + dataLen);
                    res.addHeader("Content-Range", "bytes " + startFrom + "-" +endAt + "/" + fileLen);
                    res.addHeader("ETag", etag);
                    Log.d("Server", "serveFile --1--: Start:"+startFrom+" End:"+endAt);
                }
            } else {
                if (etag.equals(header.get("if-none-match"))) {
                    res = createResponse(Status.NOT_MODIFIED, mime, "");
                }
                else
                {
                    FileInputStream fis=new FileInputStream(file);
                    res = createResponse(Status.OK, mime, fis,fis.available());
                    res.addHeader("Content-Length", "" + fileLen);
                    res.addHeader("ETag", etag);
                }
            }
        } catch (IOException ioe) {
            res = getResponse("Forbidden: Reading file failed");
        }
        return (res == null) ? getResponse("Error 404: File not found") : res;
    }

    private Response createResponse(Status status, String mimeType, InputStream message,long totalBytes) {
        Response res = newFixedLengthResponse(status, mimeType, message,totalBytes);
        res.addHeader("Accept-Ranges", "bytes");
        return res;
    }

    private Response createResponse(Status status, String mimeType, String message) {
        Response res = newFixedLengthResponse(status, mimeType, message);
        res.addHeader("Accept-Ranges", "bytes");
        return res;
    }

    private Response getResponse(String message) {
        return createResponse(Status.OK, "text/plain", message);
    }

    public static String getMimeType(String url) {
        String type = null;
        String extension = MimeTypeMap.getFileExtensionFromUrl(sanitizeFileName(Uri.encode(url)));
        if (extension != null) {
            type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
        }
        return type;
    }

    public static String sanitizeFileName(String name)
    {
        byte[] invalidChars = new byte[]{34, 60, 62, 124, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 58, 42, 63, 92, 47};
        for(byte i : invalidChars)
        {
            name = name.replace((char)i,'_');
        }
        return name;
    }
}
