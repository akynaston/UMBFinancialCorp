package com.trivir.idm.driver.ace;

import java.io.UnsupportedEncodingException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import com.google.gson.reflect.TypeToken;
import com.novell.nds.dirxml.driver.Trace;
import com.trivir.ace.api.AceApi;

class DriverObjectCache {
	private static final String DB_BASE_NAME = "rsaCache";
	private static final String DB_URL_TEMPLATE = "jdbc:hsqldb:file:%s-%s";
	private static final String TABLE_VERSION = "version";
	private static final String COLUMN_VERSION_MAJOR = "ver_major";
	private static final String COLUMN_VERSION_MINOR = "ver_minor";
//	private static final String TABLE_STATE = "state";
//	private static final String COLUMN_NAME = "name";
//	private static final String COLUMN_VALUE = "value";
	private static final String TABLE_USER = "user";
	private static final String COLUMN_ID = "id";
	private static final String COLUMN_USERNUM = "usernum";
	private static final String COLUMN_USERNAME = "defaultLogin";
//	private static final String COLUMN_FULLNAME = "fullname";
	private static final String COLUMN_DATA = "data";
	private static final String TABLE_TOKEN = "token";
	private static final String COLUMN_SERIAL = "serial";
	private static final int MAJOR_VERSION = 1;
	private static final int MINOR_VERSION = 0;
//	private static final String STATE_UNPROCESSED_USERS = "unprocessedusers";

	private Connection cacheConnection;
	private String cacheName;
	private Trace trace;
	private boolean newDatabase = false;
	

	private DriverObjectCache(String cacheName, Trace trace) {
		this.cacheName = cacheName;
		this.trace = trace;
	}

	static DriverObjectCache getInstance(String cacheName, Trace trace) throws DriverObjectCacheException {
		DriverObjectCache driverObjectCache = new DriverObjectCache(cacheName, trace);
		driverObjectCache.init();

		return driverObjectCache;
	}

	private void init() throws DriverObjectCacheException {     
		trace.trace("Starting cache");

		try {
			Class.forName("org.hsqldb.jdbcDriver");
		} catch (ClassNotFoundException e) {
            throw new DriverObjectCacheException("Error loading cache driver. Make sure required jars are correctly installed. Unable to create application cache.", e);
		}

		try {
			cacheConnection = DriverManager.getConnection(String.format(DB_URL_TEMPLATE, DB_BASE_NAME, cacheName) , "sa", "");
		} catch (SQLException e) {
            throw new DriverObjectCacheException("Error initializing cache. Cache not initialized.", e);
		}

		try {
			if(dbTableExists(TABLE_VERSION) == false) {
				newDatabase = true;
				createVersionTable();
			}

			validateSchemaVersion();
		} catch (DriverObjectCacheException e) {
			close();
			throw e;
		}

		trace.trace("Cache started");
	}
	
    void close() {
    	if(cacheConnection != null) {
	    	try {
				Statement st = cacheConnection.createStatement();
				String sql = "SHUTDOWN COMPACT";
				st.execute(sql);

				cacheConnection.close();
			} catch (SQLException e) {
				// Intentionally left blank.
			}
    	}
    }
    
    private Map<String, Object> parseCacheJson(String json) {
    	JsonParser p = new JsonParser();
        JsonObject e = p.parse(json).getAsJsonObject();
        Map<String, Object> data = new HashMap<String, Object>();
        for (Map.Entry<String, JsonElement> s : e.entrySet()) {
        	if (s.getValue().isJsonPrimitive()) {
	            JsonPrimitive val = s.getValue().getAsJsonPrimitive();
	            if (val.isNumber()) {
	                if (val.getAsInt() == val.getAsDouble()) data.put(s.getKey(), val.getAsInt());
	                else data.put(s.getKey(), val.getAsDouble());
	            }
	            else if (val.isBoolean()) data.put(s.getKey(), val.getAsBoolean());
	            else if (val.isString()) data.put(s.getKey(), val.getAsString());
        	}
        	else if (s.getValue().isJsonArray()) {
        		List<Object> item = new ArrayList<Object>();
        		for (JsonElement arrayVal : s.getValue().getAsJsonArray()) item.add(arrayVal.getAsString());
        		data.put(s.getKey(), item);
        	}
        }
        return data;
    }

    boolean isNewDatabase() {
    	return newDatabase;
    }

	void addCachedUser(Map<String,Object> attrs) throws DriverObjectCacheException {
		PreparedStatement st = null;
		try {
			st = cacheConnection.prepareStatement("insert into " + TABLE_USER + " (" + COLUMN_USERNUM + "," + COLUMN_USERNAME + "," + COLUMN_DATA + ") values(?,?,?)");
			st.setString(1, (String)attrs.get(AceApi.ATTR_USER_NUM));
			st.setString(2, (String)attrs.get(AceApi.ATTR_DEFAULT_LOGIN));
			st.setString(3, new Gson().toJson(attrs));
			st.executeUpdate();
		} catch (SQLException e) {
			String defaultLogin = (String)attrs.get(AceApi.ATTR_DEFAULT_LOGIN);
			String guid = (String)attrs.get(AceApi.ATTR_USER_NUM);
			throw new DriverObjectCacheException("Error updating cache with info for user. |" + guid + "|" + defaultLogin + "|", e);
		} finally {
			close(st);
		}
	}

	Set<String> getAllDefaultLogins() throws DriverObjectCacheException {
		Set<String> defaultLogins = new HashSet<String>();

		Statement st = null;
		try {
			st = cacheConnection.createStatement();
			ResultSet rs = st.executeQuery("select " + COLUMN_USERNAME + " from " + TABLE_USER);
				
			while(rs.next()) {
				defaultLogins.add(rs.getString(1));
			}
		} catch (SQLException e) {
			throw new DriverObjectCacheException("Error retrieving all users", e);
		}
	
		return defaultLogins;
	}

	Set<String> getAllGUIDs() throws DriverObjectCacheException {
		Set<String> defaultLogins = new HashSet<String>();

		Statement st = null;
		try {
			st = cacheConnection.createStatement();
			ResultSet rs = st.executeQuery("select " + COLUMN_USERNUM + " from " + TABLE_USER);
				
			while(rs.next()) {
				defaultLogins.add(rs.getString(1));
			}
		} catch (SQLException e) {
			throw new DriverObjectCacheException("Error retrieving all users", e);
		}
	
		return defaultLogins;
	}
	
	Map<String,Object> getCachedUserAttrsByDefaultLogin(String defaultLogin) throws DriverObjectCacheException {
		PreparedStatement st = null;
		String json;
		try {
			st = cacheConnection.prepareStatement("select " + COLUMN_DATA + " from " + TABLE_USER + " where " + COLUMN_USERNAME + "=?");
			st.setString(1, defaultLogin);
			ResultSet rs = st.executeQuery();
			if (rs.next() == false) {
				return null;
			}
			json = rs.getString(COLUMN_DATA);
		} catch (SQLException e) {
			throw new DriverObjectCacheException("Error retrieving cached info for user.", e);
		} finally {
			close(st);
		}

		Map<String,Object> attrs = parseCacheJson(json);
		return attrs;
	}

	Map<String,Object> getCachedUserAttrsByGuid(String guid) throws DriverObjectCacheException {
		PreparedStatement st = null;
		String json;
		try {
			st = cacheConnection.prepareStatement("select " + COLUMN_DATA + " from " + TABLE_USER + " where " + COLUMN_USERNUM + "=?");
			st.setString(1, guid);
			ResultSet rs = st.executeQuery();
			if (rs.next() == false) {
				return null;
			}
			json = rs.getString(COLUMN_DATA);
		} catch (SQLException e) {
			throw new DriverObjectCacheException("Error retrieving cached info for user.", e);
		} finally {
			close(st);
		}

		Map<String,Object> attrs = parseCacheJson(json);
		return attrs;
	}

	void removeCachedUser(final String guid) throws DriverObjectCacheException {
		PreparedStatement st = null;
		try {
			st = cacheConnection.prepareStatement("delete from " + TABLE_USER + " where " + COLUMN_USERNUM + "=?");
			st.setString(1, guid);
			st.executeUpdate();
		} catch (SQLException e) {
			close(st);
			throw new DriverObjectCacheException("Error removing user from cache.", e);
		}
	}

	void updateCachedUserAtrrs(Map<String,Object> attrs) throws DriverObjectCacheException {
		Gson gson = new Gson();

		String id;
		PreparedStatement st = null;
		try {
			st = cacheConnection.prepareStatement("select * from " + TABLE_USER + " where " + COLUMN_USERNUM + "=?");
			st.setString(1, (String)attrs.get(AceApi.ATTR_USER_NUM));
			ResultSet rs = st.executeQuery();
			if (rs.next() == false) {
				// This means the GUID on the user has changed.
				// This happens when a user is switch to or from
				// being a "registered" user. It would be better
				// to catch this as an event from monitorHistory.
				// For now, we will try looking up the user by
				// their username.
				st.close();
				st = cacheConnection.prepareStatement("select * from " + TABLE_USER + " where " + COLUMN_USERNAME + "=?");
				st.setString(1, (String)attrs.get(AceApi.ATTR_DEFAULT_LOGIN));
				rs = st.executeQuery();
				if (rs.next() == false) {
					throw new DriverObjectCacheException("Unable to find user record to update (" + attrs.get(AceApi.ATTR_DEFAULT_LOGIN) + ":" + attrs.get(AceApi.ATTR_USER_NUM) +")");
				}
				
				if (rs.getString(COLUMN_USERNAME).equals(attrs.get(AceApi.ATTR_DEFAULT_LOGIN)) == false) {
					trace.trace("Warning: " + AceApi.ATTR_DEFAULT_LOGIN + " changed in addition to the GUID");
				}
			}
			id = rs.getString(COLUMN_ID);
		} catch (SQLException e) {
			throw new DriverObjectCacheException("Error looking up user to update in cache.", e);
		} finally {
			close(st);
		}

		try {
			st = cacheConnection.prepareStatement("update " + TABLE_USER + " set " + COLUMN_USERNUM + "=?," + COLUMN_USERNAME + "=?," + COLUMN_DATA + "=? where " + COLUMN_ID + "=?");
			st.setString(1, (String)attrs.get(AceApi.ATTR_USER_NUM));
			st.setString(2, (String)attrs.get(AceApi.ATTR_DEFAULT_LOGIN));
			st.setString(3, gson.toJson(attrs));
			st.setString(4, id);
			st.executeUpdate();
		} catch (SQLException e) {
			throw new DriverObjectCacheException("Error updating user in cache.", e);
		} finally {
			close(st);
		}
	}

	void addCachedToken(String serialNumber, Map<String,Object> attrs) throws DriverObjectCacheException {
		PreparedStatement st = null;
		try {
			st = cacheConnection.prepareStatement("insert into " + TABLE_TOKEN + " (" + COLUMN_SERIAL + "," + COLUMN_DATA + ") values (?,?)");
			st.setString(1, serialNumber);
			st.setString(2, new Gson().toJson(attrs));
			st.executeUpdate();
		} catch (SQLException e) {
			close(st);
			throw new DriverObjectCacheException("Error adding token info to cache.", e);
		}
	}

	Set<String> getAllTokenSerialNumbers() throws DriverObjectCacheException {
		Set<String> serials = new HashSet<String>();

		Statement st = null;
		try {
			st = cacheConnection.createStatement();
			ResultSet rs = st.executeQuery("select " + COLUMN_SERIAL + " from " + TABLE_TOKEN);
				
			while(rs.next()) {
				serials.add(rs.getString(1));
			}
		} catch (SQLException e) {
			throw new DriverObjectCacheException("Error retrieving all users", e);
		}
	
		return serials;
	}

    Map<String,Object> getCachedTokenAttrs(String tokenSerial) throws DriverObjectCacheException {
		PreparedStatement st = null;
		String json;
		try {
			st = cacheConnection.prepareStatement("select " + COLUMN_DATA + " from " + TABLE_TOKEN + " where " + COLUMN_SERIAL + "=?");
			st.setString(1, tokenSerial);
			ResultSet rs = st.executeQuery();
			if (rs.next()) {
				json = rs.getString(COLUMN_DATA);
			} else {
				return null;
			}
		} catch (SQLException e) {
			throw new DriverObjectCacheException("Error retrieving cached token info.", e);
		} finally {
			close(st);
		}

		Map<String,Object> attrs = parseCacheJson(json);
		return attrs;
    }

	void removeCachedToken(String tokenSerial) throws DriverObjectCacheException {
		PreparedStatement st = null;
		try {
			st = cacheConnection.prepareStatement("delete from " + TABLE_TOKEN + " where " + COLUMN_SERIAL + "=?");
			st.setString(1, tokenSerial);
			st.executeUpdate();
		} catch (SQLException e) {
			close(st);
			throw new DriverObjectCacheException("Error removing user from cache.", e);
		}
	}

	void updateCachedTokenAttrs(String serialNumber, Map<String,Object> attrs) throws DriverObjectCacheException {
		PreparedStatement st = null;
//		boolean tokenExists = false;
//		try {
//			st = cacheConnection.prepareStatement("select count(*) from " + TABLE_TOKEN + " where " + COLUMN_SERIAL + "=?");
//			st.setString(1, serialNumber);
//			ResultSet rs = st.executeQuery();
//			if (rs.next()) {
//				tokenExists = true;
//			}
//		} catch (SQLException e) {
//			close(st);
//			throw new DriverObjectCacheException("Error checking for token existance.", e);
//		}
		
		try {
			st = cacheConnection.prepareStatement("update " + TABLE_TOKEN + " set " + COLUMN_DATA + "=? where " + COLUMN_SERIAL + "=?");
			st.setString(1, new Gson().toJson(attrs));
			st.setString(2, serialNumber);
			st.executeUpdate();
		} catch (SQLException e) {
			close(st);
			throw new DriverObjectCacheException("Error updating token info in cache.", e);
		}
	}

    private void validateSchemaVersion() throws DriverObjectCacheException {
    	PreparedStatement stmt = null;
		try {
			stmt = cacheConnection.prepareStatement("select "+COLUMN_VERSION_MAJOR+", "+COLUMN_VERSION_MINOR+" from " + TABLE_VERSION);
			ResultSet rs = stmt.executeQuery();
	
			// We only care about the first entry. There should only be one.
			if (rs.next() == false) {
				throw new DriverObjectCacheException("No values in " + TABLE_VERSION + " table.");
			}

			int actualMajor = rs.getInt(COLUMN_VERSION_MAJOR);
			int actualMinor = rs.getInt(COLUMN_VERSION_MINOR);

			stmt.close();
	
			if (actualMajor == 1 && actualMinor == 0) {
				upgrade10Db();
			} else if (actualMajor != MAJOR_VERSION || actualMinor != MINOR_VERSION ) {
	            throw new DriverObjectCacheException(String.format("Unexpected schema version major(%d) minor(%d). Error reading cache.", actualMajor, actualMinor));
			}		
		} catch (SQLException e) {
			close(stmt);
	        throw new DriverObjectCacheException("Error retrieving schema version. Error reading cache ", e);
		}
	}

    private boolean dbTableExists(String tableName) {
		Statement st = null;
		try {
			st = cacheConnection.createStatement();
			st.execute(String.format("select count(*) from %s", tableName));
			//Select COUNT(*) From INFORMATION_SCHEMA.SYSTEM_TABLES Where TABLE_NAME='VERSION';
		} catch (Exception e) {
			return false;
		} finally {
			close(st);
		}
	
		return true;
	}
	
	private void createVersionTable() throws DriverObjectCacheException {
		Statement st = null;
		try {
			st = cacheConnection.createStatement();
			st.executeUpdate(String.format("create table %s(%s integer, %s integer, constraint uq_version unique(%s,%s))", TABLE_VERSION, COLUMN_VERSION_MAJOR, COLUMN_VERSION_MINOR, COLUMN_VERSION_MAJOR, COLUMN_VERSION_MINOR));

			st.executeUpdate(String.format("insert into %s(%s, %s) values(%d,%d)", TABLE_VERSION, COLUMN_VERSION_MAJOR, COLUMN_VERSION_MINOR, MAJOR_VERSION, MINOR_VERSION));
		} catch (SQLException e) {
			throw new DriverObjectCacheException("Error creating cache version.", e);
		} finally {
			close(st);
		}
	}

//	private void createStateTable() throws DriverObjectCacheException {
//		PreparedStatement st = null;
//		try {
//			st = cacheConnection.prepareStatement("create table " + TABLE_STATE + "(" + COLUMN_NAME + " varchar(64), " + COLUMN_VALUE + " longvarchar, UNIQUE(" + COLUMN_NAME + "))");			
//			st.execute();
//		} catch (SQLException e) {
//			throw new DriverObjectCacheException("Error creating '" + TABLE_STATE + "' table.", e);
//		} finally {
//			close(st);
//		}
//	}
//	
	private void createUserTable() throws DriverObjectCacheException {
		PreparedStatement st = null;
		try {
			st = cacheConnection.prepareStatement("create table " + TABLE_USER + " (" + 
					COLUMN_ID + " INTEGER IDENTITY, " +
					COLUMN_USERNUM + " varchar(64), " +
					COLUMN_USERNAME + " varchar(64), " +
					COLUMN_DATA + " LONGVARCHAR)");
			st.execute();
			st.close();
			st = cacheConnection.prepareStatement("create unique index idx_uq_"+TABLE_USER+"_usernum on " + TABLE_USER + " (" + COLUMN_USERNUM + ")");
			st.execute();
			st.close();
			st = cacheConnection.prepareStatement("create unique index idx_uq_"+TABLE_USER+"_defaultlogin on " + TABLE_USER + " (" + COLUMN_USERNAME + ")");
			st.execute();
		} catch (SQLException e) {
			throw new DriverObjectCacheException("Error creating '" + TABLE_USER + "' table.", e);
		} finally {
			close(st);
		}
	}

	private void createTokenTable() throws DriverObjectCacheException {
		PreparedStatement st = null;
		try {
			st = cacheConnection.prepareStatement("create table " + TABLE_TOKEN + " (" + COLUMN_SERIAL + " varchar(32), " + COLUMN_DATA + " LONGVARCHAR)");
			st.execute();
		} catch (SQLException e) {
			throw new DriverObjectCacheException("Error creating '" + TABLE_USER + "' table.", e);
		} finally {
			close(st);
		}
	}
	
	private void dropTable(String tableName) throws SQLException {
		Statement st = cacheConnection.createStatement();
		st.executeUpdate("drop table " + tableName);
		st.close();
	}

	private void upgrade10Db() throws DriverObjectCacheException {
		Gson gson = new Gson();

//		if (dbTableExists(TABLE_STATE) == false) {
//			createStateTable();
//		}
//		
		if (dbTableExists(TABLE_USER) == false) {
			createUserTable();
		}

		if (dbTableExists(TABLE_TOKEN) == false) {
			createTokenTable();
		}

        try {
        	String TABLE_USER_FILTER = "userfilter";
        	if(dbTableExists(TABLE_USER_FILTER)) {
        		dropTable(TABLE_USER_FILTER);
        	}

        	String TABLE_USER_EXTENSIONS = "userextensions";
        	if(dbTableExists(TABLE_USER_EXTENSIONS)) {
//                ArrayList<String> cachedUserExtensions = new ArrayList<String>();
//
//        		PreparedStatement stmt = null;
//        		ResultSet rs = null;
//        		try {
//	        		stmt = cacheConnection.prepareStatement("select extensionelement from " + TABLE_USER_EXTENSIONS);
//	        		rs = stmt.executeQuery();
//	        		while(rs.next()) {
//	        			cachedUserExtensions.add(rs.getString("extensionelement"));
//	        		}
//        		} finally {
//        			if(stmt != null) stmt.close();
//        			if(rs != null) rs.close();
//        		}
//        		
//        		setStateList("userextensions", cachedUserExtensions);
        		
        		dropTable(TABLE_USER_EXTENSIONS);
            }

        	String TABLE_TOKEN_FILTER = "tokenfilter";
        	if(dbTableExists(TABLE_TOKEN_FILTER)) {
        		dropTable(TABLE_TOKEN_FILTER);
            }

        	String TABLE_TOKEN_EXTENSIONS = "tokenextensions";
        	if (dbTableExists(TABLE_TOKEN_EXTENSIONS)) {
//                ArrayList<String> cachedTokenExtensions = new ArrayList<String>();
//        		
//        		Statement query = null;
//        		ResultSet rs = null;
//        		try {
//	    			query = cacheConnection.createStatement();
//	        		rs = query.executeQuery("select extensionelement from " + TABLE_TOKEN_EXTENSIONS);
//	        		while(rs.next()) {
//	        			cachedTokenExtensions.add(rs.getString("extensionelement"));
//	        		}
//        		} finally {
//        			if(rs != null) rs.close();
//        			if(query != null) query.close();
//        		}
//
//        		setStateList("tokenextensions", cachedTokenExtensions);
				
        		dropTable(TABLE_TOKEN_EXTENSIONS);
            }

        	String TABLE_UNPROCESSED_USERS = "unprocessedusers";
        	if(dbTableExists(TABLE_UNPROCESSED_USERS)) {
//                ArrayList<String> cachedUnprocessedUsers = new ArrayList<String>();
//        		
//        		Statement query = null;
//        		ResultSet rs = null;
//        		try {
//	    			query = cacheConnection.createStatement();
//	        		rs = query.executeQuery("select defaultlogin from " + TABLE_UNPROCESSED_USERS);
//	        		while(rs.next()) {
//	        			cachedUnprocessedUsers.add(rs.getString("defaultlogin"));
//	        		}
//        		} finally {
//        			if(rs != null) rs.close();
//        			if(query != null) query.close();
//        		}
//
//        		setStateList(STATE_UNPROCESSED_USERS, cachedUnprocessedUsers);
        		
        		dropTable(TABLE_UNPROCESSED_USERS);
            }

        	String OLD_TABLE_USERS = "users";
        	if(dbTableExists(OLD_TABLE_USERS)) {    			
        		PreparedStatement query = null;
        		ResultSet rs = null;
    			PreparedStatement insert = null;
    			PreparedStatement delete = null;
        		try {
					cacheConnection.setAutoCommit(false);
	    			query = cacheConnection.prepareStatement("select usernum, defaultlogin, fullname, userdata from " + OLD_TABLE_USERS);
	        		rs = query.executeQuery();
	        		
	    			insert = cacheConnection.prepareStatement("insert into " + TABLE_USER + "(" + COLUMN_USERNUM + "," + COLUMN_USERNAME + "," + COLUMN_DATA + ") values(?,?,?)");
	    			delete = cacheConnection.prepareStatement("delete from " + OLD_TABLE_USERS + " where " + COLUMN_USERNUM + "=?");
	        		while(rs.next()) {
	        			String usernum = rs.getString("usernum");
						insert.setString(1, usernum);
						insert.setString(2, rs.getString("defaultlogin"));
	        			String json = new String(rs.getBytes("userdata"), "UTF-8");
	        			// Unfortunately, Jettison has a bug that causes the ATTR_MEMBER_OF and
	        			// ATTR_TOKEN_SERIAL_NUMBER to be serialized as a string rather than as
	        			// a list of strings. This code deserializes these attributes back into
	        			// lists and then re-serailizes all of the attributes.
	        			Map<String,Object> attrs = parseCacheJson(json);;
	        			for (String key : Arrays.asList(new String[] {AceApi.ATTR_MEMBER_OF, AceApi.ATTR_TOKEN_SERIAL_NUMBER})) {
	        				Object value = attrs.get(key);
        					if (value instanceof String) {
        						String s = (String)value;
        						s = s.replaceAll("\\[\\s*", "\\[\"");
        						s = s.replaceAll("\\s*\\]", "\"\\]");
        						s = s.replaceAll("\\s*,\\s*", "\",\"");
        						attrs.put(key, gson.fromJson(s, new TypeToken<List<String>>(){}.getType()));
        					}
	        			}

						insert.setString(3, gson.toJson(attrs));
						insert.execute();
						
						delete.setString(1, usernum);
						delete.executeUpdate();
						cacheConnection.commit();
	        		}
        		} finally {
        			if (query != null) query.close();
        			if (rs != null) rs.close();
        			if (insert != null) insert.close();
        			if (delete != null) delete.close();
        			cacheConnection.setAutoCommit(true);
        		}

        		dropTable(OLD_TABLE_USERS);
            }

        	String TABLE_UNPROCESSED_TOKENS = "unprocessedtokens";
        	if(dbTableExists(TABLE_UNPROCESSED_TOKENS)) {
//                ArrayList<String> cachedUnprocessedTokens = new ArrayList<String>();
//        		
//        		PreparedStatement stmt = null;
//        		ResultSet rs = null;
//        		try {
//	    			stmt = cacheConnection.prepareStatement("select tokenserial from " + TABLE_UNPROCESSED_TOKENS);
//	        		rs = stmt.executeQuery();
//	        		while(rs.next()) {
//	        			cachedUnprocessedTokens.add(rs.getString("tokenserial"));
//	        		}
//        		} finally {
//        			if(stmt != null) stmt.close();
//        			if(rs != null) rs.close();
//        		}
//
//        		setStateList("unprocessedtokens", cachedUnprocessedTokens);
        		
        		dropTable(TABLE_UNPROCESSED_TOKENS);
            }

        	String OLD_TABLE_TOKENS = "tokens";
        	if(dbTableExists(OLD_TABLE_TOKENS)) {
        		PreparedStatement query = null;
        		ResultSet rs = null;
        		PreparedStatement insert = null;
        		PreparedStatement delete = null;

				try {
					cacheConnection.setAutoCommit(false);
					query = cacheConnection.prepareStatement("select tokenserial, tokendata from " + OLD_TABLE_TOKENS);
					rs = query.executeQuery();
    	
	    			insert = cacheConnection.prepareStatement("insert into " + TABLE_TOKEN + "(" + COLUMN_SERIAL + "," + COLUMN_DATA + ") values(?,?)");
	    			delete = cacheConnection.prepareStatement("delete from " + OLD_TABLE_TOKENS + " where tokenserial=?");
	    			
	    			while(rs.next()) {
	        			String serial = rs.getString("tokenserial");
						insert.setString(1, serial);
						insert.setString(2, new String(rs.getBytes("tokendata"), "UTF-8"));
						insert.execute();
    						
						delete.setString(1, serial);
						delete.executeUpdate();
						cacheConnection.commit();
	        		}
    		    } catch (SQLException e) {
		            try {
		            	cacheConnection.rollback();
		            } catch(SQLException e2) {
		                throw e;
		            }
        		} finally {
        			if(query != null) query.close();
        			if(rs != null) rs.close();
        			if(insert != null) insert.close();
        			if(delete != null) delete.close();
        			cacheConnection.setAutoCommit(true);
    		    }

        		dropTable(OLD_TABLE_TOKENS);
            }
        	
        	Statement st = cacheConnection.createStatement();
        	st.executeUpdate("Update "+TABLE_VERSION+" set "+COLUMN_VERSION_MAJOR+"="+MAJOR_VERSION+", "+COLUMN_VERSION_MINOR+"="+MINOR_VERSION+" where "+COLUMN_VERSION_MAJOR+"=1 AND "+COLUMN_VERSION_MINOR+"=0");
        	close(st);
        } catch (SQLException e) {
//			log("Error reading cache from database. Cache may not be fully loaded '" + e + "'");
        } catch (UnsupportedEncodingException e) {
//			log("Unexpected UnsupportedEncodingException. Error reading cache from database. Cache may not be fully loaded '" + e + "'");
        } finally {
//	        con.setAutoCommit(true);
        }
	}

//	private String getState(String key) throws DriverObjectCacheException {	
//		PreparedStatement stmt = null;
//		try {
//			stmt = cacheConnection.prepareStatement("select " + COLUMN_VALUE + " from " + TABLE_STATE + " where " + COLUMN_NAME + "='?'");
//			stmt.setString(1, key);
//			ResultSet rs = stmt.executeQuery();
//	
//			// We only care about the first entry. There should only be one.
//			rs.next();
//			return rs.getString(COLUMN_VALUE);
//		} catch (SQLException e) {
//	        throw new DriverObjectCacheException("Error reading state", e);
//		} finally {
//			try {
//				stmt.close();
//			} catch (SQLException e) {
//		        throw new DriverObjectCacheException("Error closing statement", e);
//			}
//		}
//	}

//	private void setState(String key, String value) throws DriverObjectCacheException {
//		try {
//			PreparedStatement insert = cacheConnection.prepareStatement("insert into " + TABLE_STATE + " (" + COLUMN_NAME + ", " + COLUMN_VALUE + ") values(?,?)");
//			insert.setString(1, key);
//			insert.setString(2, value);
//			insert.executeUpdate();
//		} catch (SQLException e) {
//	        throw new DriverObjectCacheException("Error setting cache state.", e);
//		}
//	}

//	private List<String> getStateList(String key) throws DriverObjectCacheException {
//		String value = getState(key);
//		if (value == null) {
//			return null;
//		}
//		return new Gson().fromJson(value, new TypeToken<List<String>>(){}.getType());
//	}
	
//	private void setStateList(String key, List<String> value) throws DriverObjectCacheException {
//		Gson gson = new Gson();
//		setState(key, gson.toJson(value));
//	}

	private static void close(Statement st) {
		if (st != null) {
			try {
				st.close();
			} catch (SQLException e) {
				// Ignore, there isn't anything to be done about this
			}
		}
	}
}