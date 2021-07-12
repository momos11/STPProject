package de.uniks.stp.cellfactories;

import de.uniks.stp.model.User;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.OverrunStyle;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Circle;

public class UserListCell implements javafx.util.Callback<ListView<User>, ListCell<User>> {
    /**
     * The <code>call</code> method is called when required, and is given a
     * single argument of type P, with a requirement that an object of type R
     * is returned.
     *
     * @param param The single argument upon which the returned value should be
     *              determined.
     * @return An object of type R that may be determined based on the provided
     * parameter value.
     */

    @Override
    public ListCell<User> call(ListView<User> param) {
        return new UserCell();
    }

    private static class UserCell extends ListCell<User> {
        protected void updateItem(User item, boolean empty) {
            // creates a HBox for each cell of the listView
            HBox cell = new HBox();
            Circle circle = new Circle(15);
            Label name = new Label();
            super.updateItem(item, empty);
            if (!empty) {
                cell.setId("user");
                cell.setAlignment(Pos.CENTER_LEFT);
                if (item.isStatus()) {
                    circle.setFill(Paint.valueOf("#13d86b"));
                } else {
                    circle.setFill(Paint.valueOf("#eb4034"));
                }
                name.setId(item.getId());
                name.setText("   " + item.getName());
                name.setStyle("-fx-font-size: 18");
                name.setTextOverrun(OverrunStyle.CENTER_ELLIPSIS);
                name.setPrefWidth(135);
                cell.getChildren().addAll(circle, name);
            }
            this.setGraphic(cell);
        }
    }
}