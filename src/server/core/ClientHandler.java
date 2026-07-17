package server.core;

import common.JsonParser;
import common.JsonWriter;
import common.Request;
import common.Result;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

//یعنی از این به بعد ClientHandler نقش Bridge بین شبکه و منطق برنامه را دارد:
//از سوکت متن JSON را می‌خواند.
//با JsonParser آن را به Request تبدیل می‌کند.
//Request را به CommandDispatcher می‌دهد.
//Result را از Dispatcher می‌گیرد.
//آن را به JSON تبدیل می‌کند و روی سوکت می‌فرستد.
//این تفکیک وظایف باعث می‌شود اگر فردا به جای TCP از WebSocket یا HTTP هم استفاده کنی، فقط لایه‌ی ارتباطی عوض شود و هیچ تغییری در CommandDispatcher یا AuthService لازم نباشد. این دقیقاً یکی از اصول مهم طراحی لایه‌ای است.
public class ClientHandler implements Runnable {

    private final Socket socket;
    private final CommandDispatcher dispatcher;

    private BufferedReader reader;
    private PrintWriter writer;

    public ClientHandler(Socket socket, CommandDispatcher dispatcher) throws IOException {

        this.socket = socket;
        this.dispatcher = dispatcher;

        reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

        writer = new PrintWriter(socket.getOutputStream(), true);

    }

    @Override
    public void run() {
        try {
            String line;

            while ((line = reader.readLine()) != null) {

                System.out.println("Client says: " + line);

                Result<?> result;

                try {
                    Request request = JsonParser.parse(line);

                    result = dispatcher.dispatch(request);
                }
                catch (RuntimeException e) {

                    result = Result.error("Error: " + e.getMessage());
                }

                String response = JsonWriter.writeResult(result);

                writer.println(response);
            }
        }
        catch (IOException e)  {
            System.out.println("Client disconnected: " + e.getMessage());
        }
        finally {
            try {
                socket.close();
            } catch (IOException ioException) {
            }
        }
    }

    //JSON String
    //   ↓
    //JsonParser.parse
    //   ↓
    //Request
    //   ↓
    //dispatcher.dispatch
    //   ↓
    //Result
    //   ↓
    //JsonWriter.writeResult
    //   ↓
    //JSON String
}
