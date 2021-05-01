import java.io.IOException;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class App {
    public static void main(String[] args) throws IOException
    {
        
        MyFtpClient ftpClient = new MyFtpClient();

        JFrame frame = new JFrame("FTP CLIENT");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(600,600);
        Font font1 = new Font("SansSerif", Font.PLAIN, 20);
        JLabel connectLabel = new JLabel("CONNECTION:");
        connectLabel.setFont(font1);
        connectLabel.setBounds(20,0,500,25);

        JTextField hostField = new JTextField(10);
        hostField.setFont(font1);
        hostField.setText("192.168.1.74");
        hostField.setBounds(20,30,270,25);
        JTextField portField = new JTextField(4);
        portField.setFont(font1);
        portField.setText("21");
        portField.setBounds(300,30,95,25);
        JTextField usrField = new JTextField(4);
        usrField.setFont(font1);
        usrField.setText("kkersis");
        usrField.setBounds(20,60,150,25);
        JPasswordField passField = new JPasswordField(4);
        passField.setFont(font1);
        passField.setText("");
        passField.setBounds(20,90,150,25);
        JButton connectButton = new JButton("Connect");
        connectButton.setBounds(20,120,560,25);
        
        JTextField inputField = new JTextField();
        inputField.setFont(font1);
        inputField.setBounds(20,150,250,25);

        JButton getButton = new JButton("RETRIEVE");
        getButton.setBounds(20,180,100,25);

        JButton uploadButton = new JButton("UPLOAD");
        uploadButton.setBounds(20,210,100,25);

        JButton cdButton = new JButton("CD");
        cdButton.setBounds(20,240,100,25);

        JButton delButton = new JButton("DELETE");
        delButton.setBounds(140,180,100,25);

        JButton mkdirButton = new JButton("NEW DIR");
        mkdirButton.setBounds(140,210,100,25);

        JLabel currDirArea = new JLabel();
        currDirArea.setBounds(140,240,400,25);

        JButton refreshButton = new JButton("REFRESH");
        refreshButton.setBounds(480,240,100,25);

        JTextArea dirArea = new JTextArea();
        dirArea.setEditable(false);
        dirArea.setBounds(20, 270, 560, 270);

        JScrollPane scroll = new JScrollPane (dirArea, 
        JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        scroll.setBounds(20, 270, 560, 270);

        
        

        connectButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                try{
                    ftpClient.disconnect();
                    ftpClient.connect(hostField.getText(),Integer.parseInt(portField.getText()) , usrField.getText(), passField.getText());
                    dirArea.setText(ftpClient.list());
                    currDirArea.setText(ftpClient.pwd());
                }catch(IOException ex){
                    ex.printStackTrace();
                }
            }
        });

        getButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                try{
                    ftpClient.retrieve(inputField.getText());
                }catch(IOException ex){
                    ex.printStackTrace();
                }
            }
        });

        uploadButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                try{
                    System.out.println(ftpClient.stor(inputField.getText())); 
                }catch(IOException ex){
                    ex.printStackTrace();
                }
            }
        });

        cdButton.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e) {
                try{
                    System.out.println(ftpClient.cwd(inputField.getText()));
                    currDirArea.setText(ftpClient.pwd());
                    dirArea.setText(ftpClient.list());
                }catch(IOException ex){
                    ex.printStackTrace();
                }
            }
        });

        delButton.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e) {
                try{
                    ftpClient.dele(inputField.getText());
                }catch(IOException ex){
                    ex.printStackTrace();
                }
            }
        });

        mkdirButton.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e) {
                try{
                    ftpClient.mkd(inputField.getText());
                }catch(IOException ex){
                    ex.printStackTrace();
                }
            }
        });

        refreshButton.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e) {
                try{
                    dirArea.setText(ftpClient.list());
                    currDirArea.setText(ftpClient.pwd());
                }catch(IOException ex){
                    ex.printStackTrace();
                }
            }
        });
        

        JPanel p = new JPanel();
        frame.add(p);
        

        p.setLayout(null);
        
        p.add(connectLabel);
        p.add(hostField);
        p.add(portField);
        p.add(usrField);
        p.add(passField);
        p.add(connectButton);
        p.add(inputField);
        p.add(getButton);
        p.add(uploadButton);
        p.add(cdButton);
        p.add(delButton);
        p.add(mkdirButton);
        p.add(refreshButton);
        p.add(currDirArea);
        p.add(scroll);

        
        frame.setVisible(true);
    }
}