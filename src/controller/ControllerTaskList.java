package controller;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import core.ScrumTask;
import core.ScrumTasksList;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.*;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class ControllerTaskList extends Controller {
    Controller paneController;
    ScrumTasksList newList = new ScrumTasksList();
    ObservableList<ScrumTask> newObsList = FXCollections.observableList(newList.getList());

    static DataFormat dataFormat = new DataFormat("name", "list");

    public void setPaneController(Controller paneController) {
        this.paneController = paneController;
    }

    public void setNewList(ScrumTasksList list) {
        newList = list;
        setName(newList.getListName());
        newObsList = FXCollections.observableList(newList.getList());
        listOfTasks.setItems(newObsList);
    }

    @FXML
    private ListView<ScrumTask> listOfTasks;

    @FXML
    private AnchorPane backPanel;

    @FXML
    public Text taskListName;

    @FXML
    public MenuItem deleteThisList;

    @FXML
    void editNameList(ActionEvent event) {
        ControllerEditNameListDialog controllerEditNameListDialog = loadDialogWindow("../resources/editNameListDialog.fxml").getController();
        controllerEditNameListDialog.setController(this);
    }

    @FXML
    void addNew(MouseEvent event) {
        ControllerAddNewTask controllerAddNewTask = loadDialogWindow("../resources/addNewTask.fxml").getController();
        controllerAddNewTask.setController(this);
    }

    @FXML
    void closeList(ActionEvent event) {
        closeThisList();
    }

    @FXML
    void editSelectedItem(ActionEvent event) {
        ControllerEditSelectedItem controllerEditSelectedItem = loadDialogWindow("../resources/editSelectedItem.fxml").getController();
        controllerEditSelectedItem.setController(this);
    }

    @FXML
    void initialize() {
        listOfTasks.setItems(newObsList);
        listOfTasks.setUserData(this);
        listOfTasks.setOnDragDetected(new EventHandler<MouseEvent>(){
            @Override
            public void handle(MouseEvent event) {
                Dragboard db = listOfTasks.startDragAndDrop(TransferMode.MOVE);
                ScrumTask items = listOfTasks.getSelectionModel().getSelectedItem();

                ClipboardContent content = new ClipboardContent();
                if(items != null)
                    content.put(dataFormat, items.toString());
                else
                    content.put(dataFormat, "");
                db.setContent(content);
                event.consume();
            }
        });

        listOfTasks.setOnDragOver(new EventHandler<DragEvent>(){
            @Override
            public void handle(DragEvent event) {
                event.acceptTransferModes(TransferMode.MOVE);
            }
        });

        listOfTasks.setOnDragDropped(new EventHandler<DragEvent>() {
            @Override
            public void handle(DragEvent event) {
                Dragboard dragboard = event.getDragboard();
                if ((event.getGestureTarget() instanceof ListView) && (event.getGestureSource() instanceof ListView) && !"".equals(dragboard.getContent(dataFormat))) {
                    ListView oldList = (ListView) event.getGestureSource();
                    ControllerTaskList oldController = (ControllerTaskList) oldList.getUserData();
                    oldController.removeTask();
                    oldList.getSelectionModel().clearSelection();

                    String[] task = dragboard.getContent(dataFormat).toString().split(" ");
                    if (task.length == 1) {
                        addTaskFromStr(task[0], "");
                    } else {
                        addTaskFromStr(task[0], task[1]);
                    }
                }
                event.consume();
            }
        });
    }

    void setName(String name) {
        newList.setName(name);
        taskListName.setText(name);
    }

    public void closeThisList() {
        VBox box = new VBox();
        box.setAlignment(Pos.CENTER);
        Button yes = new Button("Да");
        Button no = new Button("Нет");
        HBox but = new HBox(yes, no);
        but.setAlignment(Pos.CENTER);
        box.getChildren().addAll(new Label("Вы действительно хотите удалить этот список?"), but);
        Scene scene = new Scene(box);
        showWindow(scene);

        yes.setOnMouseClicked(MouseEvent -> {
            paneController.getTaskTable().getChildren().remove(backPanel);
            paneController.scrumBoard.remove(newList);
            closeCurrentWindow(yes);
        });

        no.setOnMouseClicked(MouseEvent -> closeCurrentWindow(no));
    }

    public void addTaskFromStr(String name, String date) {
        if (!("".equals(name) && "".equals(date))) {
            newObsList.add(new ScrumTask(name, date));
        }
    }

    public void removeTask() {
        newList.remove(listOfTasks.getSelectionModel().getSelectedIndex());
        listOfTasks.refresh();
    }

    public void changeTask(String nameTask, LocalDate date) {
        ScrumTask thisTask = listOfTasks.getSelectionModel().getSelectedItem();

        if (!("").equals(nameTask)) {
            thisTask.setTaskName(nameTask);
        }

        if (date != null) {
            thisTask.setTaskDate(date.toString());
        }

        listOfTasks.refresh();
    }
}
