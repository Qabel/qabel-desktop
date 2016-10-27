package de.qabel.desktop.ui.accounting.avatar;

import com.airhacks.afterburner.views.QabelFXMLView;
import javafx.scene.layout.Pane;

public class AvatarView extends QabelFXMLView {
    public AvatarView(String alias) {
        super(singleObjectMap("alias", alias));
    }

    /**
     * Get the view async and put it into the parent
     */
    public AvatarView place(Pane parent) {
        getViewAsync(parent.getChildren()::setAll);
        return this;
    }

    /**
     * Get the view async and put it into the parent (blocking)
     */
    public AvatarView placeSync(Pane parent) {
        parent.getChildren().setAll(getView());
        return this;
    }

    @Override
    public AvatarController getPresenter() {
        return (AvatarController) super.getPresenter();
    }
}
