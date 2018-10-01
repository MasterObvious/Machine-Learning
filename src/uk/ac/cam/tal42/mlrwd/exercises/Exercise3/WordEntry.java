package uk.ac.cam.tal42.mlrwd.exercises.Exercise3;

public class WordEntry implements Comparable<WordEntry>{
	
	private String mWord;
	private int mCount;
	private int mRank = 0;
	
	
	
	
	@Override
	public String toString() {
		String message =  "The count of '" + mWord + "' is " + mCount + " and its rank is " + mRank; 
		return message;
	}
	
	
	public WordEntry(String word, Integer count) {
		mWord = word;
		mCount = count;
	}
	
	public void setRank(int rank) {
		mRank = rank;
	}
	
	public int getRank() {
		return mRank;
	}
	
	public int getCount() {
		return mCount;
	}
	
	public String getWord() {
		return mWord;
	}


	@Override
	public int compareTo(WordEntry arg0) {
		// TODO Auto-generated method stub
		WordEntry other = arg0;
		int difference = other.getCount() - this.mCount;
		return difference;
	}
	
	

}
