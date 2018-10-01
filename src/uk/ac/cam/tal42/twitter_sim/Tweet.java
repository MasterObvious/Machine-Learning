package uk.ac.cam.tal42.twitter_sim;

public class Tweet {
	
	private String source;
	private String id_str;
	private String text;
	private String created_at;
	private int retweet_count;
	private String in_reply_to_user_id_str;
	private int favourite_count;
	private boolean is_retweet;
	public String getSource() {
		return source;
	}
	public String getId_str() {
		return id_str;
	}
	public String getText() {
		return text;
	}
	public String getCreated_at() {
		return created_at;
	}
	public int getRetweet_count() {
		return retweet_count;
	}
	public String getIn_reply_to_user_id_str() {
		return in_reply_to_user_id_str;
	}
	public int getFavourite_count() {
		return favourite_count;
	}
	public boolean isRetweet() {
		return is_retweet;
	}
	
	

}
