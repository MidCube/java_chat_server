/*
 * Copyright 2020 Andrew Rice <acr31@cam.ac.uk>, Alastair Beresford <arb33@cam.ac.uk>, C.I. Griffiths
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.ac.cam.cig23.fjava.tick4;

import java.io.*;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.Socket;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

import uk.ac.cam.cl.fjava.messages.*;

public class ClientHandler {
  private Socket socket;
  private MultiQueue<Message> multiQueue;
  private String nickname;
  private MessageQueue<Message> clientMessages;


  public ClientHandler(Socket s, MultiQueue<Message> q) {

    this.socket = s;
    this.multiQueue = q;
    this.clientMessages = new SafeMessageQueue<Message>();
    this.multiQueue.register(this.clientMessages);
    Random random = new Random();
    this.nickname = "Anonymous" + random.nextInt(100000);
    String hostname = socket.getInetAddress().getHostName();
    StatusMessage joined = new StatusMessage(nickname+" connected from " + hostname +".");
    multiQueue.put(joined);
    Thread input =
            new Thread() {
              @Override
              public void run() {
                try {
                  InputStream input = s.getInputStream();
                  ObjectInputStream inputStream = new ObjectInputStream(input);
                  while (true) {

                    Object message = inputStream.readObject();
                    if (message instanceof ChangeNickMessage) {
                      ChangeNickMessage myMessage = (ChangeNickMessage) message;
                      //print time [name] Message
                      String oldNick = nickname;
                      nickname = myMessage.name;
                      StatusMessage newStatus = new StatusMessage(oldNick + " is now known as " + nickname + ".");
                      multiQueue.put(newStatus);
                    } else if (message instanceof ChatMessage) {
                      ChatMessage myMessage = (ChatMessage) message;
                      //print time [Server] Message
                      String note = myMessage.getMessage();
                      RelayMessage newMessage = new RelayMessage(nickname, note, new Date());
                      multiQueue.put(newMessage);
                    }
                  }
                } catch (IOException | ClassNotFoundException e) {
                  multiQueue.deregister(clientMessages);
                  multiQueue.put(new StatusMessage(nickname + " has disconnected."));
                  return;
                }



              }
            };
    input.start();

    Thread output =
            new Thread() {
              @Override
              public void run() {

                try {
                  OutputStream output = s.getOutputStream();
                  ObjectOutputStream out = new ObjectOutputStream(output);

                  while (true) {
                    Message sendMe = clientMessages.take();
                    if (sendMe instanceof RelayMessage) {
                      out.writeObject(sendMe);
                    } else if (sendMe instanceof StatusMessage) {
                      out.writeObject(sendMe);
                    }
                  }
                } catch (IOException e) {
                  multiQueue.deregister(clientMessages);
                  multiQueue.put(new StatusMessage(nickname + " has disconnected."));
                  return;
                }
              }
            };
    output.setDaemon(true);
    output.start();

  }



}
