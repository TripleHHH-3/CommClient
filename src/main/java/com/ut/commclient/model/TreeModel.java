package com.ut.commclient.model;

import javafx.scene.control.Tab;

public class TreeModel {
    private String name;
    private Class<? extends Tab> tabClass;

    public TreeModel() {
    }

    public TreeModel(String name, Class<? extends Tab> tabClass) {
        this.name = name;
        this.tabClass = tabClass;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Class<? extends Tab> getTabClass() {
        return tabClass;
    }

    public void setTabClass(Class<? extends Tab> tabClass) {
        this.tabClass = tabClass;
    }

    @Override
    public String toString() {
        return name;
    }
}
