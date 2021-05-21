package de.uniks.stp;

import de.uniks.stp.builder.ModelBuilder;
import de.uniks.stp.controller.HomeViewController;
import de.uniks.stp.controller.LoginScreenController;
import de.uniks.stp.controller.SettingsController;
import de.uniks.stp.controller.subcontroller.LanguageController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import kong.unirest.JsonNode;
import kong.unirest.Unirest;

import java.util.Objects;
import java.util.ResourceBundle;


public class StageManager extends Application {
    private static ModelBuilder builder;
    private static Stage stage;
    private static Stage subStage;
    private static HomeViewController homeViewController;
    private static LoginScreenController loginCtrl;
    private static SettingsController settingsController;
    private static Scene scene;
    private static ResourceBundle langBundle;

    @Override
    public void start(Stage primaryStage) {
        langBundle = ResourceBundle.getBundle("de/uniks/stp/LangBundle");
        SettingsController.setup();

        // start application
        stage = primaryStage;
        showLoginScreen();
        primaryStage.show();
    }

    public static void showLoginScreen() {
        cleanup();

        //show login screen
        try {
            Parent root = FXMLLoader.load(StageManager.class.getResource("LoginScreenView.fxml"), getLangBundle());
            scene = new Scene(root);
            builder = new ModelBuilder();
            loginCtrl = new LoginScreenController(root, builder);
            loginCtrl.init();
            stage.setTitle(getLangBundle().getString("window_title_login"));
            stage.setResizable(false);
            stage.setScene(scene);
            stage.sizeToScene();
            stage.centerOnScreen();
        } catch (Exception e) {
            System.err.println("Error on showing LoginScreen");
            e.printStackTrace();
        }
    }

    public static void showHome() {
        cleanup();
        try {
            Parent root = FXMLLoader.load(StageManager.class.getResource("HomeView.fxml"));
            scene.setRoot(root);
            homeViewController = new HomeViewController(root, builder);
            homeViewController.init();
            stage.setTitle("Accord - Main");
            stage.setScene(scene);
            stage.setResizable(true);
            stage.sizeToScene();
            stage.setMinHeight(650);
            stage.setMinWidth(900);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void stop() {
        try {
            super.stop();

            //automatic logout if application is closed
            if (!Objects.isNull(builder.getPersonalUser())) {
                String userKey = builder.getPersonalUser().getUserKey();
                if (userKey != null && !userKey.isEmpty()) {
                    JsonNode body = Unirest.post("https://ac.uniks.de/api/users/logout").header("userKey", userKey).asJson().getBody();
                    System.out.println("Logged out");
                }
            }


            Unirest.shutDown();
        } catch (Exception e) {
            System.err.println("Error while shutdown");
            e.printStackTrace();
        }
        cleanup();
    }


    private static void cleanup() {
        // call cascading stop
        if (loginCtrl != null) {
            loginCtrl.stop();
            loginCtrl = null;
        }
        if (homeViewController != null) {
            homeViewController.stop();
            homeViewController = null;
        }
    }

    public static void showSettingsScreen() {
        try {
            // load view
            Parent root = FXMLLoader.load(StageManager.class.getResource("view/settings/Settings.fxml"), getLangBundle());
            Scene scene = new Scene(root);

            // init controller
            settingsController = new SettingsController(root);
            settingsController.init();

            subStage = new Stage();
            subStage.setTitle(getLangBundle().getString("window_title_settings"));
            subStage.setResizable(false);
            subStage.setScene(scene);
            subStage.centerOnScreen();
            subStage.initOwner(stage);
            subStage.initModality(Modality.WINDOW_MODAL);
            subStage.setOnCloseRequest(event -> {
                if (settingsController != null) {
                    settingsController.stop();
                    settingsController = null;
                }
            });
            subStage.show();
        } catch (Exception e) {
            System.err.println("Error on showing Setting Screen");
            e.printStackTrace();
        }
    }

    public ModelBuilder getBuilder() {
        return builder;
    }

    public HomeViewController getHomeViewController() {
        return homeViewController;
    }

    public static ResourceBundle getLangBundle() {
        return langBundle;
    }

    public static void resetLangBundle() {
        langBundle = ResourceBundle.getBundle("de/uniks/stp/LangBundle");
    }

    public static void onLanguageChanged() {
        resetLangBundle();

        // Titles
        subStage.setTitle(getLangBundle().getString("window_title_settings"));
        stage.setTitle(getLangBundle().getString("window_title_login"));

        SettingsController.onLanguageChanged();
        LanguageController.onLanguageChanged();
        LoginScreenController.onLanguageChanged();
    }
}
