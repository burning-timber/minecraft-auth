package com.burningtimber.minecraft.btauth;

import com.amazonaws.services.secretsmanager.AWSSecretsManager;
import com.amazonaws.services.secretsmanager.AWSSecretsManagerClientBuilder;
import com.amazonaws.services.secretsmanager.model.DecryptionFailureException;
import com.amazonaws.services.secretsmanager.model.GetSecretValueRequest;
import com.amazonaws.services.secretsmanager.model.GetSecretValueResult;
import com.amazonaws.services.secretsmanager.model.ResourceNotFoundException;

import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.util.Base64;

public final class BtAuth extends JavaPlugin {
    private BtAuthConfig pluginConfig;
    private BtAuthEventListener authListener;

    public BtAuth() {
        super();
        this.pluginConfig = new BtAuthConfig();
        this.authListener = new BtAuthEventListener(pluginConfig);
    }

    private void setAuth0ClientInfo() {
        AWSSecretsManager client = AWSSecretsManagerClientBuilder.standard()
                .withRegion(this.pluginConfig.getAwsRegion()).build();

        String secret, decodedBinarySecret;
        GetSecretValueRequest getSecretValueRequest = new GetSecretValueRequest()
                .withSecretId(this.pluginConfig.getAuth0SecretName());
        GetSecretValueResult getSecretValueResult = null;

        try {
            getSecretValueResult = client.getSecretValue(getSecretValueRequest);
        } catch (DecryptionFailureException e) {
            getLogger().severe("Secrets Manager can't decrypt the protected secret text using the provided KMS key");
            getLogger().throwing(this.getClass().getName(), e.getStackTrace()[0].getMethodName(), e);
        } catch (ResourceNotFoundException e) {
            getLogger().severe("AWS Secrets manager can't find the Auth0 management key");
            getLogger().throwing(this.getClass().getName(), e.getStackTrace()[0].getMethodName(), e);
        } catch (Exception e) {
            getLogger().throwing(this.getClass().getName(), e.getStackTrace()[0].getMethodName(), e);
        }

        String auth0SecretJSON;
        if (getSecretValueResult.getSecretString() != null) {
            auth0SecretJSON = getSecretValueResult.getSecretString();
        } else {
            auth0SecretJSON = new String(Base64.getDecoder().decode(getSecretValueResult.getSecretBinary()).array());
        }
        try {
            JSONObject auth0Secret = (JSONObject) new JSONParser().parse(auth0SecretJSON);
            this.pluginConfig.setAuth0ClientId((String) auth0Secret.get("client_id"));
            this.pluginConfig.setAuth0ClientSecret((String) auth0Secret.get("client_secret"));
            getLogger().info("Found Auth0 client ID: " + this.pluginConfig.getAuth0ClientId());
        } catch (ParseException e) {
            getLogger().severe("Failed to parse JSON from AWS Secrets Manager");
            getLogger().throwing(this.getClass().getName(), e.getStackTrace()[0].getMethodName(), e);
        }
    }

    @Override
    public void onEnable() {
        getLogger().info("Initializing Burning Timber authentication");
        setAuth0ClientInfo();
        getServer().getPluginManager().registerEvents(new BtAuthEventListener(this.pluginConfig), this);
    }

    @Override
    public void onDisable() {
        getLogger().info("Disabling Burning Timber authentication");
        HandlerList.unregisterAll(this.authListener);
    }
}
