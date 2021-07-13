package de.uniks.stp.util;

import com.github.cliftonlabs.json_simple.JsonException;
import de.uniks.stp.StageManager;
import de.uniks.stp.builder.ModelBuilder;
import de.uniks.stp.controller.settings.SettingsControllerTest;
import de.uniks.stp.model.Message;
import de.uniks.stp.model.PrivateChat;
import de.uniks.stp.net.RestClient;
import de.uniks.stp.net.websocket.privatesocket.PrivateChatWebSocket;
import de.uniks.stp.net.websocket.privatesocket.PrivateSystemWebSocketClient;
import de.uniks.stp.net.websocket.serversocket.ServerChatWebSocket;
import de.uniks.stp.net.websocket.serversocket.ServerSystemWebSocket;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import kong.unirest.Callback;
import kong.unirest.HttpResponse;
import kong.unirest.JsonNode;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;
import org.testfx.framework.junit.ApplicationTest;
import org.testfx.util.WaitForAsyncUtils;

import javax.json.JsonObject;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static de.uniks.stp.util.Constants.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.doCallRealMethod;

@RunWith(MockitoJUnitRunner.Silent.class)
public class ResourceManagerTest extends ApplicationTest {
    private Stage stage;
    private StageManager app;

    @Mock
    private RestClient restClient;

    @Mock
    private HttpResponse<JsonNode> response;

    @Captor
    private ArgumentCaptor<Callback<JsonNode>> callbackCaptor;

    @InjectMocks
    StageManager mockApp = new StageManager();

    @Mock
    private PrivateSystemWebSocketClient privateSystemWebSocketClient;

    @Mock
    private PrivateChatWebSocket privateChatWebSocket;

    @Mock
    private ServerSystemWebSocket serverSystemWebSocket;

    @Mock
    private ServerChatWebSocket serverChatWebSocket;


    @BeforeClass
    public static void setupHeadlessMode() {
        System.setProperty("testfx.robot", "glass");
        System.setProperty("testfx.headless", "true");
        System.setProperty("headless.geometry", "1920x1080-32");
    }

    @Override
    public void start(Stage stage) throws InterruptedException {
        //start application
        ModelBuilder builder = new ModelBuilder();
        builder.setUSER_CLIENT(privateSystemWebSocketClient);
        builder.setPrivateChatWebSocketClient(privateChatWebSocket);
        builder.setSERVER_USER(serverSystemWebSocket);
        builder.setServerChatWebSocketClient(serverChatWebSocket);
        this.stage = stage;
        app = mockApp;
        StageManager.setBuilder(builder);
        app.setRestClient(restClient);

        builder.setLoadUserData(false);

        app.start(stage);
        this.stage.centerOnScreen();
    }

    @BeforeAll
    static void setup() {
        MockitoAnnotations.openMocks(SettingsControllerTest.class);
    }


    public void loginInit() throws InterruptedException {
        doCallRealMethod().when(privateSystemWebSocketClient).handleMessage(any());
        doCallRealMethod().when(privateSystemWebSocketClient).setBuilder(any());
        doCallRealMethod().when(privateSystemWebSocketClient).setPrivateViewController(any());
        doCallRealMethod().when(privateChatWebSocket).handleMessage(any());
        doCallRealMethod().when(privateChatWebSocket).setBuilder(any());
        doCallRealMethod().when(privateChatWebSocket).setPrivateViewController(any());
        doCallRealMethod().when(serverChatWebSocket).handleMessage(any());
        doCallRealMethod().when(serverChatWebSocket).setBuilder(any());
        doCallRealMethod().when(serverChatWebSocket).setServerViewController(any());

        JSONObject jsonString = new JSONObject()
                .put("status", "success")
                .put("name", "Peter")
                .put("password", "1234")
                .put("data", new JSONObject().put("userKey", "c3a981d1-d0a2-47fd-ad60-46c7754d9271"));
        String jsonNode = new JsonNode(jsonString.toString()).toString();
        when(response.getBody()).thenReturn(new JsonNode(jsonNode));
        doAnswer((Answer<Void>) invocation -> {
            String name = (String) invocation.getArguments()[0];
            String password = (String) invocation.getArguments()[1];
            System.out.println(name);
            System.out.println(password);
            Callback<JsonNode> callback = callbackCaptor.getValue();
            callback.completed(response);
            return null;
        }).when(restClient).login(anyString(), anyString(), callbackCaptor.capture());
        TextField usernameTextField = lookup("#usernameTextfield").query();
        usernameTextField.setText("Peter");
        PasswordField passwordField = lookup("#passwordTextField").query();
        passwordField.setText("1234");
        clickOn("#loginButton");

        String message = "{\"action\":\"userJoined\",\"data\":{\"id\":\"60c8b3fb44453702009c07b3\",\"name\":\"Gustav\"}}";
        JsonObject jsonObject = (JsonObject) org.glassfish.json.JsonUtil.toJson(message);
        privateSystemWebSocketClient.handleMessage(jsonObject);


        message = "{\"channel\":\"private\",\"to\":\"Mr. Poopybutthole\",\"message\":\"Hallo\",\"from\":\"Allyria Dayne\",\"timestamp\":1623805070036}\"";
        jsonObject = (JsonObject) org.glassfish.json.JsonUtil.toJson(message);
        privateChatWebSocket.handleMessage(jsonObject);
    }


    @Test
    public void saveAndLoadHighScoreTest() throws InterruptedException {
        loginInit();
        ResourceManager.loadHighScore("GustavTest");
        ResourceManager.saveHighScore("GustavTest", 9999);
        int highScore = ResourceManager.loadHighScore("GustavTest");
        Assert.assertEquals(highScore, 9999);
    }

    @Test
    public void loadSnakeGameIconTest() throws InterruptedException {
        loginInit();
        Image image = ResourceManager.loadSnakeGameIcon("apple");
        Assert.assertTrue(image.getUrl().contains("apple"));
    }

    @Test
    public void loadMuteGameStateTest() throws InterruptedException {
        loginInit();
        ResourceManager.saveMuteGameState(true, "GustavTest");
        Boolean isMute = ResourceManager.loadMuteGameState("GustavTest");
        Assert.assertTrue(isMute);
        ResourceManager.saveMuteGameState(false, "GustavTest");
        isMute = ResourceManager.loadMuteGameState("GustavTest");
        Assert.assertFalse(isMute);
    }


    @Test
    public void savePrivateChatTest() throws JsonException, IOException, InterruptedException {
        loginInit();
        Message message = new Message();
        message.setMessage("Hallo Test");
        PrivateChat privateChat = new PrivateChat();
        ResourceManager.savePrivatChat("GustavTest", "OttoTest", message);
        ResourceManager.loadPrivatChat("GustavTest", "OttoTest", privateChat);
        Assert.assertEquals(message.getMessage(), privateChat.getMessage().get(0).getMessage());
    }

    @Test
    public void extractEmojisTest() throws InterruptedException {
        loginInit();
        ResourceManager.extractEmojis();
        Assert.assertTrue(Files.isDirectory(Path.of(APPDIR_ACCORD_PATH + TEMP_PATH + EMOJIS_PATH)));
    }

    @Test
    public void saveNotificationsTest() throws InterruptedException {
        loginInit();
        File file = new File("de/uniks/stp/sounds/notification/default.wav");
        ResourceManager.saveNotifications(file);
        Assert.assertTrue(Files.exists(Path.of(APPDIR_ACCORD_PATH + SAVES_PATH + NOTIFICATION_PATH + "/" + file.getName())));
    }

    @Test
    public void getNotificationSoundFilesTest() throws InterruptedException {
        loginInit();
        List<File> files = ResourceManager.getNotificationSoundFiles();
        Assert.assertTrue(files.size() > 0);
    }

    @Test
    public void deleteNotificationSoundTest() throws InterruptedException {
        loginInit();
        ResourceManager.deleteNotificationSound("default");
        List<File> files = ResourceManager.getNotificationSoundFiles();
        Assert.assertEquals(0, files.size());
    }

    @Test
    public void setGetComboValueTest() throws InterruptedException {
        loginInit();
        ResourceManager.setComboValue("GustavTest", "default");
        String value = ResourceManager.getComboValue("GustavTest");
        Assert.assertEquals(value, "default");
    }

    @Test
    public void copyFileTest() throws IOException, URISyntaxException, InterruptedException {
        loginInit();
        File file = ResourceManager.getNotificationSoundFiles().get(0);
        String targetPath = APPDIR_ACCORD_PATH + TEMP_PATH  + "/" + file.getName();
        ResourceManager.copyFile(file, targetPath);
        Assert.assertTrue(Files.exists(Path.of( APPDIR_ACCORD_PATH + TEMP_PATH  + "/" + file.getName())));
    }

    @Test
    public void saveUserNameAndSound() throws InterruptedException, IOException {
        loginInit();
        Files.createDirectory(Path.of(APPDIR_ACCORD_PATH + SAVES_PATH + "/CurrentNotification/"));
        ResourceManager.saveUserNameAndSound("GustavTest", "test");
        Assert.assertTrue(Files.exists(Path.of(APPDIR_ACCORD_PATH + SAVES_PATH + "/CurrentNotification/" + "GustavTest" + ".json")));
    }

    @Test
    public void saveGetVolumeTest() throws InterruptedException {
        loginInit();
        ResourceManager.saveVolume("GustavTest", 50.00F);
        float volume = ResourceManager.getVolume("GustavTest");
        Assert.assertEquals(50.00F, volume, 0.0);
    }
}
