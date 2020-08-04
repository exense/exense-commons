package ch.exense.commons.core.mongo.accessors.generic;

import java.util.List;

public class MongoResult {

	private List<ObjectWrapper> data;
	private int count;
	public List<ObjectWrapper> getData() {
		return data;
	}
	public void setData(List<ObjectWrapper> data) {
		this.data = data;
	}
	public int getCount() {
		return count;
	}
	public void setCount(int count) {
		this.count = count;
	}
}
