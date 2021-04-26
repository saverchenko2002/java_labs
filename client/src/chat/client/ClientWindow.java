package chat.client;

import chat.network.IStatusCodes;
import chat.network.TCPConnection;
import chat.network.TCPConnectionListener;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.StringTokenizer;

public class ClientWindow extends JFrame implements ActionListener, TCPConnectionListener, IClientGUIConstants, IStatusCodes {

    private static final String IP_ADDR = "localhost";
    private static final int PORT = 8080;
    private TCPConnection connection;

    private static final JTextArea log = new JTextArea(20, 5);
    private static final JPanel clientsList = new JPanel();

    private static JScrollPane scrollLog;
    private static JScrollPane scrollUsers;

    private final JTextField inputField = new JTextField();
    private final JTextField searchField = new JTextField();
    private final JTextField loginField = new JTextField();
    private final JTextField passwordField = new JTextField();

    private final JLabel loginLabel = new JLabel("Login: ");
    private final JLabel passwordLabel = new JLabel("Password: ");
    private final JLabel registerHint = new JLabel("Don't have an account yet?");

    public final JButton loginButton = new JButton("Sign in");
    private final JButton registerButton = new JButton("Sign up");
    private final JButton confirmButton = new JButton("CONFIRM");
    private final JButton disconnectButton = new JButton("Disconnect");

    ArrayList<JTextField> inputFields = new ArrayList<>();
    String userLogin;

    boolean registration = false;
    boolean connected = false;
    GroupLayout layout;
    String spaceHandler;
    ArrayList<JButton> buttons = new ArrayList<>();
    HashMap<String, JButton> clientsOnlineButtons = new HashMap<>();
    HashMap<String, PrivateChatWindow> privateConversation = new HashMap<>();
    ArrayList<String> currentPrivateConversationWithThisClient = new ArrayList<>();

    private ClientWindow() {

        super("Messenger");

        setIconImage(Toolkit.getDefaultToolkit().getImage("icon.png"));
        setAlwaysOnTop(true);

        putButtons();
        putInputFields();
        log.setEditable(false);
        scrollLog = new JScrollPane(log);
        log.setLineWrap(true);
        scrollUsers = new JScrollPane(clientsList);
        clientsList.setLayout(new GridLayout(10, 1, 0, 0));

        inputField.addActionListener(this);
        searchField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String request = searchField.getText();
                if (request.equals(""))
                    for (String key : clientsOnlineButtons.keySet())
                        clientsOnlineButtons.get(key).setVisible(true);
                else if (clientsOnlineButtons.containsKey(request)) {
                    clientsOnlineButtons.get(request).setVisible(true);
                }
                {
                    for (String key : clientsOnlineButtons.keySet()) {
                        if (!key.contains(request))
                            clientsOnlineButtons.get(key).setVisible(false);
                    }
                }

            }
        });

        setAllFontAndInsides();

        layout = new GroupLayout(getContentPane());
        buttonsListeners(layout);

        toLoginMenu(layout);

        setVisible(true);
    }

    private void setAllFontAndInsides() {
        inputField.setToolTipText("Type a message...");
        searchField.setToolTipText("Search for a friend...");
        loginLabel.setFont(logPageFont);
        loginButton.setFocusPainted(false);
        passwordLabel.setFont(logPageFont);
        registerHint.setFont(logPageFont);

        for (JButton button : buttons)
            button.setFont(buttonsFont);

        for (JTextField field : inputFields)
            field.setFont(logPageFont.deriveFont(Font.PLAIN, 16));

        confirmButton.setFont(confirmFont);
        log.setFont(logsFont);
    }


    private void buttonsListeners(GroupLayout layout) {
        loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                registration = false;
                toJoinForm(layout);
            }
        });

        registerButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                registration = true;
                toJoinForm(layout);
            }
        });

        confirmButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (loginField.getText().equals("") || passwordField.getText().equals("")) {
                    JOptionPane.showMessageDialog(ClientWindow.this, "PLEASE ENTER YOUR LOGIN DETAILS",
                            "LOGIN FAILED", JOptionPane.WARNING_MESSAGE);
                    toLoginMenu(layout);
                    return;
                }
                if (!connected)
                    try {
                        connection = new TCPConnection(ClientWindow.this, IP_ADDR, PORT);
                        connected = true;

                    } catch (IOException ioException) {
                        printMsg("Connection exception: " + ioException);
                    }
                spaceHandler = loginField.getText() + passwordField.getText();
                String[] split = spaceHandler.split(" ");
                if (split.length > 1) {
                    if (JOptionPane.showOptionDialog(ClientWindow.this, SPACES_ERROR,
                            "ERROR", JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE, null, null, null) == 0)
                        toLoginMenu(layout);
                    return;
                }
                if (connection == null) {
                    if (JOptionPane.showOptionDialog(ClientWindow.this, "SERVER_OFFLINE",
                            "ERROR", JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE, null, null, null) == 0)
                        toLoginMenu(layout);
                } else {
                    if (registration) {

                        connection.sendString(REGISTRATION_TOKEN + " " + loginField.getText() + " " + passwordField.getText());
                    } else
                        connection.sendString(LOGIN_TOKEN + " " + loginField.getText() + " " + passwordField.getText());
                }

            }

        });

        disconnectButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (currentPrivateConversationWithThisClient.size() != 0) {
                    JOptionPane.showOptionDialog(ClientWindow.this, "please quit your private conversations first",
                            ERROR_TITLE, JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE,
                            null, null, null);
                } else {
                    toLoginMenu(layout);
                    registration = false;
                    connected = false;
                    System.out.println("DISCONNECT BUTTON HASHMAP " + privateConversation.keySet());
                    System.out.println("DISCONNECT BUTTON ARRAYLIST " + currentPrivateConversationWithThisClient);
                    connection.sendString(DISCONNECT_TOKEN);
                    connection.disconnect();
                    userLogin = null;
                    clientsList.removeAll();
                    validate();
                    clientsOnlineButtons.clear();
                }
            }
        });
    }

    private void putButtons() {
        buttons.add(loginButton);
        buttons.add(registerButton);
        buttons.add(confirmButton);
        buttons.add(disconnectButton);
    }

    private void putInputFields() {
        inputFields.add(inputField);
        inputFields.add(searchField);
        inputFields.add(loginField);
        inputFields.add(passwordField);
    }

    public void toLoginMenu(GroupLayout layout) {
        getContentPane().removeAll();
        getContentPane().setLayout(layout);
        setResizable(false);
        layout.setHorizontalGroup(
                layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                        .addComponent(registerButton, GroupLayout.PREFERRED_SIZE, 200, GroupLayout.PREFERRED_SIZE)
                                        .addComponent(loginButton, GroupLayout.PREFERRED_SIZE, 200, GroupLayout.PREFERRED_SIZE))
                                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addGroup(GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(registerHint)
                                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
                layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(loginButton, GroupLayout.PREFERRED_SIZE, 35, GroupLayout.PREFERRED_SIZE)
                                .addGap(41, 41, 41)
                                .addComponent(registerButton, GroupLayout.PREFERRED_SIZE, 35, GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(registerHint))
        );

        loginField.setText(null);
        passwordField.setText(null);
        pack();
        validate();
        setLocationRelativeTo(null);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

    }

    public void toJoinForm(GroupLayout layout) {
        getContentPane().removeAll();
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
                layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addGroup(GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                .addGap(32, 32, 32)
                                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.TRAILING)
                                        .addComponent(passwordLabel)
                                        .addComponent(loginLabel))
                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, 30, Short.MAX_VALUE)
                                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                        .addComponent(confirmButton, GroupLayout.PREFERRED_SIZE, 89, GroupLayout.PREFERRED_SIZE)
                                        .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                                .addComponent(passwordField, GroupLayout.Alignment.TRAILING, GroupLayout.PREFERRED_SIZE, 173, GroupLayout.PREFERRED_SIZE)
                                                .addComponent(loginField, GroupLayout.Alignment.TRAILING, GroupLayout.PREFERRED_SIZE, 173, GroupLayout.PREFERRED_SIZE)))
                                .addGap(45, 45, 45))
        );
        layout.setVerticalGroup(
                layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                        .addComponent(loginField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                        .addComponent(loginLabel))
                                .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                        .addComponent(passwordField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                        .addComponent(passwordLabel))
                                .addGap(26, 26, 26)
                                .addComponent(confirmButton)
                                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        loginField.setText(null);
        passwordField.setText(null);
        pack();
        validate();
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

    }

    public void toServerChat(GroupLayout layout) {

        getContentPane().removeAll();
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                                .addGap(10, 10, 10)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                        .addComponent(scrollLog)
                                        .addComponent(inputField, javax.swing.GroupLayout.DEFAULT_SIZE, 536, Short.MAX_VALUE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(searchField, javax.swing.GroupLayout.DEFAULT_SIZE, 211, Short.MAX_VALUE)
                                        .addComponent(scrollUsers, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(disconnectButton))
                                .addContainerGap())
        );
        layout.setVerticalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                        .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                        .addGroup(layout.createSequentialGroup()
                                                .addGap(0, 0, Short.MAX_VALUE)
                                                .addComponent(searchField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(scrollUsers, javax.swing.GroupLayout.PREFERRED_SIZE, 592, javax.swing.GroupLayout.PREFERRED_SIZE))
                                        .addGroup(layout.createSequentialGroup()
                                                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                .addComponent(scrollLog, javax.swing.GroupLayout.PREFERRED_SIZE, 618, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(disconnectButton, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(inputField, javax.swing.GroupLayout.PREFERRED_SIZE, 45, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addContainerGap())
        );

        log.setText(null);
        loginField.setText(null);
        passwordField.setText(null);
        pack();
        validate();
        setLocationRelativeTo(null);
        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
    }


    @Override
    public void actionPerformed(ActionEvent e) {
        String msg = inputField.getText();
        if (msg.equals("")) return;
        inputField.setText(null);
        connection.sendString(SIMPLE_MESSAGE_TOKEN + " : " + msg);
    }

    @Override
    public void onConnectionReady(TCPConnection tcpConnection) {
        printMsg("Connection ready.");
    }

    @Override
    public void onReceiveString(TCPConnection tcpConnection, String value) {
        String[] conversationInfo = new String[2];
        String fullKey = "";
        String loginAddress = "";
        if (value == null)
            return;
        StringTokenizer str = new StringTokenizer(value, " ");
        String code = str.nextToken();
        if (code.equals(ONLINE_LIST_REFRESH)) {
            ArrayList<String> onlinePeople = new ArrayList<>();
            while (str.hasMoreTokens()) {
                onlinePeople.add(str.nextToken());
            }
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    String avoid = "avoid";
                    for (String login : onlinePeople) {
                        if (!clientsOnlineButtons.containsKey(login)) {
                            if (userLogin.equals(login))
                                continue;
                            clientsOnlineButtons.put(login, new JButton(login));
                            clientsList.add(clientsOnlineButtons.get(login));
                            clientsList.validate();
                            clientsList.repaint();
                        }
                    }
                    go:
                    {
                        for (String key : clientsOnlineButtons.keySet()) {
                            if (!onlinePeople.contains(key)) {
                                clientsList.remove(clientsOnlineButtons.get(key));
                                clientsOnlineButtons.remove(key);
                                clientsList.validate();
                                clientsList.repaint();
                                break go;
                            }
                        }
                    }
                    for (JButton button : clientsOnlineButtons.values()) {
                        System.out.println("clientsOnlineButtons  " + clientsOnlineButtons.keySet());
                        System.out.println(privateConversation.keySet());
                        button.addActionListener(new ActionListener() {
                            @Override
                            public void actionPerformed(ActionEvent e) {
                                if (currentPrivateConversationWithThisClient.contains(button.getText() + " " + userLogin)) {
                                    JOptionPane.showOptionDialog(ClientWindow.this, "you are currently in conv with this user",
                                            ERROR_TITLE, JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE,
                                            null, null, null);

                                } else
                                    connection.sendString(PRIVATE_MESSAGE_TOKEN_ACTIVATE + " " + userLogin + " " + button.getText());
                            }
                        });
                    }
                }
            });

        } else if (code.equals(PRIVATE_MESSENGER_RUN)) {
            loginAddress = str.nextToken();
            fullKey += loginAddress;
            fullKey += " ";
            fullKey += userLogin;


            System.out.println("КЛЮЧ ПОМЕЩЕНИЯ " + fullKey + " ДЛЯ ОКНА " + userLogin);
            if (!privateConversation.containsKey(fullKey))
                privateConversation.put(fullKey, new PrivateChatWindow(connection, userLogin, loginAddress));
            if (!currentPrivateConversationWithThisClient.contains(fullKey))
                currentPrivateConversationWithThisClient.add(fullKey);
            System.out.println("НАБОР КЛЮЧЕЙ " + privateConversation.keySet() + " ДЛЯ ОКНА " + userLogin);
            System.out.println("КОЛИЧЕСТВО ОКОН ДЛЯ ОКНА НОМЕР " + userLogin + " " + privateConversation.size());

        } else if (code.equals(PRIVATE_MESSAGE)) {
            String msg = "";
            fullKey = "";
            conversationInfo[0] = str.nextToken(); //to

            conversationInfo[1] = str.nextToken(); //from
            fullKey += conversationInfo[1] + " ";
            fullKey += conversationInfo[0];
            while (str.hasMoreTokens()) {
                msg += str.nextToken();
                msg += " ";
            }
            privateConversation.get(fullKey).updateLogs(msg);
        } else if (code.equals(PRIVATE_MESSAGE_TOKEN_DEACTIVATE)) {
            System.out.println("VALUE " + value);
            fullKey = "";
            fullKey += str.nextToken();
            fullKey += " ";
            fullKey += str.nextToken();
            if (!str.hasMoreTokens())
                privateConversation.get(fullKey).closeRequest(fullKey + " left the channel");

            for (String key : privateConversation.keySet()) {
                if (key.equals(fullKey)) {
                    privateConversation.remove(fullKey);
                    currentPrivateConversationWithThisClient.remove(fullKey);
                    return;
                }
            }

        } else {
            switch (value) {
                case (EXISTING_LOGIN): {
                    if (JOptionPane.showOptionDialog(ClientWindow.this, EXISTING_LOGIN,
                            ERROR_TITLE, JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE,
                            null, null, null) == 0)
                        toLoginMenu(layout);
                    tcpConnection.disconnect();
                    log.setText(null);
                    registration = false;
                    connected = false;
                    break;
                }

                case (NONEXISTENT_LOGIN): {
                    if (JOptionPane.showOptionDialog(ClientWindow.this, NONEXISTENT_LOGIN,
                            ERROR_TITLE, JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE,
                            null, null, null) == 0)
                        toLoginMenu(layout);
                    tcpConnection.disconnect();
                    log.setText(null);
                    registration = false;
                    connected = false;
                    break;
                }
                case (WRONG_PASSWORD): {
                    if (JOptionPane.showOptionDialog(ClientWindow.this, WRONG_PASSWORD,
                            ERROR_TITLE, JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE,
                            null, null, null) == 0)
                        toLoginMenu(layout);
                    tcpConnection.disconnect();
                    log.setText(null);
                    registration = false;
                    connected = false;
                    break;
                }
                case (TO_CHAT): {
                    registration = true;
                    userLogin = loginField.getText();
                    loginField.setText(null);
                    passwordField.setText(null);
                    toServerChat(layout);
                    break;
                }
                case (REGISTRATION_SUCCESS_TOKEN): {
                    if (JOptionPane.showOptionDialog(ClientWindow.this, REGISTRATION_SUCCESS_TOKEN,
                            "CONGRATULATIONS", JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE,
                            null, null, null) == 0)
                        toLoginMenu(layout);
                    break;
                }
                case (DUAL_CONNECTION_BLOCK): {
                    if (JOptionPane.showOptionDialog(ClientWindow.this, DUAL_ERROR,
                            DUAL_CONNECTION_BLOCK, JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE,
                            null, null, null) == 0)
                        toLoginMenu(layout);
                    break;
                }
                default:
                    printMsg(value);
            }
        }
    }

    @Override
    public void onDisconnect(TCPConnection tcpConnection) {
        tcpConnection.disconnect();
    }

    @Override
    public void onException(TCPConnection tcpConnection, Exception e) {
        printMsg("Exception " + e);
    }


    private synchronized void printMsg(String msg) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                log.append(msg + "\n");
                log.setCaretPosition(log.getDocument().getLength());
            }
        });
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new ClientWindow();
            }
        });

    }
}