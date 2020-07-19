package com.ut.commclient.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public abstract class ResUtil {
    public static void close(BufferedReader reader, PrintWriter writer, ServerSocket serverSocket, Socket socket) {
        if (serverSocket != null) {
            try {
                serverSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if (socket != null) {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if (reader != null) {
            try {
                reader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if (writer != null) {
            writer.close();
        }
    }

    public static void closeServerSocket(ServerSocket serverSocket) {
        close(null, null, serverSocket, null);
    }

    public static void closeReader(BufferedReader reader) {
        close(reader, null, null, null);
    }

    public static void closeReaderAndSocket(BufferedReader reader, Socket socket) {
        close(reader, null, null, socket);
    }

    public static void closeWriterAndSocket(PrintWriter writer, Socket socket) {
        close(null, writer, null, socket);
    }

    public static void closeSocket(Socket socket) {
        close(null, null, null, socket);
    }

    public static void closeWriterAndReaderAndSocket(BufferedReader reader, PrintWriter writer, Socket socket) {
        close(reader, writer, null, socket);
    }
}
