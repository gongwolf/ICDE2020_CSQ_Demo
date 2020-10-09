package cs.nmsu.edu.demo.utilities;

public class POIObject {
	// 0,37.7749009,-122.4375083,0.6,3.0,6.0,ChIJ_dQjyK-AhYARBc9DFlxcclg,Nopa

	int placeID; // the place id that is used in this project
	double[] locations; // the latitude and the longitude of the POIs
	float[] data; // the attributes of the POI

	String g_p_id; // google map place id
	public String g_p_name;// google map showed name

	public POIObject() {
		this.locations = new double[2];
		this.data = new float[3];
	}

	public POIObject(POIObject iop_obj) {
		this.locations = new double[2];
		this.data = new float[3];

		this.placeID = iop_obj.placeID;
		this.g_p_id = iop_obj.g_p_id;
		this.g_p_name = iop_obj.g_p_name;

		System.arraycopy(iop_obj.locations, 0, this.locations, 0, iop_obj.locations.length);
		System.arraycopy(iop_obj.data, 0, this.data, 0, iop_obj.data.length);

	}

	public void cleanContents() {
		this.g_p_id = "";
		this.data = new float[] { -1, -1, -1 };
		this.locations = new double[] { -1, -1 };
		this.g_p_name = "";
	}

	@Override
	public String toString() {
		return this.placeID + " " + this.g_p_id + " " + this.data[0] + " " + this.data[1] + " " + this.data[2] + " "
				+ this.locations[0] + " " + this.locations[1];
	}

	public int getPlaceID() {
		return placeID;
	}

	public void setPlaceID(int placeID) {
		this.placeID = placeID;
	}

	public double[] getLocations() {
		return locations;
	}

	public void setLocations(double[] locations) {
		System.arraycopy(locations, 0, this.locations, 0, locations.length);
	}

	public float[] getData() {
		return data;
	}

	public void setData(float[] data) {
		System.arraycopy(data, 0, this.data, 0, data.length);
	}

	public String getG_p_id() {
		return g_p_id;
	}

	public void setG_p_id(String g_p_id) {
		this.g_p_id = g_p_id;
	}

	public String getG_p_name() {
		return g_p_name;
	}

	public void setG_p_name(String g_p_name) {
		this.g_p_name = g_p_name;
	}
}
