package View;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import views.Markdown.MarkdownIt;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.Condition;

// To run this code type `jshell -R-ea --enable-preview`

enum SSEType { WRITE, CALL, SCRIPT, LOAD, CLEAR, RELEASE; }

public interface Clerk {
    static String generateID(int n) { // random alphanumeric string of size n
        return new Random().ints(n, 0, 36).
                            mapToObj(i -> Integer.toString(i, 36)).
                            collect(Collectors.joining());
    }

    static String getHashID(Object o) { return Integer.toHexString(o.hashCode()); }

    static LiveView view(int port) { return LiveView.onPort(port); }
    static LiveView view() { return view(LiveView.getDefaultPort()); }

    static void write(LiveView view, String html)        { view.sendServerEvent(SSEType.WRITE, html); }
    static void call(LiveView view, String javascript)   { view.sendServerEvent(SSEType.CALL, javascript); }
    static void script(LiveView view, String javascript) { view.sendServerEvent(SSEType.SCRIPT, javascript); }
    static void load(LiveView view, String path) {
        if (!view.paths.contains(path.trim())) view.sendServerEvent(SSEType.LOAD, path);
    }
    static void load(LiveView view, String onlinePath, String offlinePath) {
        load(view, onlinePath + ", " + offlinePath);
    }
    static void clear(LiveView view) { view.sendServerEvent(SSEType.CLEAR, ""); }
    static void clear() { clear(view()); };

    static void markdown(String text) { new MarkdownIt(view()).write(text); }
}

///open skills/Text/Text.java
///open skills/ObjectInspector/ObjectInspector.java
///open views/Turtle/Turtle.java
///open views/Markdown/Marked.java
///open views/Markdown/MarkdownIt.java
///open views/TicTacToe/TicTacToe.java
///open views/Dot/Dot.java
///open views/Input/Slider.java
//
// LiveView view = Clerk.view();