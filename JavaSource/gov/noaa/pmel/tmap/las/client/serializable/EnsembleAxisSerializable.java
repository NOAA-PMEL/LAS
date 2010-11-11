package gov.noaa.pmel.tmap.las.client.serializable;

import com.google.gwt.user.client.rpc.IsSerializable;

public class EnsembleAxisSerializable extends AxisSerializable implements IsSerializable {

	EnsembleMemberSerializable[] members;

	public EnsembleAxisSerializable() {
		
	}
	
	public String getVariableID(String dsid, String name) {
		for (int i = 0; i < members.length; i++) {
			String member_id = members[i].getID();
			if (dsid.equals(member_id)) {
				EnsembleVariableSerializable[] variables = members[i].getMemberVariables();
				for (int j = 0; j < variables.length; j++) {
					if ( variables[j].getName().equals(name)) {
						return variables[j].getID();
					}
				}
			}
		}
		return "";
	}
	
	public EnsembleMemberSerializable[] getMembers() {
		return members;
	}

	public void setMembers(EnsembleMemberSerializable[] members) {
		this.members = members;
	}

}
