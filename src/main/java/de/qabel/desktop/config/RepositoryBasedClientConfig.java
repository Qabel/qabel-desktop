package de.qabel.desktop.config;

import de.qabel.core.config.Account;
import de.qabel.core.config.Identity;
import de.qabel.core.drop.DropURL;
import de.qabel.desktop.repository.*;
import de.qabel.desktop.repository.exception.EntityNotFoundExcepion;
import de.qabel.desktop.repository.exception.PersistenceException;

import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

public class RepositoryBasedClientConfig implements ClientConfig {
    public static final String ACCOUNT_KEY = "account";
    private final ClientConfigRepository configRepo;
    private final AccountRepository accountRepo;
    private final IdentityRepository identityRepo;
    private final DropStateRepository dropStateRepo;
    private final ShareNotificationRepository shareRepo;
    private final List<Consumer<Identity>> identityListener = new CopyOnWriteArrayList<>();
    private final List<Consumer<Account>> accountListener = new CopyOnWriteArrayList<>();

    public RepositoryBasedClientConfig(
        ClientConfigRepository configRepo,
        AccountRepository accountRepo,
        IdentityRepository identityRepo,
        DropStateRepository dropStateRepo,
        ShareNotificationRepository shareRepo
    ) {
        this.configRepo = configRepo;
        this.accountRepo = accountRepo;
        this.identityRepo = identityRepo;
        this.dropStateRepo = dropStateRepo;
        this.shareRepo = shareRepo;
    }

    @Override
    public boolean hasAccount() {
        try {
            return configRepo.contains(ACCOUNT_KEY);
        } catch (PersistenceException e) {
            throw new IllegalStateException("failed to check account", e);
        }
    }

    @Override
    public Account getAccount() {
        try {
            return accountRepo.find(configRepo.find("account"));
        } catch (PersistenceException | EntityNotFoundExcepion e) {
            throw new IllegalStateException("couldn't load current account", e);
        }
    }

    @Override
    public void setAccount(Account account) throws IllegalStateException {
        try {
            accountRepo.save(account);
            configRepo.save("account", String.valueOf(account.getId()));
            accountListener.forEach(c -> c.accept(account));
        } catch (PersistenceException e) {
            throw new IllegalStateException("failed to set account", e);
        }
    }

    @Override
    public void onSetAccount(Consumer<Account> consumer) {
        accountListener.add(consumer);
    }

    @Override
    public Identity getSelectedIdentity() {
        try {
            if (!configRepo.contains("identity")) {
                return null;
            }
            return identityRepo.find(Integer.valueOf(configRepo.find("identity")));
        } catch (PersistenceException e) {
            throw new IllegalStateException("failed to load identity", e);
        } catch (EntityNotFoundExcepion e) {
            return null;
        }
    }

    @Override
    public void selectIdentity(Identity identity) {
        try {
            if (identity == null) {
                configRepo.save("identity", null);
            } else {
                identityRepo.save(identity);
                configRepo.save("identity", String.valueOf(identity.getId()));
            }
            identityListener.forEach(c -> c.accept(identity));
        } catch (PersistenceException e) {
            throw new IllegalStateException("failed to select identity", e);
        }
    }

    @Override
    public void onSelectIdentity(Consumer<Identity> consumer) {
        identityListener.add(consumer);
    }

    @Override
    public boolean hasDeviceId() {
        try {
            return configRepo.contains("device_id");
        } catch (PersistenceException e) {
            throw new IllegalStateException("failed to check device id", e);
        }
    }

    @Override
    public void setDeviceId(String deviceId) {
        try {
            configRepo.save("device_id", deviceId);
        } catch (PersistenceException e) {
            throw new IllegalStateException("failed to set device id", e);
        }
    }

    @Override
    public String getDeviceId() {
        try {
            return configRepo.find("device_id");
        } catch (PersistenceException | EntityNotFoundExcepion e) {
            throw new IllegalStateException("failed to check device id", e);
        }
    }

    @Override
    public Date getLastDropPoll(Identity identity) {
        String drop = getDrop(identity);
        return getLastDropPoll(drop);
    }

    private String getDrop(Identity identity) {
        Set<DropURL> drops = identity.getDropUrls();
        return drops.toArray(new DropURL[drops.size()])[0].toString();
    }

    private Date getLastDropPoll(String drop) {
        try {
            String state = dropStateRepo.getDropState(drop);
            return new Date(Long.valueOf(state));
        } catch (Exception e) {
            return new Date(0L);
        }
    }

    @Override
    public void setLastDropPoll(Identity identity, Date lastDropPoll) {
        try {
            dropStateRepo.setDropState(getDrop(identity), String.valueOf(lastDropPoll.getTime()));
        } catch (PersistenceException e) {
            throw new IllegalStateException("failed to set drop state");
        }
    }

    @Override
    public ShareNotifications getShareNotification(Identity identity) {
        ShareNotifications result = new ShareNotifications();
        try {
            shareRepo.find(identity).forEach(result::add);
            shareRepo.onAdd(result::add, identity);
            shareRepo.onDelete(result::remove, identity);
            return result;
        } catch (PersistenceException e) {
            throw new IllegalStateException("failed to load shares", e);
        }
    }
}
