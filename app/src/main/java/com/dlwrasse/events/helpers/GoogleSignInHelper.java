package com.dlwrasse.events.helpers;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.tasks.Task;

import java.util.Set;

import androidx.annotation.NonNull;

public class GoogleSignInHelper {
    public static final String TAG = "GoogleSignInHelper";

    public static GoogleSignInClient getSignInClient(@NonNull Context context) {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestScopes(new Scope(Scopes.DRIVE_APPFOLDER))
                .build();
        return GoogleSignIn.getClient(context, gso);
    }

    public static Intent getSignInIntent(@NonNull Context context) {
        return getSignInClient(context).getSignInIntent();
    }

    public static GoogleSignInAccount getAccount(@NonNull Intent data) {
        Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
        try {
            return task.getResult(ApiException.class);
        }catch (ApiException e) {
            Log.e(TAG, e.getMessage());
            return null;
        }
    }

    public static boolean areScopesGranted(@NonNull GoogleSignInAccount account) {
        Set<Scope> scopes = account.getGrantedScopes();
        return scopes.contains(new Scope(Scopes.DRIVE_APPFOLDER));
    }

    public static boolean isAccountValid(GoogleSignInAccount account) {
        return account != null && areScopesGranted(account);
    }
}
