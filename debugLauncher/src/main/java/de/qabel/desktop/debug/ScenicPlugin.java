package de.qabel.desktop.debug;

import de.qabel.core.event.EventSource;
import de.qabel.desktop.event.MainStageShownEvent;
import de.qabel.desktop.inject.CompositeServiceFactory;
import de.qabel.desktop.plugin.ClientPlugin;
import javafx.application.Platform;
import org.scenicview.ScenicView;
import rx.Scheduler;

import javax.inject.Inject;

public class ScenicPlugin implements ClientPlugin {
    @Inject
    Scheduler fxScheduler;

    @Override
    public void initialize(CompositeServiceFactory serviceFactory, EventSource events) {
        events.events(MainStageShownEvent.class).subscribe(event ->
            Platform.runLater(() -> ScenicView.show(event.getPrimaryStage().getScene()))
        );
    }
}
