package de.qabel.desktop.ui.remotefs;

import de.qabel.box.storage.BoxObject;

public class FakeBoxObject extends BoxObject {
    public String ref = "";


    public FakeBoxObject(String name) {
        super(name);
    }

    @Override
    public String getRef() {
        return ref;
    }
}
