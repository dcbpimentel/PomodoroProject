import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;

import java.io.OutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;

public class PomodoroTimer {
    private static int timeLeft = 25 * 60; // Default timer (25 minutes)
    private static List<String> tasks = new ArrayList<>();

    public static void main(String[] args) throws IOException {
        // Create an HTTP server
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);

        // Set up routes
        server.createContext("/", new HomeHandler());
        server.createContext("/start-timer", new StartTimerHandler());
        server.createContext("/add-task", new AddTaskHandler());

        // Start the server
        server.setExecutor(null);
        System.out.println("Server started at http://localhost:8080");
        server.start();
    }

    // Handler for the Home Page
    static class HomeHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String response = """
                <!DOCTYPE html>
                <html lang="en">
                <head>
                    <meta charset="UTF-8">
                    <meta name="viewport" content="width=device-width, initial-scale=1.0">
                    <title>Pomodoro Timer</title>
                    <link rel="stylesheet" href="/styles.css">
                </head>
                <body>
                    <div class="container">
                        <h1>Pomodoro Timer</h1>
                        <p id="timer">25:00</p>
                        <form action="/start-timer" method="post">
                            <button type="submit">Start Timer</button>
                        </form>
                        <form action="/add-task" method="post">
                            <input type="text" name="task" placeholder="Enter task">
                            <button type="submit">Add Task</button>
                        </form>
                        <h2>Tasks</h2>
                        <ul>
            """;

            for (String task : tasks) {
                response += "<li>" + task + "</li>";
            }

            response += """
                        </ul>
                    </div>
                </body>
                </html>
            """;

            exchange.sendResponseHeaders(200, response.getBytes().length);
            OutputStream os = exchange.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }
    }

    // Handler to Start the Timer
    static class StartTimerHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("POST".equals(exchange.getRequestMethod())) {
                timeLeft = 25 * 60;
                System.out.println("Timer started for 25 minutes.");
            }
            exchange.getResponseHeaders().set("Location", "/");
            exchange.sendResponseHeaders(302, -1); // Redirect to home page
        }
    }

    // Handler to Add Tasks
    static class AddTaskHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("POST".equals(exchange.getRequestMethod())) {
                String query = new String(exchange.getRequestBody().readAllBytes());
                if (query.startsWith("task=")) {
                    String task = query.substring(5).replace("+", " ");
                    tasks.add(task);
                    System.out.println("Task added: " + task);
                }
            }
            exchange.getResponseHeaders().set("Location", "/");
            exchange.sendResponseHeaders(302, -1); // Redirect to home page
        }
    }
}
