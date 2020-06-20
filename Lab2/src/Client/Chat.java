package Client;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.IOException;
import java.util.LinkedList;

public class Chat extends Frame implements WindowListener {
    public LinkedList<ChatMessages> chatsMessagesList = new LinkedList<>();
    public Button sendButton;
    public List chatsList;
    public TextField msgField;
    private Client client;
    private JScrollPane scrollPane = new JScrollPane();
    final ListPanel chatArea = new ListPanel();
    private int currentChatIndex;

    private class Message {
        public String message;

        public JPanel getMessagePanel() {
            JPanel controlPane = new JPanel();
            controlPane.setLayout(new BoxLayout(controlPane, BoxLayout.Y_AXIS));
            JTextArea textArea = new JTextArea(1, 30);
            textArea.setEditable(false);
            textArea.setLineWrap(true);
            textArea.append(message);
            controlPane.add(textArea);
            return controlPane;
        }

        public Message(String msg) {
            message = msg;
        }
    }

    private static class ChatMessages {
        public String chatName;
        public LinkedList<Message> messagesList;
        public int unreadMessagesCount;

        private ChatMessages(String chatName) {
            this.chatName = chatName;
            messagesList = new LinkedList<>();
            unreadMessagesCount = 0;
        }
    }

    private void showErrorMessage(String title, String message) {
        JOptionPane.showMessageDialog(this,
                message,
                title,
                JOptionPane.ERROR_MESSAGE);
    }

    private void updateLayout() {
        revalidate();
        invalidate();
        repaint();
    }

    public void addMsg(String text, int index) {
        Message message = new Message(text);
        chatsMessagesList.get(index).messagesList.add(message);
        if (currentChatIndex == index) {
            chatArea.addPanel(message.getMessagePanel(), 100);
        } else {
            chatsList.remove(index);
            chatsList.add(chatsMessagesList.get(index).chatName + " (" + (++chatsMessagesList.get(index).unreadMessagesCount) + ")", index);
        }
    }

    public void deleteChat(int index) {
        if (index == currentChatIndex) {
            currentChatIndex = 0;
            chatsList.select(0);
            scrollPane.removeAll();
            for (Message msg : chatsMessagesList.get(currentChatIndex).messagesList) {
                chatArea.addPanel(msg.getMessagePanel(), 100);
            }
        }
        chatsList.remove(index);
        chatsMessagesList.remove(index);
    }

    public void addChat(String nickname) {
        chatsList.add(nickname);
        chatsMessagesList.add(new ChatMessages(nickname));
    }

    public void sendHandler() {
        if (!msgField.getText().trim().equals("")) {
            try {
                client.sendMsg(msgField.getText().trim(), 0, currentChatIndex, 0);
            } catch (IOException e) {
                e.printStackTrace();
                client.close();
            }
            msgField.setText("");
        }
    }

    public Chat(Client client) {
        addWindowListener(this);
        this.client = client;
        chatsMessagesList.add(new ChatMessages("Main chat"));
        setLayout(new FlowLayout(FlowLayout.LEFT));

        chatsList = new List(21, false);
        chatsList.add("Main chat");
        chatsList.select(0);
        chatsList.addActionListener(actionEvent -> {
            currentChatIndex = chatsList.getSelectedIndex();
            chatsList.remove(currentChatIndex);
            chatsList.add(chatsMessagesList.get(currentChatIndex).chatName, currentChatIndex);
            chatsList.select(currentChatIndex);
            chatsMessagesList.get(currentChatIndex).unreadMessagesCount = 0;
            chatArea.removeAll();
            updateLayout();
            for (Message msg : chatsMessagesList.get(currentChatIndex).messagesList) {
                chatArea.addPanel(msg.getMessagePanel(), 100);
            }
        });
        currentChatIndex = 0;

        msgField = new TextField(49);
        msgField.addActionListener(actionEvent -> {
            sendHandler();
        });

        sendButton = new Button("Send");
        sendButton.addActionListener(actionEvent -> {
            sendHandler();
        });

        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        scrollPane.setViewportView(chatArea);

        JPanel panel = new JPanel();
        panel.setPreferredSize(new Dimension(340, 350));
        panel.setBackground(Color.CYAN);
        panel.setLayout(new BorderLayout());
        panel.add(scrollPane);

        add(panel);
        add(chatsList);
        add(msgField);
        add(sendButton);

        this.setSize(500,600);
        updateLayout();
        this.setVisible(true);
        this.setTitle(Client.clientName);
    }

    @Override
    public void windowOpened(WindowEvent windowEvent) { }

    @Override
    public void windowClosing(WindowEvent windowEvent) {
        client.close();
        dispose();
    }

    @Override
    public void windowClosed(WindowEvent windowEvent) { }

    @Override
    public void windowIconified(WindowEvent windowEvent) { }

    @Override
    public void windowDeiconified(WindowEvent windowEvent) { }

    @Override
    public void windowActivated(WindowEvent windowEvent) { }

    @Override
    public void windowDeactivated(WindowEvent windowEvent) { }
}
