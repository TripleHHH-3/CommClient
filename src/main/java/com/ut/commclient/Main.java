package com.ut.commclient;

import com.alibaba.fastjson.JSON;
import com.esotericsoftware.yamlbeans.YamlReader;
import com.ut.commclient.common.Host;
import com.ut.commclient.component.*;
import com.ut.commclient.config.Config;
import com.ut.commclient.model.TreeModel;
import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.io.FileReader;
import java.util.List;

public class Main extends Application {
    BorderPane rootPane;
    TreeView<TreeModel> treeView;
    StackPane stackPane;
    MenuBar menuBar;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        rootPane = new BorderPane();
        initRootPane();

        Scene scene = new Scene(rootPane);

        primaryStage.setScene(scene);
        primaryStage.setTitle("Communication Client");
        primaryStage.setHeight(600);
        primaryStage.setWidth(800);
        primaryStage.setOnCloseRequest(event -> System.exit(0));
        primaryStage.show();

        initConfig();
    }

    private void initConfig() {
        //读取配置文件
        Config config;
        try {
            YamlReader reader = new YamlReader(new FileReader("src\\main\\resources\\application.yml"));
            config = JSON.parseObject(JSON.toJSONString(reader.read()), Config.class);
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        ObservableList<Node> children = stackPane.getChildren();

        if (config == null) return;
        //执行tcpClient初始化
        List<Host> tcpClient = config.getTcpClient();
        if (tcpClient != null && tcpClient.size() > 0) {
            tcpClient.forEach(host -> {
                TcpClientTab tcpClientTab = new TcpClientTab();
                tcpClientTab.setText(host.getIp() + ":" + host.getPort());
                ((TabPane) children.get(0)).getTabs().add(tcpClientTab);
                tcpClientTab.getIpTxt().setText(host.getIp());
                tcpClientTab.getPortTxt().setText(host.getPort().toString());
                tcpClientTab.getBeginBtn().fire();
            });
        }

        //执行tcpServer初始化
        List<Host> tcpServer = config.getTcpServer();
        if (tcpServer != null && tcpServer.size() > 0) {
            tcpServer.forEach(host -> {
                TcpServerTab tcpServerTab = new TcpServerTab();
                tcpServerTab.setText(host.getPort().toString());
                ((TabPane) children.get(1)).getTabs().add(tcpServerTab);
                tcpServerTab.getPortTxt().setText(host.getPort().toString());
                tcpServerTab.getBeginBtn().fire();
            });
        }

        //执行udpDatagram初始化
        List<Host> udpDatagram = config.getUdpDatagram();
        if (udpDatagram != null && udpDatagram.size() > 0) {
            udpDatagram.forEach(host -> {
                UdpDatagramTab udpDatagramTab = new UdpDatagramTab();
                udpDatagramTab.setText(host.getIp() + ":" + host.getPort());
                ((TabPane) children.get(2)).getTabs().add(udpDatagramTab);
                udpDatagramTab.getIpTxt().setText(host.getIp());
                udpDatagramTab.getRecPortTxt().setText(host.getPort().toString());
                udpDatagramTab.getListenBtn().fire();
            });
        }

        //执行udpMulticast初始化
        List<Host> udpMulticast = config.getUdpMulticast();
        if (udpMulticast != null && udpMulticast.size() > 0) {
            udpMulticast.forEach(host -> {
                UdpMulticastTab udpMulticastTab = new UdpMulticastTab();
                udpMulticastTab.setText(host.getIp() + ":" + host.getPort());
                ((TabPane) children.get(3)).getTabs().add(udpMulticastTab);
                udpMulticastTab.getListenIpGroupTxt().setText(host.getIp());
                udpMulticastTab.getListenPortTxt().setText(host.getPort().toString());
                udpMulticastTab.getListenBeginBtn().fire();
            });
        }
    }

    private void initRootPane() {
        //顶部
        initTop();
        rootPane.setTop(menuBar);

        //左侧
        initLeft();
        rootPane.setLeft(treeView);

        //中心
        initCenter();
        rootPane.setCenter(stackPane);
    }

    private void initTop() {
        menuBar = new MenuBar();
        Menu menu = new Menu("文件");
        MenuItem menuItem = new MenuItem("新建");
        menu.getItems().add(menuItem);
        menuBar.getMenus().add(menu);
    }


    private void initLeft() {
        treeView = new TreeView<>();
        TreeItem<TreeModel> root = new TreeItem<>(new TreeModel("ROOT", null));

        TreeItem<TreeModel> tcp = new TreeItem<>(new TreeModel("TCP", null));
        TreeItem<TreeModel> tcpClient = new TreeItem<>(new TreeModel("tcpClient", TcpClientTab.class));
        TreeItem<TreeModel> tcpServer = new TreeItem<>(new TreeModel("tcpServer", TcpServerTab.class));
        tcp.getChildren().add(tcpClient);
        tcp.getChildren().add(tcpServer);
        tcp.setExpanded(true);

        TreeItem<TreeModel> udp = new TreeItem<>(new TreeModel("UDP", null));
        TreeItem<TreeModel> udpClient = new TreeItem<>(new TreeModel("udpDatagramTab", UdpDatagramTab.class));
        TreeItem<TreeModel> udpServer = new TreeItem<>(new TreeModel("udpMulticastTab", UdpMulticastTab.class));
        udp.getChildren().add(udpClient);
        udp.getChildren().add(udpServer);
        udp.setExpanded(true);

        root.getChildren().add(tcp);
        root.getChildren().add(udp);
        treeView.setRoot(root);
        treeView.setShowRoot(false);


        ContextMenu menu = new ContextMenu();
        MenuItem addItem = new MenuItem("新建");
        addItem.setOnAction(actionEvent -> {
            TreeItem<TreeModel> treeModel = treeView.getSelectionModel().getSelectedItem();
            ObservableList<Node> nodes = stackPane.getChildren();
            TabPane tabPane = (TabPane) nodes.get(nodes.size() - 1);
            try {
                Tab tab = treeModel.getValue().getTabClass().newInstance();
                tab.setText("New Tab");
                tabPane.getTabs().add(tab);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        menu.getItems().add(addItem);

        treeView.setContextMenu(menu);


        treeView.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<TreeItem<TreeModel>>() {
            @Override
            public void changed(ObservableValue<? extends TreeItem<TreeModel>> observableValue, TreeItem<TreeModel> oldItem, TreeItem<TreeModel> newItem) {
                //获取被点击项并获得对应的tab类，如果点击的是根节点则是null并返回
                Class<? extends Tab> tabClass = newItem.getValue().getTabClass();
                if (tabClass == null) return;

                ObservableList<Node> children = stackPane.getChildren();
                //找到与点击项对应的stack图层
                Node node = null;
                for (Node child : children) {
                    TabPane tabPane = (TabPane) child;
                    Tab tab = tabPane.getTabs().get(0);
                    if (tab.getClass() == tabClass) {
                        node = child;
                    }
                }
                //将stack顶图层设置隐藏
                children.get(children.size() - 1).setVisible(false);
                //把对应的图层设为显示并置于顶层
                if (node != null) {
                    node.setVisible(true);
                    node.toFront();
                }
            }
        });
    }


    private void initCenter() {
        stackPane = new StackPane();

        TcpClientTab tcpClientTab = new TcpClientTab();
        tcpClientTab.setText("New Tab");
        tcpClientTab.setClosable(false);
        TcpServerTab tcpServerTab = new TcpServerTab();
        tcpServerTab.setText("New Tab");
        tcpServerTab.setClosable(false);
        UdpDatagramTab udpDatagramTab = new UdpDatagramTab();
        udpDatagramTab.setText("New Tab");
        udpDatagramTab.setClosable(false);
        UdpMulticastTab udpMulticastTab = new UdpMulticastTab();
        udpMulticastTab.setText("New Tab");
        udpMulticastTab.setClosable(false);

        TabPane tabPane = new TabPane();
        TabPane tabPane2 = new TabPane();
        TabPane tabPane3 = new TabPane();
        TabPane tabPane4 = new TabPane();

        tabPane.getTabs().add(tcpClientTab);
        tabPane2.getTabs().add(tcpServerTab);
        tabPane3.getTabs().add(udpDatagramTab);
        tabPane4.getTabs().add(udpMulticastTab);

        stackPane.getChildren().addAll(tabPane, tabPane2, tabPane3, tabPane4);
        stackPane.getChildren().forEach(node -> node.setVisible(false));
    }
}
