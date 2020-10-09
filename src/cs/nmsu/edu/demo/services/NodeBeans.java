package cs.nmsu.edu.demo.services;

public class NodeBeans {
	private long id;
	private double lat,lng;
	
	public NodeBeans() {};
	
	public NodeBeans(long id, double lat, double lng) {
		this.id = id;
		this.lat = lat;
		this.lng = lng;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public double getLat() {
		return lat;
	}

	public void setLat(double lat) {
		this.lat = lat;
	}

	public double getLng() {
		return lng;
	}

	public void setLng(double lng) {
		this.lng = lng;
	}
	

}
