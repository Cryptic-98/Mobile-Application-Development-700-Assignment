package com.example.smartpantrymanager.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper
{
    public static final String DATABASE_NAME = "smart_pantry.db";
    public static final int DATABASE_VERSION = 1;
    private final Context context;

    public DatabaseHelper(Context context)
    {
        super(requireContext(context), DATABASE_NAME, null, DATABASE_VERSION);
        this.context = requireContext(context);
    }

    private static Context requireContext(Context context)
    {
        if (context == null)
        {
            throw new IllegalArgumentException("context must not be null.");
        }
        return context.getApplicationContext();
    }

    @Override
    public void onConfigure(SQLiteDatabase db)
    {
        super.onConfigure(db);
        db.setForeignKeyConstraintsEnabled(true);
    }

    @Override
    public void onCreate(SQLiteDatabase db)
    {
        for (String statement : readStatements())
        {
            String control = statement.replaceAll("(?s)/\\*.*?\\*/", "")
                    .replaceAll("(?m)--.*$", "").trim();
            if (control.isEmpty() || control.matches("(?is)PRAGMA\\b.*")
                    || control.matches("(?is)BEGIN(?:\\s+TRANSACTION)?")
                    || control.matches("(?is)COMMIT"))
            {
                continue;
            }
            db.execSQL(statement);
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
    {
    }

    private List<String> readStatements()
    {
        try (InputStream input = context.getAssets().open("DATABASE.sql");
             ByteArrayOutputStream output = new ByteArrayOutputStream())
        {
            byte[] buffer = new byte[4096];
            int read;
            while ((read = input.read(buffer)) != -1)
            {
                output.write(buffer, 0, read);
            }
            return splitStatements(new String(output.toByteArray(), StandardCharsets.UTF_8));
        }
        catch (IOException exception)
        {
            throw new IllegalStateException("Unable to read the database schema.", exception);
        }
    }

    static List<String> splitStatements(String script)
    {
        List<String> statements = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        char quote = 0;
        boolean lineComment = false;
        boolean blockComment = false;
        for (int index = 0; index < script.length(); index++)
        {
            char character = script.charAt(index);
            char next = index + 1 < script.length() ? script.charAt(index + 1) : 0;
            current.append(character);
            if (lineComment)
            {
                if (character == '\n')
                {
                    lineComment = false;
                }
                continue;
            }
            if (blockComment)
            {
                if (character == '*' && next == '/')
                {
                    current.append(next);
                    index++;
                    blockComment = false;
                }
                continue;
            }
            if (quote != 0)
            {
                if (character == quote)
                {
                    if (next == quote)
                    {
                        current.append(next);
                        index++;
                    }
                    else
                    {
                        quote = 0;
                    }
                }
                else if (quote == ']' && character == ']')
                {
                    quote = 0;
                }
                continue;
            }
            if (character == '-' && next == '-')
            {
                current.append(next);
                index++;
                lineComment = true;
            }
            else if (character == '/' && next == '*')
            {
                current.append(next);
                index++;
                blockComment = true;
            }
            else if (character == '\'' || character == '"' || character == '`')
            {
                quote = character;
            }
            else if (character == '[')
            {
                quote = ']';
            }
            else if (character == ';')
            {
                String statement = current.substring(0, current.length() - 1).trim();
                if (!statement.isEmpty())
                {
                    statements.add(statement);
                }
                current.setLength(0);
            }
        }
        String remainder = current.toString().trim();
        if (quote != 0 || blockComment)
        {
            throw new IllegalArgumentException("The database schema contains an unterminated quoted value or comment.");
        }
        if (!remainder.isEmpty())
        {
            statements.add(remainder);
        }
        return statements;
    }
}