package de.uniks.stp.controller;

import de.uniks.stp.AlternateServerChannelListCellFactory;
import de.uniks.stp.model.Categories;
import de.uniks.stp.model.ServerChannel;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.input.MouseEvent;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

public class CategorySubController {
    private final ServerViewController serverViewController;
    private Parent view;
    private Categories category;
    private Label categoryName;
    private ListView<ServerChannel> channelList;
    private AlternateServerChannelListCellFactory channelListCellFactory;
    private int ROW_HEIGHT = 30;
    private PropertyChangeListener channelListPCL = this::onChannelNameChanged;

    public CategorySubController(Parent view, ServerViewController serverViewController, Categories category) {
        this.view = view;
        this.category = category;
        this.serverViewController = serverViewController;
    }

    public void init() {
        categoryName = (Label) view.lookup("#categoryName");
        categoryName.setText(category.getName());
        channelList = (ListView<ServerChannel>) view.lookup("#channellist");
        channelListCellFactory = new AlternateServerChannelListCellFactory();
        channelList.setCellFactory(channelListCellFactory);
        channelList.setItems(FXCollections.observableList(category.getChannel()));
        channelList.setOnMouseClicked(this::onChannelListClicked);
        //PCL
        category.addPropertyChangeListener(Categories.PROPERTY_CHANNEL, this::onChannelChanged);
        category.addPropertyChangeListener(Categories.PROPERTY_NAME, this::onCategoryNameChanged);

        for (ServerChannel channel : category.getChannel()) {
            channel.addPropertyChangeListener(ServerChannel.PROPERTY_NAME, this.channelListPCL);
        }

        if (category.getChannel().size() > 0) {
            channelList.setPrefHeight(category.getChannel().size() * ROW_HEIGHT);
        } else {
            channelList.setPrefHeight(ROW_HEIGHT);
        }
    }

    /**
     * sets the selectedChat new.
     */
    private void onChannelListClicked(MouseEvent mouseEvent) {
        ServerChannel channel = this.channelList.getSelectionModel().getSelectedItem();
        if (mouseEvent.getClickCount() == 2 && this.channelList.getItems().size() != 0 && ServerViewController.getSelectedChat() != channel) {
            channel.setUnreadMessagesCounter(0);
            ServerViewController.setSelectedChat(channel);
            System.out.println(channel.getName());
            serverViewController.refreshAllChannelLists();
            serverViewController.showMessageView();
        }
    }

    private void onChannelChanged(PropertyChangeEvent propertyChangeEvent) {
        Platform.runLater(() -> channelList.setItems(FXCollections.observableList(category.getChannel())));
        if (category.getChannel().size() > 0) {
            channelList.setPrefHeight(category.getChannel().size() * ROW_HEIGHT);
            for (ServerChannel channel : category.getChannel()) {
                ServerChannel theChannel = (ServerChannel) propertyChangeEvent.getNewValue(); // if newValue not null -> add PCL, else if null -> removePCL
                channel.removePropertyChangeListener(this.channelListPCL);
                channel.addPropertyChangeListener(ServerChannel.PROPERTY_NAME, this.channelListPCL);
            }
        } else {
            channelList.setPrefHeight(ROW_HEIGHT);
        }
    }

    /**
     * sets the new Categoryname
     */
    private void onCategoryNameChanged(PropertyChangeEvent propertyChangeEvent) {
        Platform.runLater(() -> categoryName.setText(category.getName()));
    }

    private void onChannelNameChanged(PropertyChangeEvent propertyChangeEvent) {
        channelList.refresh();
    }

    public Categories getCategories() {
        return category;
    }

    public void stop() {
        channelList.setOnMouseReleased(null);
        category.removePropertyChangeListener(Categories.PROPERTY_CHANNEL, this::onChannelChanged);
        category.removePropertyChangeListener(Categories.PROPERTY_NAME, this::onCategoryNameChanged);

        for (ServerChannel channel : category.getChannel()) {
            channel.removePropertyChangeListener(this.channelListPCL);
        }
    }

    public void refreshChannelList() {
        channelList.refresh();
    }
}
