package de.uniks.stp;

import de.uniks.stp.model.Channel;
import de.uniks.stp.model.Server;
import de.uniks.stp.model.User;
import de.uniks.stp.net.RestClient;
import javafx.scene.control.*;
import javafx.stage.Stage;
import kong.unirest.Callback;
import kong.unirest.HttpResponse;
import kong.unirest.JsonNode;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testfx.framework.junit.ApplicationTest;
import org.testfx.util.WaitForAsyncUtils;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ServerViewControllerTest extends ApplicationTest {

    private Stage stage;
    private StageManager app;

    @Override
    public void start (Stage stage) {
        //start application
        this.stage = stage;
        app = new StageManager();
        app.start(stage);
        this.stage.centerOnScreen();
    }

    @Mock
    private RestClient restMock;

    @Mock
    private HttpResponse<JsonNode> res;

    @Captor
    private ArgumentCaptor<Callback<JsonNode>> callbackCaptor;

    @Before
    public void setup () {
        MockitoAnnotations.openMocks(this);
    }

    public void loginInit() {
        TextField usernameTextField = lookup("#usernameTextfield").query();
        usernameTextField.setText("TestUser Team Bit Shift");
        PasswordField passwordField = lookup("#passwordTextField").query();
        passwordField.setText("test123");
        clickOn("#loginButton");

    }

    @Test
    public void showServerTest() throws InterruptedException{
        loginInit();
        WaitForAsyncUtils.waitForFxEvents();
        Thread.sleep(2000);
        ListView<Server> serverList = lookup("#scrollPaneServerBox").lookup("#serverList").query();
        clickOn(serverList.lookup("#server"));
        Label serverNameText = lookup("#serverName").query();
        Assert.assertEquals(serverNameText.getText(), serverList.getItems().get(0).getName());
    }

    @Test
    public void showServerUsersTest() throws InterruptedException{

    }
}
