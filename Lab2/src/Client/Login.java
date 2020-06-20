package Client;

import java.awt.*;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

public class Login extends Frame implements WindowListener {
    public Label nickLabel;
    public TextField login;
    public Button joinBtn;

    public Login() {
        addWindowListener(this);
        setLayout(new FlowLayout(FlowLayout.CENTER));

        nickLabel = new Label("Enter nickname:");
        joinBtn = new Button("Join");
        login = new TextField(10);

        joinBtn.addActionListener(actionEvent -> {
            new Client(login.getText());
            this.dispatchEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING));
        });

        login.addTextListener(action -> {
            joinBtn.setEnabled(login.getText().length() > 0);
        });

        login.addActionListener(actionEvent -> {
            new Client(login.getText());
            this.dispatchEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING));
        });

        add(nickLabel);
        add(login);
        add(joinBtn);

        this.setSize(300, 100);
        this.setVisible(true);
        this.setTitle("Lab2");
    }

    @Override
    public void windowOpened(WindowEvent windowEvent) { }

    @Override
    public void windowClosing(WindowEvent windowEvent) {
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
