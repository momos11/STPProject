package de.uniks.stp.controller.subcontroller;

import de.uniks.stp.StageManager;
import de.uniks.stp.builder.ModelBuilder;
import de.uniks.stp.net.RestClient;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import kong.unirest.JsonNode;

import java.util.Objects;
import java.util.Optional;
import java.util.ResourceBundle;

public class OverviewOwnerController {
    private final Parent view;
    private final ModelBuilder builder;
    private Label serverName;
    private TextField nameText;
    private final RestClient restClient;
    private VBox root;

    public OverviewOwnerController(Parent view, ModelBuilder modelBuilder) {
        this.view = view;
        this.builder = modelBuilder;
        this.restClient = modelBuilder.getRestClient();
    }

    public void init() {
        root = (VBox) view.lookup("rootCategory");
        this.serverName = (Label) view.lookup("#serverName");
        serverName.setText(builder.getCurrentServer().getName());
        Button deleteServer = (Button) view.lookup("#deleteServer");
        Button changeName = (Button) view.lookup("#serverChangeButton");
        this.nameText = (TextField) view.lookup("#nameText");
        //Buttons
        deleteServer.setOnAction(this::onDeleteServerClicked);
        changeName.setOnAction(this::onChangeNameClicked);
    }

    /**
     * Changes name of current server
     */
    private void onChangeNameClicked(ActionEvent actionEvent) {
        builder.getCurrentServer().setName(nameText.getText());
        restClient.putServer(builder.getCurrentServer().getId(), builder.getCurrentServer().getName(), builder.getPersonalUser().getUserKey(), response -> {
            builder.getCurrentServer().setName(nameText.getText());
        });
    }

    /**
     * Deletes current server and shows homeView with webSocket
     */
    private void onDeleteServerClicked(ActionEvent actionEvent) {
        ResourceBundle lang = StageManager.getLangBundle();
        ButtonType button = new ButtonType(lang.getString("button.deleteServer"));
        ButtonType button2 = new ButtonType(lang.getString("button.cancel"));
        Alert alert = new Alert(Alert.AlertType.INFORMATION, "", button, button2);
        alert.setTitle(lang.getString("window_title_serverSettings"));
        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.getStyleClass().remove("alert");
        ButtonBar buttonBar = (ButtonBar) alert.getDialogPane().lookup(".button-bar");
        alert.setHeaderText(lang.getString("warning.deleteServer"));
        buttonBar.setStyle("-fx-font-size: 14px;" +
                "-fx-text-fill: white;"
                + "-fx-background-color: indianred;");
        buttonBar.getButtons().get(0).setStyle("-fx-background-color: red;" + "-fx-text-fill: white;");
        buttonBar.getButtons().get(1).setStyle("-fx-background-color: red;" + "-fx-text-fill: white;");
        dialogPane.getStylesheets().add(
                StageManager.class.getResource("styles/AlertStyle.css").toExternalForm());
        dialogPane.getStyleClass().add("AlertStyle");
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == button) {
            //delete server
            restClient.deleteServer(builder.getCurrentServer().getId(), builder.getPersonalUser().getUserKey(), response -> {
                JsonNode body = response.getBody();
                System.out.println("Overview controller: " + body.toString());
                String status = body.getObject().getString("status");
                System.out.println("status: " + status);
            });
            Platform.runLater(() -> {
                Stage stage = (Stage) serverName.getScene().getWindow();
                stage.close();
            });

        }
    }
    public void setTheme() {
        if (builder.getTheme().equals("Bright")) {
            setWhiteMode();
        } else {
            setDarkMode();
        }
    }

    private void setWhiteMode() {
        root.getStylesheets().clear();
        root.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/de/uniks/stp/themes/bright/ServerSettings.css")).toExternalForm());
    }

    private void setDarkMode() {
        root.getStylesheets().clear();
        root.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/de/uniks/stp/themes/dark/ServerSettings.css")).toExternalForm());
    }
}
