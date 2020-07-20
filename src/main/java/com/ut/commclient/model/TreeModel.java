package com.ut.commclient.model;

import javafx.scene.control.Tab;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TreeModel {
    private String name;
    private Class<? extends Tab> tabClass;

    @Override
    public String toString() {
        return name;
    }
}
