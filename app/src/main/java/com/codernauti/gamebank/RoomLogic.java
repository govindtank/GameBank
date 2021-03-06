package com.codernauti.gamebank;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.codernauti.gamebank.bluetooth.BTBundle;
import com.codernauti.gamebank.bluetooth.BTEvent;
import com.codernauti.gamebank.database.Match;
import com.codernauti.gamebank.database.Player;
import com.codernauti.gamebank.database.Transaction;
import com.codernauti.gamebank.util.SharePrefUtil;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;
import java.util.UUID;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmList;
import io.realm.RealmResults;

/**
 * Created by Eduard on 03-Mar-18.
 */

public final class RoomLogic {

    private static final String TAG = "RoomLogic";

    public static final String RECONNECTED_PLAYER_ID = "reconnected_player_id";

    private final LocalBroadcastManager mLocalBroadcastManager;


    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(final Context context, final Intent intent) {
            String action = intent.getAction();

            Log.d(TAG, "Received action: " + action + "\n" +
                    Thread.currentThread().getName());

            BTBundle btBundle = BTBundle.extractFrom(intent);
            if (btBundle != null) {

                if (BTEvent.MEMBER_CONNECTED.equals(action)) {

                    final String newPlayerJson = (String) btBundle.get(String.class.getName());
                    Log.d(TAG, "Player json: \n" + newPlayerJson);

                    Realm.getDefaultInstance().executeTransaction(new Realm.Transaction() {
                        @Override
                        public void execute(Realm realm) {
                            final Player playerFromJson = GameBank.gsonConverter.fromJson(newPlayerJson, Player.class);
                            Log.d(TAG, "Valid: " + playerFromJson.isValid());


                            // new player or update
                            final Player oldPlayer = realm.where(Player.class)
                                    .equalTo("mId", playerFromJson.getPlayerId())
                                    .findFirst();

                            final int currentMatchId = SharePrefUtil.getCurrentMatchId(context);
                            final Match currentMatch = realm
                                    .where(Match.class)
                                    .equalTo("mId", currentMatchId)
                                    .findFirst();

                            if (oldPlayer == null) {
                                // new player
                                final Player player = realm.copyToRealm(playerFromJson);

                                currentMatch.getPlayerList().add(player);

                            } else {
                                // reconnected player
                                realm.copyToRealmOrUpdate(playerFromJson);



                                Intent memberConnected = new Intent(Event.MEMBER_RECONNECTED);
                                memberConnected.putExtra(RECONNECTED_PLAYER_ID,
                                        playerFromJson.getPlayerId());
                                mLocalBroadcastManager.sendBroadcast(memberConnected);
                            }

                        }
                    });

                } else if (Event.Game.MEMBER_READY.equals(action)) {

                    String uuid = btBundle.getUuid().toString();
                    final boolean isReady = (boolean) btBundle.get(Boolean.class.getName());
                    Log.d(TAG, "Player " + uuid + " is ready? " + isReady);

                    final Player player = Realm.getDefaultInstance()
                            .where(Player.class)
                            .equalTo("mId", uuid)
                            .findFirst();

                    Realm.getDefaultInstance().executeTransaction(new Realm.Transaction() {
                        @Override
                        public void execute(Realm realm) {
                            player.setReady(isReady);
                        }
                    });

                } else if (BTEvent.MEMBER_DISCONNECTED.equals(action)) {

                    final String playerDisconnected = btBundle.get(UUID.class.getName()).toString();
                    Log.d(TAG, "Player to remove: " + playerDisconnected);

                    final Player player = Realm.getDefaultInstance()
                            .where(Player.class)
                            .equalTo("mId", playerDisconnected)
                            .findFirst();

                    // TODO: remove player from match?

                    if (player != null) {
                        Realm.getDefaultInstance().executeTransaction(new Realm.Transaction() {
                            @Override
                            public void execute(Realm realm) {
                                player.deleteFromRealm();
                            }
                        });
                    }
                }

            }
        }
    };


    RoomLogic(LocalBroadcastManager broadcastManager) {
        Log.d(TAG, "Create RoomLogic");
        mLocalBroadcastManager = broadcastManager;

        registerReceiver();
    }

    private void registerReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(BTEvent.MEMBER_CONNECTED);
        filter.addAction(Event.Game.MEMBER_READY);
        filter.addAction(BTEvent.MEMBER_DISCONNECTED);

        mLocalBroadcastManager.registerReceiver(mReceiver, filter);
    }

}
