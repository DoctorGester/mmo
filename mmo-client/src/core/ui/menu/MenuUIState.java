package core.ui.menu;

import com.jme3.app.Application;
import com.jme3.app.state.AbstractAppState;
import com.jme3.app.state.AppStateManager;
import com.jme3.font.BitmapFont;
import com.jme3.font.LineWrapMode;
import com.jme3.input.event.MouseButtonEvent;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector2f;
import core.exceptions.IncorrectHeaderException;
import core.graphics.MainFrame;
import core.main.DataUtil;
import core.main.Packet;
import program.main.Program;
import tonegod.gui.controls.buttons.Button;
import tonegod.gui.controls.buttons.ButtonAdapter;
import tonegod.gui.controls.form.Form;
import tonegod.gui.controls.text.Label;
import tonegod.gui.controls.text.Password;
import tonegod.gui.controls.text.TextField;
import tonegod.gui.controls.windows.Panel;
import tonegod.gui.core.Screen;
import tonegod.gui.core.layouts.Layout;
import tonegod.gui.core.layouts.MigLayout;

import java.io.UnsupportedEncodingException;

/**
 * @author doc
 */
public class MenuUIState extends AbstractAppState {
	private MainFrame frame;
	private Screen screen;

	private Vector2f dimension;

    private Label messageLabel;
	private Panel panel;
    private TextField passwordField;
    private TextField loginField;

    public MenuUIState(MainFrame frame) {
		this.frame = frame;
		this.screen = frame.getGuiScreen();
		dimension = new Vector2f(screen.getWidth(), screen.getHeight());
	}

	public void initialize(AppStateManager stateManager, Application app) {
		super.initialize(stateManager, app);

		Vector2f panelPosition = DataUtil.parseVector2f("65%, 30%", dimension),
				panelSize = DataUtil.parseVector2f("30%, 60%", dimension);
		panel = new Panel(screen, panelPosition, panelSize);
		panel.setIgnoreMouse(true);

        Layout layout = new MigLayout(screen, "[][]", "[][10%][][10%][][]", "margin 20 20 20 20");
        panel.setLayout(layout);

		screen.addElement(panel);

        Label loginLabel = new Label(screen, Vector2f.ZERO);
        loginLabel.setText("Username");
        loginLabel.getLayoutHints().define("cell 0 0", "span 2 1");
        panel.addChild(loginLabel);

        loginField = new TextField(screen);
        loginField.getLayoutHints().define("cell 1 0");
        panel.addChild(loginField);

        Label passwordLabel = new Label(screen, Vector2f.ZERO);
        passwordLabel.setText("Password");
        passwordLabel.getLayoutHints().define("cell 2 0");
        panel.addChild(passwordLabel);

        passwordField = new Password(screen);
        passwordField.getLayoutHints().define("cell 3 0");
        panel.addChild(passwordField);

        Button loginButton = new ButtonAdapter(screen){
            public void onButtonMouseLeftUp(MouseButtonEvent evt, boolean toggled) {
                sendLoginAndPassword(Program.HEADER_LOGIN, "Signing in");
            }
        };
        loginButton.getLayoutHints().define("cell 4 0", "span 1 1", "pad 0 10 20 0");
        panel.addChild(loginButton);

        Button registerButton = new ButtonAdapter(screen){
            public void onButtonMouseLeftUp(MouseButtonEvent evt, boolean toggled) {
                sendLoginAndPassword(Program.HEADER_REGISTER, "Waiting for response");
            }
        };
        registerButton.getLayoutHints().define("cell 4 1", "pad 10 0 20 0");
        panel.addChild(registerButton);

        messageLabel = new Label(screen, Vector2f.ZERO);
        messageLabel.getLayoutHints().define("cell 5 0", "span 2 1");
        messageLabel.setTextAlign(BitmapFont.Align.Center);
        messageLabel.setTextWrap(LineWrapMode.Word);
        messageLabel.setFontColor(ColorRGBA.Red);
        panel.addChild(messageLabel);

        layout.layoutChildren();

        // Setting text after we laid elements out, or else text position will break
        loginButton.setText("Login");
        registerButton.setText("Register");

		// Creating form for nice tab traversal
		Form form = new Form(screen);

		form.addFormElement(loginField);
		form.addFormElement(passwordField);
		form.addFormElement(loginButton);
		form.addFormElement(registerButton);
	}

	public void setMessage(String message, boolean error){
        messageLabel.setText(message);
        messageLabel.setFontColor(error ? ColorRGBA.Red : ColorRGBA.Green);
	}

	public void cleanup() {
		super.cleanup();

		screen.removeElement(panel);
	}

    private String getLogin(){
        return loginField.getText();
    }

    private String getPassword(){
        return passwordField.getText();
    }

    public void sendLoginAndPassword(byte[] header, String message) {
        setMessage(message, false);

        if (getInfoErrors(getLogin(), getPassword()))
            return;
        try {
            byte bname[] = getLogin().getBytes("UTF-8");
            byte bpass[] = getPassword().getBytes("UTF-8");
            byte data[] = new byte[2 + bname.length + bpass.length];

            data[0] = (byte) bname.length;
            data[1] = (byte) bpass.length;

            System.arraycopy(bname, 0, data, 2, bname.length);
            System.arraycopy(bpass, 0, data, 2 + bname.length, bpass.length);

            Packet p = new Packet(header, data);

            Program.getInstance().getLocalClient().send(p);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IncorrectHeaderException e) {
            e.printStackTrace();
        }
    }

    private boolean getInfoErrors(String name, String pass){
        String error = null;
        name = name.trim().replaceAll("\\s{2,}", " "); // Replacing 2+ spaces in name with one space and trimming it

        // Exit if name doesn't match pattern
        if (!name.matches("[a-zA-Z0-9 \\-_]{3,20}")) // Only latin letters, numbers, spaces, - or _, from 3 to 20 symbols
            error = "Invalid login format";

        // Exit if password is too big
        if (pass.length() > 32 || pass.length() < 3)
            error = "Password length is incorrect.";

        if (error != null){
            setMessage(error, true);
            return true;
        }
        return false;
    }
}
