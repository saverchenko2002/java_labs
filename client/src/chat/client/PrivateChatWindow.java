package chat.client;


import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.StringTokenizer;

import chat.network.IStatusCodes;
import chat.network.TCPConnection;

public class PrivateChatWindow extends JFrame {
    private final TCPConnection connection;
    private final String userLogin;
    JButton leftConversationButton = new javax.swing.JButton();
    JLabel jLabel1 = new javax.swing.JLabel();
    JScrollPane jScrollPane1 = new javax.swing.JScrollPane();
    JTextArea log = new javax.swing.JTextArea();
    JTextField inputField = new javax.swing.JTextField();


    PrivateChatWindow(TCPConnection connection, String userLogin, String participantLogin) {
        super("PRIVATE CONVERSATION");
        this.connection = connection;
        this.userLogin = userLogin;
        setIconImage(Toolkit.getDefaultToolkit().getImage("icon.png"));
        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        setAlwaysOnTop(true);
        setVisible(true);


        leftConversationButton.setText("QUIT");

        jLabel1.setText("CONVERSATION WITH " + participantLogin.toUpperCase());

        log.setColumns(20);
        log.setRows(5);
        log.setEditable(false);
        jScrollPane1.setViewportView(log);

        inputField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String msg = inputField.getText();
                if (msg.equals("")) return;
                inputField.setText(null);
                updateLogs(userLogin + ": " + msg);
                System.out.println(IStatusCodes.PRIVATE_MESSAGE + " " + participantLogin + " " + userLogin + " " + userLogin + ": " + msg);
                connection.sendString(IStatusCodes.PRIVATE_MESSAGE + " " + participantLogin + " " + userLogin + " " + userLogin + ": " + msg);
            }
        });

        leftConversationButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
                connection.sendString(IStatusCodes.PRIVATE_MESSAGE_TOKEN_DEACTIVATE + " " + participantLogin + " " + userLogin);
            }
        });


        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                                .addGap(19, 19, 19)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                                .addComponent(inputField)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                                .addComponent(leftConversationButton, javax.swing.GroupLayout.PREFERRED_SIZE, 96, javax.swing.GroupLayout.PREFERRED_SIZE))
                                        .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 257, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 210, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addContainerGap(20, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                .addContainerGap()
                                .addComponent(jLabel1)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 192, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 15, Short.MAX_VALUE)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(leftConversationButton)
                                        .addGroup(layout.createSequentialGroup()
                                                .addGap(1, 1, 1)
                                                .addComponent(inputField, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                .addContainerGap())
        );

        pack();
        validate();
    }

    public synchronized void updateLogs(String msg) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                log.append(msg + "\n");
                log.setCaretPosition(log.getDocument().getLength());
            }
        });
    }

    public void closeRequest(String msg) {
        StringTokenizer str = new StringTokenizer(msg, " ");
        String who = str.nextToken();
        String uselessTemp = str.nextToken();
        String leftMsg = str.nextToken();
        if (JOptionPane.showOptionDialog(this, who + " " + leftMsg,
                "participant left", JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE, null, null, null) == 0) {
            dispose();
        }
    }

}
