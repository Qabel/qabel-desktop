package de.qabel.desktop.ui.accounting;

import de.qabel.core.config.Identity;
import de.qabel.core.index.IndexHTTP;
import de.qabel.core.index.UpdateAction;
import de.qabel.core.index.UpdateIdentity;
import de.qabel.core.index.UpdateResult;

public class IndexClient {

    protected final IndexHTTP index;

    public IndexClient(IndexHTTP index) {
        this.index = index;
    }

    public UpdateResult updateIdentity(Identity identity) {
        UpdateIdentity updateIdentity = UpdateIdentity.Companion.fromIdentity(identity, UpdateAction.CREATE);
        return index.updateIdentity(updateIdentity);
    }

}
