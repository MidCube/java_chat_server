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

import uk.ac.cam.cl.fjava.messages.Message;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class ChatServer {
  public static void main(String args[]) {

    int port;
    try {
      port = Integer.parseInt(args[0]);
    } catch(NumberFormatException | NullPointerException e) {
      System.out.println("Usage: java ChatServer "+args[0]);
      return;
    }
    //else create server
    //if unable return error
    ServerSocket myServer;
    try {
      myServer = new ServerSocket(port);
    } catch (IOException e){
      System.out.println("Cannot use port number "+args[0]);
      return;
    }

    //create multiq
    MultiQueue<Message> clientQs = new MultiQueue<>();
    //loop forever
    while(true) {
      //call accept
      try {
        Socket newSocket = myServer.accept();
        //create chat handler
        new ClientHandler(newSocket,clientQs);
      } catch (IOException e) {
        e.printStackTrace();
      }
    }

    //
  }
}
