package ru.geekbrains.chat.network;

import java.io.IOException;
//интерфейс используемый клиентом и сервером
public interface TCPConnectionListener {

    void onConnectionReady(TCPConnection tcpConnection);//когда мы запустили соединение
    void onReceiveString(TCPConnection tcpConnection, String value);// когда соединение приняло входящую строчку
    void onDisconnect(TCPConnection tcpConnection);//разрыв соединения
    void onException(TCPConnection tcpConnection, Exception e);//исключение, когда что то пошло не так


}
