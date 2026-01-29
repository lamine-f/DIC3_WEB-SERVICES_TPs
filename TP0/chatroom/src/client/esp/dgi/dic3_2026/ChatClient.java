package esp.dgi.dic3_2026;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class ChatClient extends UnicastRemoteObject implements MessageListener {

    private final ChatRoom chatRoom;
    private String username = null;

    private final String title = "Logiciel de discussion en ligne";
    private final JFrame window = new JFrame(title);
    private final JTextArea txtOutput = new JTextArea();
    private final JTextField txtMessage = new JTextField();
    private final JButton btnSend = new JButton("Envoyer");

    public ChatClient(ChatRoom chatRoom) throws RemoteException {
        this.chatRoom = chatRoom;
        this.createIHM();
        this.requestUsername();
    }

    public void createIHM() {
        // Assemblage des composants
        JPanel panel = (JPanel) this.window.getContentPane();
        JScrollPane sclPane = new JScrollPane(txtOutput);
        panel.add(sclPane, BorderLayout.CENTER);
        JPanel southPanel = new JPanel(new BorderLayout());
        southPanel.add(this.txtMessage, BorderLayout.CENTER);
        southPanel.add(this.btnSend, BorderLayout.EAST);
        panel.add(southPanel, BorderLayout.SOUTH);

        // Gestion des évènements
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

        // Initialisation des attributs
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

        try {
            this.chatRoom.subscribe(this, this.username);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }

        if (this.username == null) System.exit(0);
    }

    public void window_windowClosing(WindowEvent e) {
        try {
            this.chatRoom.unsubscribe(this.username);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
        System.exit(-1);
    }

    public void btnSend_actionPerformed(ActionEvent e) {
        this.txtOutput.append(String.format("Vous: %s\n", this.txtMessage.getText()));
        try {
            this.chatRoom.postMessage(username, this.txtMessage.getText());
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
        this.txtMessage.setText("");
        this.txtMessage.requestFocus();
    }

    @Override
    public void displayMessage(String username, String message) throws RemoteException {
        this.txtOutput.append(String.format("%s: %s\n", username, message));
    }
}
