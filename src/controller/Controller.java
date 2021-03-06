package controller;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import com.google.gson.Gson;
import core.ScrumBoard;
import core.ScrumTasksList;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.*;


public class Controller {
    ScrumBoard scrumBoard = new ScrumBoard();

    @FXML
    private FlowPane taskTable;

    public FlowPane getTaskTable() {
        return taskTable;
    }

    @FXML
    void loadFile(ActionEvent event) {
        scrumBoard = load();
        taskTable.getChildren().clear();

        for (ScrumTasksList i: scrumBoard.getBoard()) {
            FXMLLoader listLoader = new FXMLLoader();
            listLoader.setLocation(getClass().getResource("../resources/taskList.fxml"));

            try {
                listLoader.load();
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }

            ControllerTaskList taskListController = listLoader.getController();
            taskListController.setPaneController(this);
            taskListController.setNewList(i);
            taskTable.getChildren().add(listLoader.getRoot());
        }
    }

    @FXML
    void saveProgress(ActionEvent event) throws IOException {
        save(taskTable.getChildren());
    }

    @FXML
    void closeDesk(ActionEvent event) {
        closeCurrentWindow(taskTable);
    }

    @FXML
    private void createNewTask(MouseEvent event) {
        ControllerCreateListDialog controllerCreateListDialog = loadDialogWindow("../resources/createListDialog.fxml").getController();
        controllerCreateListDialog.setController(this);
    }

    @FXML
    void initialize() {
    }

    void closeCurrentWindow(Node node) {
        Stage stage = (Stage) node.getScene().getWindow();
        stage.close();
    }

    private void save(ObservableList<Node> children) throws IOException {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save file");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("*.json", "*.*"));
        File file = fileChooser.showSaveDialog(new Stage());
        if (file != null) {
            org.apache.commons.io.FileUtils.write(file, new Gson().toJson(scrumBoard), StandardCharsets.UTF_8);
        }
    }

    private ScrumBoard load() {
        FileChooser fileChooser = new FileChooser();
        ScrumBoard newBoard = new ScrumBoard();
        fileChooser.setTitle("Load file");
        File file = fileChooser.showOpenDialog(new Stage());
        if (file != null) {
            try {
                newBoard = new Gson().fromJson(org.apache.commons.io.FileUtils.readFileToString(file, StandardCharsets.UTF_8),
                        ScrumBoard.class);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return newBoard;
    }

    public void createList(String listName) {
        FXMLLoader listLoader = new FXMLLoader();
        listLoader.setLocation(getClass().getResource("../resources/taskList.fxml"));

        try {
            listLoader.load();
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }

        ControllerTaskList taskListController = listLoader.getController();
        scrumBoard.add(taskListController.getList());
        taskListController.setPaneController(this);
        taskListController.setName(listName);
        taskTable.getChildren().add(listLoader.getRoot());
    }

    public FXMLLoader loadDialogWindow(String name) {
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(getClass().getResource(name));

        try {
            loader.load();
        } catch (IOException e) {
            e.printStackTrace();
        }

        Parent root = loader.getRoot();
        Stage stage = new Stage();
        stage.setScene(new Scene(root));
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.show();

        return loader;
    }

    public void removeList(Node element, ScrumTasksList list) {
        taskTable.getChildren().remove(element);
        scrumBoard.remove(list);
    }
}
