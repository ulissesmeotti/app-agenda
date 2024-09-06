
package com.example.agendaservicos.database;
import androidx.room.Room;
import android.content.Context;

public class DatabaseClient {
    private static DatabaseClient instance;
    private final Banco database;

    private DatabaseClient(Context context) {
        database = Room.databaseBuilder(context.getApplicationContext(), Banco.class, "banco_agendamento").build();
    }

    public static synchronized DatabaseClient getInstance(Context context) {
        if (instance == null) {
            instance = new DatabaseClient(context);
        }
        return instance;
    }

    public Banco getDatabase() {
        return database;
    }

}
