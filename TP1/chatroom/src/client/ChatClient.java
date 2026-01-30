import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Vector;
import org.apache.xmlrpc.XmlRpcClient;
import org.apache.xmlrpc.WebServer;

public class ChatClient {

    private XmlRpcClient serverClient;
    private WebServer clientServer;
    private final int clientPort;
    private String username = null;

    private final String title = "ChatRoom XML-RPC";
    private final JFrame window = new JFrame(title);
    private final JTextArea txtOutput = new JTextArea();
    private final JTextField txtMessage = new JTextField();
    private final JButton btnSend = new JButton("Envoyer");

    public ChatClient(int clientPort) {
        this.clientPort = clientPort;
        this.startClientServer();
        this.connectToServer();
        this.createIHM();
        this.requestUsername();
    }

    private void startClientServer() {
        try {
            clientServer = new WebServer(clientPort);
            clientServer.addHandler("client", new ClientHandler(this));
            clientServer.start();
            System.out.println("Client XML-RPC server started on port " + clientPort);
        } catch (Exception e) {
            System.err.println("Failed to start client server: " + e);
            e.printStackTrace();
            System.exit(1);
        }
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
            params.addElement(this.clientPort);
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

    public void window_windowClosing(WindowEvent e) {
        try {
            Vector<Object> params = new Vector<>();
            params.addElement(this.username);
            serverClient.execute("chatroom.unsubscribe", params);
        } catch (Exception ex) {
            System.err.println("Failed to unsubscribe: " + ex);
        }
        clientServer.shutdown();
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

    public static class ClientHandler {
        private ChatClient client;

        public ClientHandler(ChatClient client) {
            this.client = client;
        }

        public boolean displayMessage(String username, String message) {
            client.appendMessage(username, message);
            return true;
        }
    }
}
