package Phase3;

import java.util.ArrayList;

public class Word {

	public ArrayList<Double> content;
	
	public Word(String[] window, int startIndex){
		content = new ArrayList<Double>();
		for(int i =startIndex; i<window.length;i++) {
			content.add(Double.parseDouble(window[i]));
		}
	}
	
	//override hashcode method 
	public int hashCode() {
		return content.hashCode();
	}
	//override equals method to compare word content
	public boolean equals(Object o) {
		if(o != null && o instanceof Word) {
			Word w = (Word)o;
			return this.equals(w);
		}
		return false;
	}
	
	public boolean equals(Word w) {
		ArrayList<Double> temp = w.content;
		if(content.size() != w.content.size())
			return false;
		for(int i = 0; i < content.size(); i++) {
			Double d1 = content.get(i);
			Double d2 = temp.get(i);
			if(!d1.equals(d2))  {
				return false;
			}
		}
		return true;
	}
}
