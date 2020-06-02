package com.burningtimber.minecraft.btauth;

import java.util.Date;

import com.auth0.client.auth.AuthAPI;
import com.auth0.client.mgmt.ManagementAPI;
import com.auth0.client.mgmt.filter.UserFilter;
import com.auth0.exception.Auth0Exception;
import com.auth0.json.auth.TokenHolder;
import com.auth0.json.mgmt.users.UsersPage;
import com.auth0.net.AuthRequest;

import com.auth0.net.Request;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;

public class BtAuthEventListener implements Listener {
    private BtAuthConfig pluginConfig;
    private String auth0MgmtToken;
    private Date auth0MgmtTokenExpires;

    public BtAuthEventListener(BtAuthConfig config) {
        super();
        this.pluginConfig = config;
    }

    private ManagementAPI getAuth0MgmtApi() throws Auth0Exception {
        if(this.auth0MgmtToken == null || new Date().after(this.auth0MgmtTokenExpires)) {
            AuthAPI authAPI = new AuthAPI(
                    this.pluginConfig.getAuth0AuthDomain(),
                    this.pluginConfig.getAuth0ClientId(),
                    this.pluginConfig.getAuth0ClientSecret());
            AuthRequest authRequest = authAPI.requestToken("https://" + this.pluginConfig.getAuth0AuthDomain() + "/api/v2/");
            TokenHolder holder = authRequest.execute();
            this.auth0MgmtToken = holder.getAccessToken();
            long expiresMilliseconds = (holder.getExpiresIn() - 60) * 1000;
            this.auth0MgmtTokenExpires = new Date(new Date().getTime() + expiresMilliseconds);
        }
        return new ManagementAPI(this.pluginConfig.getAuth0AuthDomain(), this.auth0MgmtToken);
    }


    @EventHandler
    public void onPlayerJoin(AsyncPlayerPreLoginEvent event) {
        String playerUUID = event.getUniqueId().toString().replace("-","");
        UsersPage response;
        try {
            ManagementAPI userAPI = getAuth0MgmtApi();
            UserFilter query = new UserFilter().withQuery("user_metadata.minecraft_uuid:" + playerUUID);
            Request<UsersPage> request = userAPI.users().list(query);
            response = request.execute();
        } catch(Auth0Exception e) {
            Bukkit.getLogger().throwing(this.getClass().getName(), e.getStackTrace()[0].getMethodName(), e);
            event.disallow(
                    AsyncPlayerPreLoginEvent.Result.KICK_OTHER,
                    "Could not verify user account");
            return;
        }

        if(response.getItems().isEmpty()) {
            event.disallow(
                    AsyncPlayerPreLoginEvent.Result.KICK_WHITELIST,
                    "You must link your minecraft profile with a Burning Timber account");
        } else if(response.getItems().size() > 1) {
            event.disallow(
                    AsyncPlayerPreLoginEvent.Result.KICK_OTHER,
                    "Your minecraft profile is linked with multiple accounts");
        } else {
            event.allow();
        }

    }
}
