package Client;

import Client.StorageManager.StorageFile;
import Client.StorageManager.StorageManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.util.LinkedList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Chat extends Frame implements WindowListener {
    public LinkedList<ChatMessages> chatsMessagesList = new LinkedList<>();
    public LinkedList<StorageFile> filesList = new LinkedList<>();
    private LinkedList<Panel> panelsList = new LinkedList<>();
    private final StorageManager storageManager = new StorageManager();
    public Button sendButton;
    public Button addFileButton;
    public List chatsList;
    public TextField msgField;
    private Client client;
    private JScrollPane scrollPane = new JScrollPane();
    final ListPanel chatArea = new ListPanel();
    private int currentChatIndex;

    private class Message {
        public String message;
        public LinkedList<StorageFile> files;

        public JPanel getMessagePanel() {
            JPanel controlPane = new JPanel();
            controlPane.setLayout(new BoxLayout(controlPane, BoxLayout.Y_AXIS));
            JTextArea textArea = new JTextArea(1, 30);
            textArea.setEditable(false);
            textArea.setLineWrap(true);
            textArea.append(message);
            controlPane.add(textArea);
            if (!files.isEmpty()) {
                for (StorageFile storageFile: files) {
                    JPanel buttonPanel = new JPanel();
                    Label fileNameLabel  = new Label(storageFile.getOriginalName());
                    Button downloadButton = new Button("Get");
                    downloadButton.addActionListener(actionEvent -> {
                        storageManager.getFileFromStorage(storageFile);
                    });
                    buttonPanel.setBackground(new Color(0xCC0000));
                    fileNameLabel.setForeground(new Color(0x000000));
                    buttonPanel.add(fileNameLabel);
                    buttonPanel.add(downloadButton);
                    controlPane.add(buttonPanel);
                }
            }
            return controlPane;
        }

        public Message(String msg, LinkedList<StorageFile> files) {
            message = msg;
            if (files == null) {
                this.files = new LinkedList<>();
            } else  {
                this.files = files;
            }
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

    private void clearFilesForSending() {
        for (Panel filePanel: panelsList) {
            this.remove(filePanel);
        }
        filesList.clear();
        panelsList.clear();
        storageManager.totalFileSize = 0;
    }

    private String correctFileName(String fileName) {
        Pattern pattern = Pattern.compile(" ");
        Matcher matcher = pattern.matcher(fileName);
        return matcher.replaceAll("_");
    }

    private Panel createFileControlElement(StorageFile file, double fileSize) {
        Panel controlPane;
        Button deleteButton;
        Label fileNameLabel;
        controlPane = new Panel();
        controlPane.setBackground(new Color(0xCC0000));
        controlPane.setLayout(new FlowLayout());
        deleteButton = new Button("Delete");
        deleteButton.addActionListener(actionEvent -> {
            try {
                storageManager.deleteFileFromStorage(filesList.get(filesList.indexOf(file)), sendButton);
            } catch (Exception ex) {
                System.out.println("File has already been deleted.");
            }
            panelsList.remove(controlPane);
            filesList.remove(file);
            storageManager.totalFileSize =- fileSize;
            remove(controlPane);
            updateLayout();
        });
        fileNameLabel = new Label(file.getOriginalName());
        fileNameLabel.setForeground(new Color(0x000000));
        controlPane.add(fileNameLabel);
        controlPane.add(deleteButton);
        controlPane.setVisible(true);
        return controlPane;
    }

    private void updateLayout() {
        revalidate();
        invalidate();
        repaint();
    }

    public void addMsg(String text, int index, LinkedList<StorageFile> files) {
        Message message = new Message(text, files);
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

    public LinkedList<StorageFile> parseFilesFromString(String filesArray) {
        if (filesArray.isEmpty()) {
            return null;
        }
        LinkedList<StorageFile> result = new LinkedList<>();
        Pattern p = Pattern.compile("[A-z.\\-_А-я0-9]+,[-A-z.0-9]+");
        Matcher m = p.matcher(filesArray);
        while (m.find()) {
            String[] data = m.group().split(",");
            result.add(new StorageFile(data[0], Integer.parseInt(data[1])));
        }
        return result;
    }

    public String parseFilesFromList(LinkedList<StorageFile> storageFiles) {
        StringBuilder result = new StringBuilder("[");
        for (StorageFile storageFile: storageFiles) {
            result.append("{").append(storageFile.getOriginalName()).append(",").append(storageFile.getID()).append("}");
        }
        result.append("]");
        System.out.println(result);
        return result.toString();
    }

    public void sendHandler() {
        if (!msgField.getText().trim().equals("")) {
            client.sendMsg(msgField.getText().trim(), 0, currentChatIndex, 0, parseFilesFromList(filesList));
            clearFilesForSending();
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

        addFileButton = new Button("Add file");
        addFileButton.addActionListener(actionEvent -> {
            FileDialog fileDialog = new FileDialog((Frame) null);
            fileDialog.setVisible(true);
            String filename = fileDialog.getFile();
            String directory = fileDialog.getDirectory();
            if (filename != null) {
                File file = new File(directory + filename);
                double fileSize = file.length() / 1048576.0;
                if (!storageManager.isValidFile(filename)) {
                    showErrorMessage("Forbidden extension", "File " + filename +  " with such extension is not allowed.");
                } else if (!storageManager.isValidFileSize(fileSize)) {
                    showErrorMessage("Maximum file size", "File " + file.getName() + " exceeds maximum file size limit (" + storageManager.fileSizeLimitMB + ").");
                } else if (!storageManager.isValidTotalFileSize(fileSize)) {
                    showErrorMessage("Maximum total file size", "Adding file " + file.getName() + " exceeds maximum file size limit ( " + storageManager.totalFileSizeLimitMB + " ) per message.");
                } else if (storageManager.isZip(filename) && !storageManager.checkZipFiles(file)) {
                    showErrorMessage("Wrong zip", "Zip file " + file.getName() + " contains files with forbidden extensions.");
                } else {
                    storageManager.totalFileSize += fileSize;
                    StorageFile newFileEntry = new StorageFile(correctFileName(filename));
                    filesList.add(newFileEntry);
                    try {
                        storageManager.putFileToStorage(file, newFileEntry, sendButton);
                        Panel filePanel = createFileControlElement(newFileEntry, fileSize);
                        panelsList.add(filePanel);
                        add(filePanel);
                        updateLayout();
                    } catch (Exception ex) {
                        System.out.println("Error");
                    }
                }
            }
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
        add(addFileButton);

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
