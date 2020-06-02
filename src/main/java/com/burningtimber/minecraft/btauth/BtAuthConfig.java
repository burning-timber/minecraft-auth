package com.burningtimber.minecraft.btauth;

import com.amazonaws.regions.Regions;
import org.bukkit.Bukkit;

public class BtAuthConfig {
    private String auth0SecretName;
    private String auth0AuthDomain;
    private String auth0UserInfo;
    private String auth0AuthToken;
    private String auth0ClientId;
    private String auth0ClientSecret;

    private String awsRegion;

    public BtAuthConfig() {
        super();
        this.auth0SecretName = "minecraft/auth0";
        this.auth0AuthDomain = "burningtimber.auth0.com";
        this.auth0UserInfo   = this.auth0AuthDomain + "/userinfo";
        this.auth0AuthToken  = this.auth0AuthDomain + "/oauth/token";

        this.awsRegion = "us-east-1";
    }

    public String getAuth0ClientId() {
        return this.auth0ClientId;
    }

    public void setAuth0ClientId(String clientId) {
        this.auth0ClientId = clientId;
    }

    public String getAuth0ClientSecret() {
        return this.auth0ClientSecret;
    }

    public void setAuth0ClientSecret(String clientSecret) {
        this.auth0ClientSecret = clientSecret;
    }

    public String getAuth0SecretName() {
        return this.auth0SecretName;
    }

    public String getAuth0AuthDomain() {
        return this.auth0AuthDomain;
    }

    public String getAuth0UserInfo() {
        return this.auth0UserInfo;
    }

    public String getAuth0AuthToken() {
        return this.auth0AuthToken;
    }

    public String getAwsRegion() {
        return this.awsRegion;
    }

    public void setAwsRegion(String region) {
        Regions[] regions = Regions.values();
        for(Regions awsRegion: regions) {
            if(region.equalsIgnoreCase(awsRegion.getName())) {
                this.awsRegion = region;
                return;
            }
        }
        Bukkit.getLogger().warning("Invalid AWS Region specified, falling back to us-east-1");
        this.awsRegion = "us-east-1";
    }
}
