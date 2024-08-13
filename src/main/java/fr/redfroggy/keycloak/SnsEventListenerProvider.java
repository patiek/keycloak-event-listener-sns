package fr.redfroggy.keycloak;

import org.keycloak.events.Event;
import org.keycloak.events.EventListenerProvider;
import org.keycloak.events.admin.AdminEvent;
import org.keycloak.models.AbstractKeycloakTransaction;
import org.keycloak.models.KeycloakTransactionManager;
import org.keycloak.models.RealmProvider;
import org.keycloak.models.UserModel;
import org.keycloak.models.UserProvider;

public class SnsEventListenerProvider implements EventListenerProvider {

    private final SnsEventPublisher snsEventPublisher;
    private final UserProvider userProvider;
    private final RealmProvider realmProvider;
    private final KeycloakTransactionManager transactionManager;

    public SnsEventListenerProvider(SnsEventPublisher snsEventPublisher, KeycloakTransactionManager transactionManager,
                                    UserProvider userProvider, RealmProvider realmProvider) {
        this.snsEventPublisher = snsEventPublisher;
        this.transactionManager = transactionManager;
        this.userProvider = userProvider;
        this.realmProvider = realmProvider;
    }

    @Override
    public void onEvent(Event event) {
        String username = getUsername(event.getRealmId(), event.getUserId());
        SnsEvent snsEvent = new SnsEvent(event, username);

        AbstractKeycloakTransaction transaction = new AbstractKeycloakTransaction() {
            @Override
            protected void commitImpl() {
                snsEventPublisher.sendEvent(snsEvent);
            }

            @Override
            protected void rollbackImpl() {

            }
        };

        transaction.begin();
        transactionManager.enlistAfterCompletion(transaction);
    }

    @Override
    public void onEvent(AdminEvent event, boolean includeRepresentation) {
        String adminUserId = null;
        if (event.getAuthDetails() != null) {
            adminUserId = event.getAuthDetails().getUserId();
        }
        String username = getUsername(event.getRealmId(), adminUserId);
        SnsAdminEvent snsAdminEvent = new SnsAdminEvent(event, username);

        AbstractKeycloakTransaction transaction = new AbstractKeycloakTransaction() {
            @Override
            protected void commitImpl() {
                snsEventPublisher.sendAdminEvent(snsAdminEvent);
            }

            @Override
            protected void rollbackImpl() {

            }
        };

        transaction.begin();
        transactionManager.enlistAfterCompletion(transaction);
    }

    @Override
    public void close() {
        // No-op if no cleanup is needed
    }

    private String getUsername(String realmId, String userId) {
        if (userId != null) {
            UserModel user = userProvider.getUserById(realmProvider.getRealm(realmId), userId);
            if (user != null) {
                return user.getUsername();
            }
        }
        return null;
    }
}
