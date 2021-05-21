package de.uniks.stp.controller;

import de.uniks.stp.AlternateMessageListCellFactory;
import de.uniks.stp.builder.ModelBuilder;
import de.uniks.stp.model.Message;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import org.json.JSONObject;

import java.io.IOException;

public class ChatViewController {
    private ModelBuilder builder;
    private Parent view;
    private Button sendButton;
    private TextField messageTextField;
    private ListView<Message> messageList;
    private static ObservableList<Message> ob;
    private HBox messageBox;
    private boolean flag;


    public ChatViewController(Parent view, ModelBuilder builder, Boolean flag) {
        this.view = view;
        this.builder = builder;
        this.flag = flag;
    }

    public void init() {
        // Load all view references
        this.sendButton = (Button) view.lookup("#sendButton");
        this.messageTextField = (TextField) view.lookup("#messageTextField");
        this.sendButton.setOnAction(this::sendButtonClicked);
        this.messageBox = (HBox) view.lookup("#messageBox");
        messageBox.setHgrow(messageTextField, Priority.ALWAYS);
        //ListView with message as parameter and observableList
        messageList = (ListView<Message>) view.lookup("#messageListView");
        messageList.setStyle("-fx-background-color: grey;");
        messageList.setCellFactory(new AlternateMessageListCellFactory());
        ob = FXCollections.observableArrayList();
        this.messageList.setItems(ob);
        AlternateMessageListCellFactory.setCurrentUser(builder.getPersonalUser());
    }

    /**
     * get Text from TextField and build message
     */
    private void sendButtonClicked(ActionEvent actionEvent) {
        //get Text from TextField and clear TextField after
        String textMessage = messageTextField.getText();
        if (!textMessage.isEmpty()) {
            if (!flag) {
                AlternateMessageListCellFactory.setCurrentUser(builder.getPersonalUser());
                try {
                    if (builder.getPrivateChatWebSocketCLient() != null && PrivateViewController.getSelectedChat() != null)
                        builder.getPrivateChatWebSocketCLient().sendMessage(new JSONObject().put("channel", "private").put("to", PrivateViewController.getSelectedChat().getName()).put("message", textMessage).toString());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                AlternateMessageListCellFactory.setCurrentUser(builder.getPersonalUser());
                try {
                    if (builder.getServerChatWebSocketClient() != null)
                        builder.getServerChatWebSocketClient().sendMessage(new JSONObject().put("channel", builder.getCurrentServer().getCategories().get(0).getChannel().get(0).getId()).put("message", textMessage).toString());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * insert new message in observableList
     */
    public static void printMessage(Message msg) {
        if (PrivateViewController.getSelectedChat().getName().equals(msg.getChannel().getName())) // only print message when user is on correct chat channel
            Platform.runLater(() -> ob.add(msg));
        //change for server
    }

    public void clearMessageField() {
        this.messageTextField.setText("");
    }
}
