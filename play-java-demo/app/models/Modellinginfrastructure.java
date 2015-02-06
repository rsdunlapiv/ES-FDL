package models;

import java.util.ArrayList;

public class Modellinginfrastructure {
	public String name;

	public ArrayList<String> getModellingInfranames() {
		ArrayList<String> temp = new ArrayList<String>();
		temp.add("ESMF");
		temp.add("CSDMS");
		temp.add("OASIS");
		temp.add("MCT");
		return temp;
	}
}