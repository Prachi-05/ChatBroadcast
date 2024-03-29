import java.io.*;
import java.net.*;
import java.util.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

class MyClient implements ActionListener
{
    Socket s;
    DataInputStream dis;
    DataOutputStream dos;
    
    public final static int PORT=3000;
    public final static String UPDATE_USERS="updateuserslist:";
    public final static String LOGOUT_MESSAGE="logmeout:";
    public final static String SIGNUP_MESSAGE="signmeup:";

    JButton sendButton, logoutButton,loginButton, exitButton, addUser;
    JFrame chatWindow;
    JTextArea txtBroadcast;
    JTextArea txtMessage;
    JList usersList;

    public void displayGUI()
    {
        chatWindow=new JFrame();
        txtBroadcast=new JTextArea(5,30);
        txtBroadcast.setEditable(false);
        txtMessage=new JTextArea(2,20);
        usersList=new JList();

        sendButton=new JButton("Send");
        logoutButton=new JButton("Log out");
        loginButton=new JButton("Log in");
        exitButton=new JButton("Exit");
        addUser = new JButton("Sign up");

        JPanel center1=new JPanel();
        center1.setLayout(new BorderLayout());
        center1.add(new JLabel("Broad Cast messages from all online users",JLabel.CENTER),"North");
        center1.add(new JScrollPane(txtBroadcast),"Center");

        JPanel south1=new JPanel();
        south1.setLayout(new FlowLayout());
        south1.add(new JScrollPane(txtMessage));
        south1.add(sendButton);

        JPanel south2=new JPanel();
        south2.setLayout(new FlowLayout());
        south2.add(addUser);
        south2.add(loginButton);
        south2.add(logoutButton);
        south2.add(exitButton);

        JPanel south=new JPanel();
        south.setLayout(new GridLayout(2,1));
        south.add(south1);
        south.add(south2);

        JPanel east=new JPanel();
        east.setLayout(new BorderLayout());
        east.add(new JLabel("Online Users",JLabel.CENTER),"East");
        east.add(new JScrollPane(usersList),"South");

        chatWindow.add(east,"East");

        chatWindow.add(center1,"Center");
        chatWindow.add(south,"South");

        chatWindow.pack();
        chatWindow.setTitle("Login for Chat");
        chatWindow.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        chatWindow.setVisible(true);
        addUser.addActionListener(this);
        sendButton.addActionListener(this);
        logoutButton.addActionListener(this);
        loginButton.addActionListener(this);
        exitButton.addActionListener(this);
        logoutButton.setEnabled(false);
        loginButton.setEnabled(true);
        txtMessage.addFocusListener(new FocusAdapter(){
            public void focusGained(FocusEvent fe){
                txtMessage.selectAll();
            }
        });

        chatWindow.addWindowListener(new WindowAdapter()
        {
            public void windowClosing(WindowEvent ev)
            {
                if(s!=null)
                {
                    JOptionPane.showMessageDialog(chatWindow,"You are logged out right now. ","Exit",JOptionPane.INFORMATION_MESSAGE);
                    logoutSession();
                }
                System.exit(0);
            }
        });
    }
    public void actionPerformed(ActionEvent ev)
    {
        JButton temp=(JButton)ev.getSource();
        if(temp==addUser)
        {
            JTextField user = new JTextField();
            JTextField pass = new JPasswordField();
            JTextField pass2 = new JPasswordField();
            Object[] msg = {
                "Username:", user,
                "Password:", pass,
                "Confirm Password:", pass2
            };

            int o = JOptionPane.showConfirmDialog(chatWindow, msg, "Sign Up", JOptionPane.OK_CANCEL_OPTION);
            if (o == JOptionPane.OK_OPTION){
                String uname = user.getText();
                String pw = pass.getText();
                String pw2 = pass2.getText();
                if(uname.isEmpty() || pw.isEmpty() || pw2.isEmpty())
                    JOptionPane.showMessageDialog(chatWindow,"Enter credentials.","Exit",JOptionPane.INFORMATION_MESSAGE);
                else if(!pw.equals(pw2))
                    JOptionPane.showMessageDialog(chatWindow,"Your passwords don't match. \nPlease enter valid password.","Exit",JOptionPane.INFORMATION_MESSAGE);
                else
                    clientChat(MyClient.SIGNUP_MESSAGE+uname,pw);
            }
        }
        if(temp==sendButton)
        {
            if(s==null)
                {
                    JOptionPane.showMessageDialog(chatWindow,"You are not logged in. Please login first"); 
                    return;
                }
            try
            {
                dos.writeUTF(txtMessage.getText());
                txtMessage.setText("");
            }
            catch(Exception excp){
                txtBroadcast.append("\nsend button click :"+excp);
            }
        }
        if(temp==loginButton)
        {
            JTextField username = new JTextField();
            JTextField password = new JPasswordField();
            Object[] message = {
                "Username:", username,
                "Password:", password
            };

            int option = JOptionPane.showConfirmDialog(chatWindow, message, "Login", JOptionPane.OK_CANCEL_OPTION);
            if (option == JOptionPane.OK_OPTION){
                String uname = username.getText();
                String pwd = password.getText();
                if(uname.isEmpty() || pwd.isEmpty())
                    JOptionPane.showMessageDialog(chatWindow,"Enter credentials.","Exit",JOptionPane.INFORMATION_MESSAGE);
                else
                    clientChat(uname,pwd);
            }
            
            //if(uname!=null)
             //       clientChat(uname); 
        }
        if(temp==logoutButton)
        {
            if(s!=null)
                logoutSession();
        }
        if(temp==exitButton)
        {
            if(s!=null)
            {
                JOptionPane.showMessageDialog(chatWindow,"You are logged out right now. ","Exit",JOptionPane.INFORMATION_MESSAGE);
                logoutSession();
            }
            System.exit(0);
        }
    }
    public void logoutSession()
    {
        if(s==null) 
            return;
        try
        {
            dos.writeUTF(MyClient.LOGOUT_MESSAGE);
            Thread.sleep(500);
            s=null;
        }
        catch(Exception e)
        {
            txtBroadcast.append("\n inside logoutSession Method"+e);
        }
        logoutButton.setEnabled(false);
        loginButton.setEnabled(true);
        chatWindow.setTitle("Login for Chat");
    }
    /*public void login(String uname, String pass)
    {
        Connection con;
        Statement stmt;
        ResultSet rs;
        try {
            Class.forName("com.mysql.jdbc.Driver");
            
            con = DriverManager.getConnection("jdbc:mysql://localhost:3306/chat", "root", "alohomora");
            stmt = con.prepareStatement("select * from user;");
            rs = stmt.executeQuery("select * from user where uname='"+uname+"' and pass ='"+pass+"';");
            if(rs.next())
            {
                if(uname.equals(rs.getString(1)) && pass.equals(rs.getString(2)))
                    clientChat(uname,pass);
            }
            else
                JOptionPane.showMessageDialog(chatWindow,"Invalid username and password. ","Exit",JOptionPane.INFORMATION_MESSAGE);
        } catch (ClassNotFoundException | SQLException ex) {
            Logger.getLogger(MyClient.class.getName()).log(Level.SEVERE, null, ex);
        }
    }*/
   
    public void clientChat(String uname, String pass)
    {
        try
        {
            s=new Socket(InetAddress.getByName("192.168.0.111"),MyClient.PORT);
            dis=new DataInputStream(s.getInputStream());
            dos=new DataOutputStream(s.getOutputStream());
            dos.writeUTF(uname);
            dos.writeUTF(pass);
            String check = dis.readUTF();
            if(check.equals("signedUser"))
            {
                JOptionPane.showMessageDialog(chatWindow, "User registered successfully.");
            }
            if(check.equals("userAlreadyExists"))
            {
                JOptionPane.showMessageDialog(chatWindow, "Username is taken. Try again.");
            }
            if(check.equals("null"))
            {
                JOptionPane.showMessageDialog(chatWindow, "Error during Sign up process. Please try again.");
            }
            if(check.equals("valid"))
            {              
                ClientThread ct=new ClientThread(dis,this);
                Thread t1=new Thread(ct);
                t1.start();
                chatWindow.setTitle(uname+" Chat Window");
                logoutButton.setEnabled(true);
                loginButton.setEnabled(false);
            }
            if(check.equals("invalid"))
            {
                JOptionPane.showMessageDialog(chatWindow, "Invalid Username and Password");
                
            }
        }
        catch(Exception e)
        {
            txtBroadcast.append("\nClient Constructor " +e);
        }
        
    }
    public MyClient()
    {
            displayGUI();
    }
    public static void main(String []args)
    {
        new MyClient();
    }
}
class ClientThread implements Runnable
{
    DataInputStream dis;
    MyClient client;

    ClientThread(DataInputStream dis,MyClient client)
    {
        this.dis=dis;
        this.client=client;
    }

    public void run()
    {
        String s2="";
        do
        {
            try
            {
                s2=dis.readUTF();
                if(s2.startsWith(MyClient.UPDATE_USERS))
                    updateUsersList(s2);
                else if(s2.equals(MyClient.LOGOUT_MESSAGE))
                    break;
                else
                    client.txtBroadcast.append("\n"+s2);
                int lineOffset=client.txtBroadcast.getLineStartOffset(client.txtBroadcast.getLineCount()-1);
                client.txtBroadcast.setCaretPosition(lineOffset);
            }
            catch(Exception e){client.txtBroadcast.append("\nClientThread run : "+e);}
        }
        while(true);
    }
  
    public void updateUsersList(String ul)
    {
        Vector ulist=new Vector();
        ul=ul.replace("[","");
        ul=ul.replace("]","");
        ul=ul.replace(MyClient.UPDATE_USERS,"");
        StringTokenizer st=new StringTokenizer(ul,",");
        while(st.hasMoreTokens())
        {
            String temp=st.nextToken();
            ulist.add(temp);
        }
        client.usersList.setListData(ulist);
    }
}
