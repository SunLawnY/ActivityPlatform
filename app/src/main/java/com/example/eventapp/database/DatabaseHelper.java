package com.example.eventapp.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.text.TextUtils;
import android.util.Log;

import com.example.eventapp.model.Event;
import com.example.eventapp.model.User;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "EventApp.db";
    private static final int DATABASE_VERSION = 2;

    // 表名
    private static final String TABLE_EVENTS = "events";
    private static final String TABLE_REGISTRATIONS = "registrations";
    private static final String TABLE_USERS = "users";

    // Events 表的列名
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_TITLE = "title";
    private static final String COLUMN_DESCRIPTION = "description";
    private static final String COLUMN_LOCATION = "location";
    private static final String COLUMN_START_TIME = "start_time";
    private static final String COLUMN_END_TIME = "end_time";
    private static final String COLUMN_ORGANIZER = "organizer";
    private static final String COLUMN_MAX_PARTICIPANTS = "max_participants";
    private static final String COLUMN_CURRENT_PARTICIPANTS = "current_participants";

    // Users 表的列名
    private static final String COLUMN_USERNAME = "username";
    private static final String COLUMN_PASSWORD = "password";
    private static final String COLUMN_EMAIL = "email";
    private static final String COLUMN_IS_STAFF = "is_staff";

    // Registrations 表的列名
    private static final String COLUMN_EVENT_ID = "event_id";
    private static final String COLUMN_USER_ID = "user_id";
    private static final String COLUMN_REGISTER_TIME = "register_time";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.d("DatabaseHelper", "开始创建数据库表");
        
        // 创建活动表
        String createEventsTable = "CREATE TABLE " + TABLE_EVENTS + "("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COLUMN_TITLE + " TEXT NOT NULL, "
                + COLUMN_DESCRIPTION + " TEXT, "
                + COLUMN_LOCATION + " TEXT, "
                + COLUMN_START_TIME + " INTEGER, "
                + COLUMN_END_TIME + " INTEGER, "
                + COLUMN_ORGANIZER + " TEXT, "
                + COLUMN_MAX_PARTICIPANTS + " INTEGER, "
                + COLUMN_CURRENT_PARTICIPANTS + " INTEGER DEFAULT 0)";
        Log.d("DatabaseHelper", "创建活动表SQL: " + createEventsTable);
        db.execSQL(createEventsTable);

        // 创建用户表
        String createUsersTable = "CREATE TABLE " + TABLE_USERS + "("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COLUMN_USERNAME + " TEXT UNIQUE NOT NULL, "
                + COLUMN_PASSWORD + " TEXT NOT NULL, "
                + COLUMN_EMAIL + " TEXT UNIQUE NOT NULL, "
                + COLUMN_IS_STAFF + " INTEGER DEFAULT 0)";
        Log.d("DatabaseHelper", "创建用户表SQL: " + createUsersTable);
        db.execSQL(createUsersTable);

        // 创建报名表
        String createRegistrationsTable = "CREATE TABLE " + TABLE_REGISTRATIONS + "("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COLUMN_EVENT_ID + " INTEGER, "
                + COLUMN_USER_ID + " INTEGER, "
                + COLUMN_REGISTER_TIME + " INTEGER, "
                + "UNIQUE(" + COLUMN_EVENT_ID + ", " + COLUMN_USER_ID + "), "
                + "FOREIGN KEY(" + COLUMN_EVENT_ID + ") REFERENCES " + TABLE_EVENTS + "(" + COLUMN_ID + "), "
                + "FOREIGN KEY(" + COLUMN_USER_ID + ") REFERENCES " + TABLE_USERS + "(" + COLUMN_ID + "))";
        Log.d("DatabaseHelper", "创建报名表SQL: " + createRegistrationsTable);
        db.execSQL(createRegistrationsTable);
        
        Log.d("DatabaseHelper", "数据库表创建完成");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.d("DatabaseHelper", "数据库升级: " + oldVersion + " -> " + newVersion);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_REGISTRATIONS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_EVENTS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        onCreate(db);
        Log.d("DatabaseHelper", "数据库升级完成");
    }

    // 用户相关方法
    public long addUser(User user) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_USERNAME, user.getUsername());
        values.put(COLUMN_PASSWORD, user.getPassword());
        values.put(COLUMN_EMAIL, user.getEmail());
        values.put(COLUMN_IS_STAFF, user.isStaff() ? 1 : 0);

        long id = db.insert(TABLE_USERS, null, values);
        db.close();
        return id;
    }

    public User getUser(String username) {
        SQLiteDatabase db = this.getReadableDatabase();
        User user = null;

        try {
            Cursor cursor = db.query(TABLE_USERS, null,
                    COLUMN_USERNAME + "=?", new String[]{username},
                    null, null, null);

            if (cursor != null && cursor.moveToFirst()) {
                user = new User();
                user.setId(cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_ID)));
                user.setUsername(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_USERNAME)));
                user.setPassword(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PASSWORD)));
                user.setEmail(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_EMAIL)));
                user.setStaff(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_IS_STAFF)) == 1);
                cursor.close();
            }
        } catch (Exception e) {
            Log.e("DatabaseHelper", "获取用户信息时出错: " + e.getMessage());
        } finally {
            db.close();
        }

        return user;
    }

    public User getUserByEmail(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        User user = null;

        try {
            Cursor cursor = db.query(TABLE_USERS, null,
                    COLUMN_EMAIL + "=?", new String[]{email},
                    null, null, null);

            if (cursor != null && cursor.moveToFirst()) {
                user = new User();
                user.setId(cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_ID)));
                user.setUsername(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_USERNAME)));
                user.setPassword(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PASSWORD)));
                user.setEmail(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_EMAIL)));
                user.setStaff(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_IS_STAFF)) == 1);
                cursor.close();
            }
        } catch (Exception e) {
            Log.e("DatabaseHelper", "通过邮箱获取用户信息时出错: " + e.getMessage());
        } finally {
            db.close();
        }

        return user;
    }

    // 插入新活动
    public long insertEvent(Event event) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_TITLE, event.getTitle());
        values.put(COLUMN_DESCRIPTION, event.getDescription());
        values.put(COLUMN_LOCATION, event.getLocation());
        values.put(COLUMN_START_TIME, event.getStartTime());
        values.put(COLUMN_END_TIME, event.getEndTime());
        values.put(COLUMN_ORGANIZER, event.getOrganizer());
        values.put(COLUMN_MAX_PARTICIPANTS, event.getMaxParticipants());
        values.put(COLUMN_CURRENT_PARTICIPANTS, event.getCurrentParticipants());

        return db.insert(TABLE_EVENTS, null, values);
    }

    // 获取所有活动
    public List<Event> getAllEvents() {
        List<Event> events = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        String[] columns = {
                COLUMN_ID, COLUMN_TITLE, COLUMN_DESCRIPTION, COLUMN_LOCATION,
                COLUMN_START_TIME, COLUMN_END_TIME, COLUMN_ORGANIZER,
                COLUMN_MAX_PARTICIPANTS, COLUMN_CURRENT_PARTICIPANTS
        };

        Cursor cursor = db.query(TABLE_EVENTS, columns, null, null, null, null, COLUMN_START_TIME + " ASC");

        if (cursor != null && cursor.moveToFirst()) {
            do {
                Event event = new Event();
                event.setId(cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_ID)));
                event.setTitle(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TITLE)));
                event.setDescription(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DESCRIPTION)));
                event.setLocation(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_LOCATION)));
                event.setStartTime(cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_START_TIME)));
                event.setEndTime(cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_END_TIME)));
                event.setOrganizer(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ORGANIZER)));
                event.setMaxParticipants(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_MAX_PARTICIPANTS)));
                event.setCurrentParticipants(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_CURRENT_PARTICIPANTS)));
                events.add(event);
            } while (cursor.moveToNext());
            cursor.close();
        }

        return events;
    }

    // 获取单个活动
    public Event getEvent(long id) {
        Log.d("DatabaseHelper", "开始获取活动详情, id=" + id);
        SQLiteDatabase db = this.getReadableDatabase();
        Event event = null;
        Cursor cursor = null;
        
        try {
            // 检查数据库是否打开
            if (!db.isOpen()) {
                Log.e("DatabaseHelper", "数据库未打开");
                return null;
            }

            // 检查参数
            if (id <= 0) {
                Log.e("DatabaseHelper", "无效的活动ID: " + id);
                return null;
            }

            String[] columns = {
                COLUMN_ID,
                COLUMN_TITLE,
                COLUMN_DESCRIPTION,
                COLUMN_LOCATION,
                COLUMN_START_TIME,
                COLUMN_END_TIME,
                COLUMN_ORGANIZER,
                COLUMN_MAX_PARTICIPANTS,
                COLUMN_CURRENT_PARTICIPANTS
            };
            
            Log.d("DatabaseHelper", "执行查询: SELECT * FROM " + TABLE_EVENTS + " WHERE " + COLUMN_ID + "=" + id);
            cursor = db.query(TABLE_EVENTS, columns, COLUMN_ID + "=?",
                    new String[]{String.valueOf(id)}, null, null, null);

            Log.d("DatabaseHelper", "查询结果: cursor为" + (cursor == null ? "null" : "非null") + 
                                  ", 记录数=" + (cursor != null ? cursor.getCount() : 0));

            if (cursor != null && cursor.moveToFirst()) {
                event = new Event();
                try {
                    event.setId(cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_ID)));
                    event.setTitle(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TITLE)));
                    event.setDescription(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DESCRIPTION)));
                    event.setLocation(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_LOCATION)));
                    event.setStartTime(cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_START_TIME)));
                    event.setEndTime(cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_END_TIME)));
                    event.setOrganizer(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ORGANIZER)));
                    event.setMaxParticipants(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_MAX_PARTICIPANTS)));
                    event.setCurrentParticipants(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_CURRENT_PARTICIPANTS)));
                    Log.d("DatabaseHelper", "成功加载活动: " + event.getTitle());
                } catch (Exception e) {
                    Log.e("DatabaseHelper", "读取活动数据时出错: " + e.getMessage(), e);
                    event = null;
                }
            } else {
                Log.e("DatabaseHelper", "未找到ID为 " + id + " 的活动");
            }
        } catch (Exception e) {
            Log.e("DatabaseHelper", "获取活动时发生错误: " + e.getMessage(), e);
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
            if (db != null && db.isOpen()) {
                db.close();
            }
        }
        
        Log.d("DatabaseHelper", "getEvent完成, 返回结果: " + (event != null ? "成功" : "null"));
        return event;
    }

    // 报名活动
    public boolean registerEvent(long eventId, long userId) {
        Log.d("DatabaseHelper", "开始报名处理: eventId=" + eventId + ", userId=" + userId);
        SQLiteDatabase db = null;
        ContentValues values = new ContentValues();
        values.put(COLUMN_EVENT_ID, eventId);
        values.put(COLUMN_USER_ID, userId);
        values.put(COLUMN_REGISTER_TIME, System.currentTimeMillis());
        
        try {
            db = this.getWritableDatabase();
            db.beginTransaction();
            
            // 检查是否已经报名
            String[] columns = {COLUMN_EVENT_ID};
            String selection = COLUMN_EVENT_ID + " = ? AND " + COLUMN_USER_ID + " = ?";
            String[] selectionArgs = {String.valueOf(eventId), String.valueOf(userId)};
            Cursor cursor = db.query(TABLE_REGISTRATIONS, columns, selection, selectionArgs, null, null, null);
            
            if (cursor != null && cursor.getCount() > 0) {
                Log.d("DatabaseHelper", "用户已经报名过该活动");
                cursor.close();
                return false;
            }
            if (cursor != null) {
                cursor.close();
            }

            // 检查活动是否存在且未满
            String[] eventColumns = {
                COLUMN_CURRENT_PARTICIPANTS,
                COLUMN_MAX_PARTICIPANTS
            };
            cursor = db.query(TABLE_EVENTS, eventColumns, COLUMN_ID + "=?",
                    new String[]{String.valueOf(eventId)}, null, null, null);

            if (cursor != null && cursor.moveToFirst()) {
                int currentParticipants = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_CURRENT_PARTICIPANTS));
                int maxParticipants = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_MAX_PARTICIPANTS));
                cursor.close();

                Log.d("DatabaseHelper", "当前参与人数/最大人数: " + currentParticipants + "/" + maxParticipants);

                if (currentParticipants >= maxParticipants) {
                    Log.d("DatabaseHelper", "活动已满");
                    return false;
                }

                // 插入报名记录
                long registrationResult = db.insert(TABLE_REGISTRATIONS, null, values);
                if (registrationResult == -1) {
                    Log.e("DatabaseHelper", "插入报名记录失败");
                    return false;
                }

                // 更新活动参与人数
                ContentValues eventValues = new ContentValues();
                eventValues.put(COLUMN_CURRENT_PARTICIPANTS, currentParticipants + 1);
                int updateResult = db.update(TABLE_EVENTS, eventValues, COLUMN_ID + "=?",
                        new String[]{String.valueOf(eventId)});
                
                if (updateResult > 0) {
                    db.setTransactionSuccessful();
                    Log.d("DatabaseHelper", "报名成功");
                    return true;
                } else {
                    Log.e("DatabaseHelper", "更新活动参与人数失败");
                }
            } else {
                Log.e("DatabaseHelper", "未找到活动信息");
            }
        } catch (Exception e) {
            Log.e("DatabaseHelper", "报名过程中出错: " + e.getMessage());
        } finally {
            if (db != null) {
                if (db.inTransaction()) {
                    db.endTransaction();
                }
                db.close();
            }
        }
        
        return false;
    }

    // 检查用户是否已报名
    public boolean isUserRegistered(long userId, long eventId) {
        Log.d("DatabaseHelper", "检查用户报名状态: userId=" + userId + ", eventId=" + eventId);
        SQLiteDatabase db = this.getReadableDatabase();
        boolean isRegistered = false;
        Cursor cursor = null;
        
        try {
            String[] columns = {COLUMN_ID};
            String selection = COLUMN_EVENT_ID + " = ? AND " + COLUMN_USER_ID + " = ?";
            String[] selectionArgs = {String.valueOf(eventId), String.valueOf(userId)};
            
            cursor = db.query(TABLE_REGISTRATIONS, columns, selection, selectionArgs, null, null, null);
            isRegistered = cursor != null && cursor.getCount() > 0;
            Log.d("DatabaseHelper", "查询结果: cursor非空=" + (cursor != null) + ", 记录数=" + (cursor != null ? cursor.getCount() : 0));
            Log.d("DatabaseHelper", "用户报名状态: " + isRegistered);
        } catch (Exception e) {
            Log.e("DatabaseHelper", "检查用户报名状态时出错: " + e.getMessage());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            db.close();
        }
        
        return isRegistered;
    }

    // 获取用户报名的活动
    public List<Event> getUserRegisteredEvents(long userId) {
        List<Event> events = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        
        try {
            String query = "SELECT e.* FROM " + TABLE_EVENTS + " e"
                    + " INNER JOIN " + TABLE_REGISTRATIONS + " r"
                    + " ON e." + COLUMN_ID + " = r." + COLUMN_EVENT_ID
                    + " WHERE r." + COLUMN_USER_ID + " = ?"
                    + " ORDER BY e." + COLUMN_START_TIME + " ASC";

            Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(userId)});

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    Event event = new Event();
                    event.setId(cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_ID)));
                    event.setTitle(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TITLE)));
                    event.setDescription(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DESCRIPTION)));
                    event.setLocation(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_LOCATION)));
                    event.setStartTime(cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_START_TIME)));
                    event.setEndTime(cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_END_TIME)));
                    event.setOrganizer(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ORGANIZER)));
                    event.setMaxParticipants(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_MAX_PARTICIPANTS)));
                    event.setCurrentParticipants(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_CURRENT_PARTICIPANTS)));
                    events.add(event);
                } while (cursor.moveToNext());
                cursor.close();
            }
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        } finally {
            db.close();
        }
        
        return events;
    }

    // 检查活动表是否有数据
    public boolean hasEvents() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        try {
            cursor = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_EVENTS, null);
            if (cursor != null && cursor.moveToFirst()) {
                int count = cursor.getInt(0);
                Log.d("DatabaseHelper", "Events count: " + count);
                return count > 0;
            }
            return false;
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Error checking events: " + e.getMessage());
            return false;
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
            if (db != null && db.isOpen()) {
                db.close();
            }
        }
    }

    // 取消报名
    public boolean cancelRegistration(long eventId, long userId) {
        Log.d("DatabaseHelper", "开始取消报名: eventId=" + eventId + ", userId=" + userId);
        SQLiteDatabase db = null;
        Cursor cursor = null;
        boolean success = false;
        
        try {
            db = this.getWritableDatabase();
            db.beginTransaction();
            
            // 获取活动信息
            String[] columns = {COLUMN_CURRENT_PARTICIPANTS};
            cursor = db.query(TABLE_EVENTS, columns, COLUMN_ID + "=?",
                    new String[]{String.valueOf(eventId)}, null, null, null);
                
            if (cursor != null && cursor.moveToFirst()) {
                int currentParticipants = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_CURRENT_PARTICIPANTS));
                
                // 删除报名记录
                int deleteResult = db.delete(TABLE_REGISTRATIONS,
                        COLUMN_EVENT_ID + "=? AND " + COLUMN_USER_ID + "=?",
                        new String[]{String.valueOf(eventId), String.valueOf(userId)});
                    
                if (deleteResult > 0) {
                    // 更新活动当前参与人数
                    ContentValues values = new ContentValues();
                    values.put(COLUMN_CURRENT_PARTICIPANTS, Math.max(0, currentParticipants - 1));
                    int updateResult = db.update(TABLE_EVENTS, values, COLUMN_ID + "=?",
                            new String[]{String.valueOf(eventId)});
                        
                    if (updateResult > 0) {
                        db.setTransactionSuccessful();
                        success = true;
                        Log.d("DatabaseHelper", "取消报名成功");
                    } else {
                        Log.e("DatabaseHelper", "更新活动参与人数失败");
                    }
                } else {
                    Log.d("DatabaseHelper", "取消报名失败: 未找到报名记录");
                }
            } else {
                Log.e("DatabaseHelper", "未找到活动信息");
            }
        } catch (Exception e) {
            Log.e("DatabaseHelper", "取消报名时出错: " + e.getMessage());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            if (db != null) {
                if (db.inTransaction()) {
                    db.endTransaction();
                }
                db.close();
            }
        }
        
        return success;
    }

    public boolean updateEvent(Event event) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("title", event.getTitle());
        values.put("description", event.getDescription());
        values.put("location", event.getLocation());
        values.put("start_time", event.getStartTime());
        values.put("end_time", event.getEndTime());
        values.put("max_participants", event.getMaxParticipants());

        try {
            return db.update("events", values, "id = ?", new String[]{String.valueOf(event.getId())}) > 0;
        } catch (Exception e) {
            Log.e("DatabaseHelper", "更新活动失败: " + e.getMessage());
            return false;
        } finally {
            db.close();
        }
    }

    public boolean deleteEvent(long eventId) {
        SQLiteDatabase db = this.getWritableDatabase();
        try {
            db.beginTransaction();
            // 首先删除所有相关的报名记录
            db.delete("registrations", "event_id = ?", new String[]{String.valueOf(eventId)});
            // 然后删除活动
            boolean success = db.delete("events", "id = ?", new String[]{String.valueOf(eventId)}) > 0;
            db.setTransactionSuccessful();
            return success;
        } catch (Exception e) {
            Log.e("DatabaseHelper", "删除活动失败: " + e.getMessage());
            return false;
        } finally {
            try {
                db.endTransaction();
            } catch (Exception e) {
                Log.e("DatabaseHelper", "结束事务失败: " + e.getMessage());
            }
            db.close();
        }
    }

    public List<Event> searchEvents(String keyword, String timeFilter, String statusFilter) {
        List<Event> events = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        
        try {
            StringBuilder queryBuilder = new StringBuilder();
            List<String> args = new ArrayList<>();
            
            // 基础查询
            queryBuilder.append("SELECT * FROM ").append(TABLE_EVENTS).append(" WHERE 1=1");
            
            // 关键词搜索
            if (!TextUtils.isEmpty(keyword)) {
                queryBuilder.append(" AND (")
                        .append(COLUMN_TITLE).append(" LIKE ? OR ")
                        .append(COLUMN_DESCRIPTION).append(" LIKE ? OR ")
                        .append(COLUMN_LOCATION).append(" LIKE ?)");
                String likeArg = "%" + keyword + "%";
                args.add(likeArg);
                args.add(likeArg);
                args.add(likeArg);
            }
            
            // 时间筛选
            long currentTime = System.currentTimeMillis();
            Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis(currentTime);
            
            switch (timeFilter) {
                case "today":
                    // 设置为今天开始时间
                    cal.set(Calendar.HOUR_OF_DAY, 0);
                    cal.set(Calendar.MINUTE, 0);
                    cal.set(Calendar.SECOND, 0);
                    long todayStart = cal.getTimeInMillis();
                    // 设置为今天结束时间
                    cal.set(Calendar.HOUR_OF_DAY, 23);
                    cal.set(Calendar.MINUTE, 59);
                    cal.set(Calendar.SECOND, 59);
                    long todayEnd = cal.getTimeInMillis();
                    queryBuilder.append(" AND ").append(COLUMN_START_TIME).append(" BETWEEN ? AND ?");
                    args.add(String.valueOf(todayStart));
                    args.add(String.valueOf(todayEnd));
                    break;
                    
                case "week":
                    // 设置为本周开始时间
                    cal.set(Calendar.DAY_OF_WEEK, cal.getFirstDayOfWeek());
                    cal.set(Calendar.HOUR_OF_DAY, 0);
                    cal.set(Calendar.MINUTE, 0);
                    cal.set(Calendar.SECOND, 0);
                    long weekStart = cal.getTimeInMillis();
                    // 设置为本周结束时间
                    cal.add(Calendar.DAY_OF_WEEK, 6);
                    cal.set(Calendar.HOUR_OF_DAY, 23);
                    cal.set(Calendar.MINUTE, 59);
                    cal.set(Calendar.SECOND, 59);
                    long weekEnd = cal.getTimeInMillis();
                    queryBuilder.append(" AND ").append(COLUMN_START_TIME).append(" BETWEEN ? AND ?");
                    args.add(String.valueOf(weekStart));
                    args.add(String.valueOf(weekEnd));
                    break;
                    
                case "month":
                    // 设置为本月开始时间
                    cal.set(Calendar.DAY_OF_MONTH, 1);
                    cal.set(Calendar.HOUR_OF_DAY, 0);
                    cal.set(Calendar.MINUTE, 0);
                    cal.set(Calendar.SECOND, 0);
                    long monthStart = cal.getTimeInMillis();
                    // 设置为本月结束时间
                    cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH));
                    cal.set(Calendar.HOUR_OF_DAY, 23);
                    cal.set(Calendar.MINUTE, 59);
                    cal.set(Calendar.SECOND, 59);
                    long monthEnd = cal.getTimeInMillis();
                    queryBuilder.append(" AND ").append(COLUMN_START_TIME).append(" BETWEEN ? AND ?");
                    args.add(String.valueOf(monthStart));
                    args.add(String.valueOf(monthEnd));
                    break;
            }
            
            // 状态筛选
            switch (statusFilter) {
                case "not_started":
                    queryBuilder.append(" AND ").append(COLUMN_START_TIME).append(" > ?");
                    args.add(String.valueOf(currentTime));
                    break;
                case "ongoing":
                    queryBuilder.append(" AND ").append(COLUMN_START_TIME).append(" <= ? AND ")
                            .append(COLUMN_END_TIME).append(" >= ?");
                    args.add(String.valueOf(currentTime));
                    args.add(String.valueOf(currentTime));
                    break;
                case "ended":
                    queryBuilder.append(" AND ").append(COLUMN_END_TIME).append(" < ?");
                    args.add(String.valueOf(currentTime));
                    break;
                case "full":
                    queryBuilder.append(" AND ").append(COLUMN_CURRENT_PARTICIPANTS)
                            .append(" >= ").append(COLUMN_MAX_PARTICIPANTS);
                    break;
            }
            
            // 添加排序
            queryBuilder.append(" ORDER BY ").append(COLUMN_START_TIME).append(" ASC");
            
            // 执行查询
            Cursor cursor = db.rawQuery(queryBuilder.toString(), args.toArray(new String[0]));
            
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    Event event = new Event();
                    event.setId(cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_ID)));
                    event.setTitle(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TITLE)));
                    event.setDescription(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DESCRIPTION)));
                    event.setLocation(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_LOCATION)));
                    event.setStartTime(cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_START_TIME)));
                    event.setEndTime(cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_END_TIME)));
                    event.setOrganizer(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ORGANIZER)));
                    event.setMaxParticipants(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_MAX_PARTICIPANTS)));
                    event.setCurrentParticipants(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_CURRENT_PARTICIPANTS)));
                    events.add(event);
                } while (cursor.moveToNext());
                cursor.close();
            }
        } catch (Exception e) {
            Log.e("DatabaseHelper", "搜索活动时出错: " + e.getMessage());
        } finally {
            db.close();
        }
        
        return events;
    }
} 