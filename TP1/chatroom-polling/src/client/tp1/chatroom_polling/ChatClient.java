package tp1.chatroom_polling;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Vector;
import org.apache.xmlrpc.XmlRpcClient;

public class ChatClient implements MessageListener {

    private XmlRpcClient serverClient;
    private Thread pollingThread;
    private volatile boolean running = true;
    private String username = null;

    private final String title = "ChatRoom XML-RPC (Polling)";
    private final JFrame window = new JFrame(title);
    private final JTextArea txtOutput = new JTextArea();
    private final JTextField txtMessage = new JTextField();
    private final JButton btnSend = new JButton("Envoyer");

    public ChatClient() {
        this.connectToServer();
        this.createIHM();
        this.requestUsername();
        this.displayMessages();
    }

    private void connectToServer() {
        try {
            serverClient = new XmlRpcClient("http://localhost:80/RPC2");
            System.out.println("Connected to main server");
        } catch (Exception e) {
            System.err.println("Failed to connect to server: " + e);
            e.printStackTrace();
            System.exit(1);
        }
    }

    public void createIHM() {
        JPanel panel = (JPanel) this.window.getContentPane();
        JScrollPane sclPane = new JScrollPane(txtOutput);
        panel.add(sclPane, BorderLayout.CENTER);
        JPanel southPanel = new JPanel(new BorderLayout());
        southPanel.add(this.txtMessage, BorderLayout.CENTER);
        southPanel.add(this.btnSend, BorderLayout.EAST);
        panel.add(southPanel, BorderLayout.SOUTH);

        window.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                window_windowClosing(e);
            }
        });
        btnSend.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                btnSend_actionPerformed(e);
            }
        });
        txtMessage.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent event) {
                if (event.getKeyChar() == '\n')
                    btnSend_actionPerformed(null);
            }
        });

        this.txtOutput.setBackground(new Color(220, 220, 220));
        this.txtOutput.setEditable(false);
        this.window.setSize(500, 400);
        this.window.setVisible(true);
        this.txtMessage.requestFocus();
    }

    public void requestUsername() {
        this.username = JOptionPane.showInputDialog(
                this.window, "Entrez votre pseudo : ",
                this.title, JOptionPane.OK_OPTION
        );

        if (this.username == null || this.username.trim().isEmpty()) {
            System.exit(0);
        }

        try {
            Vector<Object> params = new Vector<>();
            params.addElement(this.username);
            Boolean result = (Boolean) serverClient.execute("chatroom.subscribe", params);
            if (!result) {
                JOptionPane.showMessageDialog(window, "Ce pseudo est deja utilise!", "Erreur", JOptionPane.ERROR_MESSAGE);
                System.exit(1);
            }
            this.window.setTitle(title + " - " + username);
            appendMessage("Systeme", "Bienvenue " + username + "!");
        } catch (Exception ex) {
            System.err.println("Failed to subscribe: " + ex);
            ex.printStackTrace();
            System.exit(1);
        }
    }

    @Override
    public void displayMessages() {
        pollingThread = new Thread(() -> {
            while (running) {
                try {
                    Vector<Object> params = new Vector<>();
                    params.addElement(username);
                    Vector<String> messages = (Vector<String>) serverClient.execute("chatroom.getMessages", params);

                    for (String message : messages) {
                        SwingUtilities.invokeLater(() -> {
                            txtOutput.append(message + "\n");
                        });
                    }

                    Thread.sleep(1000); // Polling toutes les secondes
                } catch (InterruptedException e) {
                    break;
                } catch (Exception e) {
                    System.err.println("Polling error: " + e.getMessage());
                }
            }
        });
        pollingThread.setDaemon(true);
        pollingThread.start();
    }

    public void window_windowClosing(WindowEvent e) {
        running = false;
        try {
            Vector<Object> params = new Vector<>();
            params.addElement(this.username);
            serverClient.execute("chatroom.unsubscribe", params);
        } catch (Exception ex) {
            System.err.println("Failed to unsubscribe: " + ex);
        }
        System.exit(0);
    }

    public void btnSend_actionPerformed(ActionEvent e) {
        String message = this.txtMessage.getText().trim();
        if (message.isEmpty()) return;

        this.txtOutput.append(String.format("Vous: %s\n", message));
        try {
            Vector<Object> params = new Vector<>();
            params.addElement(this.username);
            params.addElement(message);
            serverClient.execute("chatroom.postMessage", params);
        } catch (Exception ex) {
            System.err.println("Failed to send message: " + ex);
            ex.printStackTrace();
        }
        this.txtMessage.setText("");
        this.txtMessage.requestFocus();
    }

    public void appendMessage(String username, String message) {
        SwingUtilities.invokeLater(() -> {
            this.txtOutput.append(String.format("%s: %s\n", username, message));
        });
    }
}
