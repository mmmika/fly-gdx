package de.fau.cs.mad.fly.db;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.sql.Database;
import com.badlogic.gdx.sql.DatabaseCursor;
import com.badlogic.gdx.sql.DatabaseFactory;

import de.fau.cs.mad.fly.settings.AppSettingsManager;

/**
 * Manages the database of fly 
 *
 * @ Qufang Fan
 */
public class FlyDBManager {

	private static final String DATABASE_NAME = "faumadfly.db";
	private static final int DATABASE_VERSION = 1;
	private static final int LASTEST_DATABASE_VERSION = 2;

	Database dbHandler;

	public static final String TABLE_PLAYER = "player";
	public static final String PLAYER_COLUMN_ID = "player_id";
	public static final String PLAYER_COLUMN_FLY_ID = "fly_id";
	public static final String PLAYER_COLUMN_NAME = "name";
	public static final String PLAYER_COLUMN_SOCIAL_TYPE = "social_type";
	public static final String PLAYER_COLUMN_SOCIAL_NAME = "social_name";
	public static final String PLAYER_COLUMN_SOCIAL_PASSWORD = "social_password";
	public static final String PLAYER_COLUMN_TOTALSCORE = "total_score";
	public static final String PLAYER_COLUMN_TOTALGELD = "total_geld";

	public static final String TABLE_SCORE = "score";
	public static final String SCORE_COLUMN_PLAYERID = "player_id";
	public static final String SCORE_COLUMN_LEVELID = "level_id";
	public static final String SCORE_COLUMN_LEVELGROUPID = "level_group_id";
	public static final String SCORE_COLUMN_SCORE = "score";
	public static final String SCORE_COLUMN_COMPARESCORE = "compare_score";
	public static final String SCORE_COLUMN_REACHEDDATE = "reached_date";

	public static final String TABLE_SCORE_DETAIL = "score_detail";
	public static final String SCORE_DETAIL_COLUMN_ID = "scoredetail_id";
	public static final String SCORE_DETAIL_COLUMN_PLAYERID = "player_id";
	public static final String SCORE_DETAIL_COLUMN_LEVELID = "level_id";
	public static final String SCORE_DETAIL_COLUMN_LEVELGROUPID = "level_group_id";
	public static final String SCORE_DETAIL_COLUMN_DETAIL = "score_detail";
	public static final String SCORE_DETAIL_COLUMN_VALUE = "_value";

	private static String createTablePlayer = "create table if not exists " + TABLE_PLAYER + "("
			+ PLAYER_COLUMN_ID + " integer primary key autoincrement, " + PLAYER_COLUMN_FLY_ID
			+ " text, " + PLAYER_COLUMN_NAME + " text not null, " + PLAYER_COLUMN_SOCIAL_TYPE
			+ " text, " + PLAYER_COLUMN_SOCIAL_NAME + " text, " + PLAYER_COLUMN_SOCIAL_PASSWORD
			+ " text); ";

	private static String createTableScore = "create table if not exists " + TABLE_SCORE + "("
			+ SCORE_COLUMN_PLAYERID + " integer not null, " + SCORE_COLUMN_LEVELID
			+ " integer not null, " + SCORE_COLUMN_SCORE + " integer not null, "
			+ SCORE_COLUMN_COMPARESCORE + " text, " + SCORE_COLUMN_REACHEDDATE + " date ); ";

	private static String createTableScoreDetail = "create table if not exists "
			+ TABLE_SCORE_DETAIL + "(" + SCORE_DETAIL_COLUMN_ID
			+ " integer primary key autoincrement, " + SCORE_DETAIL_COLUMN_PLAYERID
			+ " integer not null, " + SCORE_DETAIL_COLUMN_LEVELID + " integer not null, "
			+ SCORE_DETAIL_COLUMN_DETAIL + " text not null, " + SCORE_DETAIL_COLUMN_VALUE
			+ " text); ";

	private static String addColLevelGroup1 = "alter table " +  TABLE_SCORE +
			" add column " + SCORE_COLUMN_LEVELGROUPID +
			" integer not null default  1 ";
	private static String addColLevelGoup2 = "alter table " +  TABLE_SCORE_DETAIL +
			" add column " + SCORE_DETAIL_COLUMN_LEVELGROUPID +
			" integer not null default  1 ";
	private static String addColScore = "alter table " +  TABLE_PLAYER +
			" add column " + PLAYER_COLUMN_TOTALSCORE +
			" integer not null default  0";
	private static String addColGeld ="alter table " +  TABLE_PLAYER +
			" add column " + PLAYER_COLUMN_TOTALGELD +
			" integer not null default  0";
	
	
	private FlyDBManager() {
		Gdx.app.log("FlyDBManager", "setupDatabase begin " + System.currentTimeMillis());
		List<String> createSQLs = new ArrayList<String>();
		createSQLs.add(createTablePlayer);
		createSQLs.add(createTableScore);
		createSQLs.add(createTableScoreDetail);
		List<String> upgradeSQLs = null;		
		dbHandler = DatabaseFactory.getNewDatabase(DATABASE_NAME, DATABASE_VERSION, createSQLs,
				upgradeSQLs);
		
		synchronized (dbHandler) {
			dbHandler.setupDatabase();
			Gdx.app.log("FlyDBManager", "setupDatabase end   " + System.currentTimeMillis());
		
			dbHandler.openOrCreateDatabase();
			Gdx.app.log("FlyDBManager", "database opened " + System.currentTimeMillis());
			
			int dbVersion = AppSettingsManager.Instance.getIntegerSetting(AppSettingsManager.DATABASE_VERSION, 0);
			if(dbVersion < 2 )
			{
				dbHandler.execSQL(addColLevelGroup1);
				dbHandler.execSQL(addColLevelGoup2);
				dbHandler.execSQL(addColScore);
				dbHandler.execSQL(addColGeld);
			}
			AppSettingsManager.Instance.setIntegerSetting(AppSettingsManager.DATABASE_VERSION, LASTEST_DATABASE_VERSION);		
		}
		
	}

	private static FlyDBManager Instance = new FlyDBManager();

	public static FlyDBManager getInstance() {
		if( Instance ==null)
			Instance = new FlyDBManager(); 
		return Instance;
	}

	/*
	 * execute one SQL without return value. please don't forget to call the
	 * open and close database methods.
	 */
	public void execSQL(String sql) {
		Gdx.app.log("FlyDBManager.execSQL", "execSQL begin " + System.currentTimeMillis());
		Gdx.app.log("FlyDBManager.execSQL", sql);
		synchronized (dbHandler) {
			dbHandler.execSQL(sql);
		}
		Gdx.app.log("FlyDBManager.execSQL", "execSQL end   " + System.currentTimeMillis());
	}
	
	public void openDatabase() {
		Gdx.app.log("FlyDBManager.openDatabase", "open db begin " + System.currentTimeMillis());
		synchronized (dbHandler) {
			dbHandler.openOrCreateDatabase();
		}
		Gdx.app.log("FlyDBManager.openDatabase", "open db end   " + System.currentTimeMillis());
	}

	/*
	 * execute one select SQL. please don't forget to call the open and close
	 * database methods.
	 */
	public DatabaseCursor selectData(String selectSQL) {
		Gdx.app.log("FlyDBManager.selectData", "selectData  begin " + System.currentTimeMillis());
		DatabaseCursor cursor = null;
		Gdx.app.log("FlyDBManager.selectData", selectSQL);
		synchronized (dbHandler) {
			cursor = dbHandler.rawQuery(selectSQL);
		}
		Gdx.app.log("FlyDBManager.selectData", "selectData  end   " + System.currentTimeMillis());
		return cursor;
	}

	protected void closeDatabase() {
		try {
			if (dbHandler != null) {
				synchronized (dbHandler) {
					dbHandler.closeDatabase();
				}
			}
		} catch (Exception e) {
			Gdx.app.error("FlyDBManager.closeDatabase", e.toString());
		}
		Gdx.app.log("FlyDBManager.closeDatabase", "close db at:" + System.currentTimeMillis());
	}

	public void dispose() {
		closeDatabase();
		Instance=null;

		Gdx.app.log("FlyDBManager", "db is closed and disposed");
	}
}
