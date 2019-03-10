package ru.geekbrains.chat.network;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.net.Socket;
import java.nio.charset.Charset;
import java.io.*;
// данный класс универсален - используется клиентом и сервером(сервер рассылает сообщения клиентам, клиет записывет себе в окошко)
public class TCPConnection {
    private final Socket socket;
    private final Thread rxThread;//поток слушающий входящие сообщения,у каждого  свой поток
    private final TCPConnectionListener eventListener;//слушатель событий
    private final BufferedReader in;
    private final BufferedWriter out;


    public TCPConnection(TCPConnectionListener eventListener, String ipAddr, int port) throws IOException{
        this(new Socket(ipAddr, port), eventListener);
    }

    public TCPConnection(Socket socket, TCPConnectionListener eventListener) throws IOException {//принимает готовый объект сокет и создаст сеодинение
        this.socket = socket;//спрашиваем сокет -> запоминаем
        in = new BufferedReader(new InputStreamReader(socket.getInputStream(), Charset.forName("UTF-8")));//поток ввола
        out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), Charset.forName("UTF-8")));//поток вывода
        this.eventListener = eventListener;
        rxThread = new Thread(new Runnable() {//поток, слушающий входящие соединения
            @Override
            public void run() {//метод ран заоверайденный от ранбл
                try {
                    eventListener.onConnectionReady(TCPConnection.this);
                    while (!rxThread.isInterrupted()){//пока поток не прерван
                        eventListener.onReceiveString(TCPConnection.this, in.readLine() );
                    }

                } catch (IOException e) {
                    eventListener.onException(TCPConnection.this, e);
                } finally {
                    eventListener.onDisconnect(TCPConnection.this);
                }
            }
        });
        rxThread.start();//запускаем поток

    }

    public synchronized void sendString(String value){//отправляем сообщение(потокобезопасный поток синхронайзд)
        try {
            out.write(value + "\r\n");
            out.flush();//сброс буфера
        } catch (IOException e) {
            eventListener.onException(TCPConnection.this, e);
            disconnect();
        }

    }

    public synchronized void disconnect(){//разрыв соединения(потокобезопасный поток синхронайзд)
        rxThread.isInterrupted();
        try {
            socket.close();
        } catch (IOException e) {
            eventListener.onException(TCPConnection.this, e);
        }
    }

    @Override
    public String toString() {
        return "TCPConnection: " + socket.getInetAddress() + ": " + socket.getPort();
    }//переопределние тустринг
}
