import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.StringTokenizer;
import java.io.*; 


public class MyFtpClient {


  private Socket socket = null;
  private BufferedReader reader = null;
  private BufferedWriter writer = null;

  public MyFtpClient() {

  }

  public synchronized void connect(String host, int port, String user,
      String pass) throws IOException {
    if (socket != null) {
      throw new IOException("Already connected. Disconnect first.");
    }
    socket = new Socket(host, port);
    reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
    writer = new BufferedWriter(
        new OutputStreamWriter(socket.getOutputStream()));

    String response = readLine();
    if (!response.startsWith("220 ")) {
      throw new IOException(
          "Received an unknown response when connecting to the FTP server: "
              + response);
    }

    sendLine("USER " + user);

    response = readLine();
    if (!response.startsWith("331 ")) {
      throw new IOException(
          "Received an unknown response after sending the user: "
              + response);
    }

    sendLine("PASS " + pass);

    response = readLine();
    if (!response.startsWith("230 ")) {
      throw new IOException(
          "Unable to log in with the supplied password: "
              + response);
    }

    System.out.println("CONNECTED");
    // Now logged in.
  }

  public synchronized void disconnect(){
    try {
      sendLine("QUIT");
    }catch(IOException e){
      System.out.println("Can't disconnect: not connected.");
    } 
    finally {
      socket = null;
    }
  }

  public synchronized boolean dele(String filename) throws IOException {
    sendLine("DELE " + filename);
    String response = readLine();
    System.out.println(response);
    return true;
  }

  public synchronized boolean mkd(String dirname) throws IOException {
    sendLine("MKD " + dirname);
    String response = readLine();
    System.out.println(response);
    return true;
  }


  public synchronized String pwd() throws IOException {
    sendLine("PWD");
    String dir = null;
    String response = readLine();
    System.out.println(response);
    if (response.startsWith("257 ")) {
      int firstQuote = response.indexOf('\"');
      int secondQuote = response.indexOf('\"', firstQuote + 1);
      if (secondQuote > 0) {
        dir = response.substring(firstQuote + 1, secondQuote);
      }
    }
    return dir;
  }

  public synchronized boolean cwd(String dir) throws IOException {
    sendLine("CWD " + dir);
    String response = readLine();
    return (response.startsWith("250 "));
  }

  public synchronized Socket enterPassive() throws IOException {
    String response;
    sendLine("PASV");
    response = reader.readLine();
    System.out.println(response);

    // establish passive connection:
    String ip = null;
    int port = -1;
    int opening = response.indexOf('(');
    int closing = response.indexOf(')', opening + 1);
    if (closing > 0) {
      String dataLink = response.substring(opening + 1, closing);
      StringTokenizer tokenizer = new StringTokenizer(dataLink, ",");
      try {
        ip = tokenizer.nextToken() + "." + tokenizer.nextToken() + "."
            + tokenizer.nextToken() + "." + tokenizer.nextToken();
        port = Integer.parseInt(tokenizer.nextToken()) * 256
            + Integer.parseInt(tokenizer.nextToken());
      } catch (Exception e) {
        throw new IOException("Received bad data link information: "
            + response);
      }
    }
    
    Socket passiveSocket = new Socket(ip, port);
    return passiveSocket;
  }

  public synchronized boolean stor(String filename) throws IOException {
    File file = new File(filename);

    if (file.isDirectory()) {
      throw new IOException("Cannot upload a directory.");
    }

    return stor(new FileInputStream(file), filename);
  }

  public synchronized String list() throws IOException {
    
    Socket passiveSocket = enterPassive();
    String response;

    sendLine("NLST");
    response = reader.readLine();
    System.out.println(response);


    DataInputStream in = new DataInputStream(new BufferedInputStream(passiveSocket.getInputStream()));
    String dataString = readFromServer(in);  

    System.out.println(dataString);
    passiveSocket.close();
    System.out.println(reader.readLine());

    return dataString;
  }

  public synchronized boolean getFileFromServer(DataInputStream in, String filename) throws IOException{
    FileOutputStream fos = new FileOutputStream(filename);
    while(in.available() > 0){
      fos.write(in.read());
    }
    fos.flush();
    fos.close();

    return true;
}

  public synchronized String readFromServer(DataInputStream in) throws IOException{
    String text = "";
    int c = 0;

    while (true){
        c = in.read();
        if (c == -1) break;
        if ( c != -1) text = text + (char) c;
    }
    return text;
}

/*private void sendToServer(String text) {
  try {
      outToServer.writeBytes(text + "\0");
  } catch (IOException i) {
      try {   // if IOError close socket as client is already closed
          socket.close();
          System.out.println("Connection to server has been closed");
      } catch (IOException ex) {}
  }
}*/


  public synchronized boolean retrieve(String filename) throws IOException{
    
    Socket passiveSocket = enterPassive();

    String response1;

    sendLine("RETR " + filename);

    response1 = reader.readLine();
    System.out.println(response1);
    boolean downloadsuccess = getFileFromServer(new DataInputStream(passiveSocket.getInputStream()), filename);
    if(!downloadsuccess) System.out.println("Couldn't retrieve the file.");

    System.out.println(reader.readLine());
    passiveSocket.close();

    return true;
  }

  public synchronized boolean stor(InputStream inputStream, String filename)
      throws IOException {

    BufferedInputStream input = new BufferedInputStream(inputStream);

    Socket dataSocket = enterPassive();

    sendLine("STOR " + filename);

    String response = readLine();
    System.out.println(response);

    BufferedOutputStream output = new BufferedOutputStream(dataSocket
        .getOutputStream());
    byte[] buffer = new byte[4096];
    int bytesRead = 0;
    while ((bytesRead = input.read(buffer)) != -1) {
      output.write(buffer, 0, bytesRead);
    }
    output.flush();
    output.close();
    input.close();

    response = readLine();
    return response.startsWith("226 ");
  }

  private void sendLine(String line) throws IOException {
    if (socket == null) {
      throw new IOException("Not connected.");
    }
    try {
      writer.write(line + "\r\n");
      writer.flush();
    } catch (IOException e) {
      socket = null;
      throw e;
    }
  }

  private String readLine() throws IOException {
    String line = reader.readLine();
    return line;
  }

}