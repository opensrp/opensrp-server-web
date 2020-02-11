/**
 * 
 */
package org.opensrp.connector.dhis2;

import java.util.HashMap;
import java.util.Map;

/**
 * @author proshanto
 */
public class DHIS2Settings {
	
	static Map<String, String> CHILDMAPPING = new HashMap<String, String>();
	static {
		CHILDMAPPING.put("firstName", "pzuh7zrs9Xx");
		CHILDMAPPING.put("lastName", "VDWBOoLHJ8S");
		CHILDMAPPING.put("gender", "xDvyz0ezL4e");
		CHILDMAPPING.put("Child_Birth_Certificate", "ZDWzVhjlgWK");
		CHILDMAPPING.put("birthdate", "vzleM5DCHs0");
		CHILDMAPPING.put("mother_guardian_name", "ra9rJm4IoD0");
		CHILDMAPPING.put("father_guardian_full_name", "cnbMOmsHTUe");
		CHILDMAPPING.put("mother_guardian_phone_number", "zETIZAoo968");
		CHILDMAPPING.put("which_health_facility_was_the_child_born_in", "bo2ffZZzA3h");
		CHILDMAPPING.put("place_birth", "grbAqXDsYoD");
		CHILDMAPPING.put("child_health_facility_name", "r4yR1YLNR7C");
		CHILDMAPPING.put("base_entity_id", "s67McYqu0lP");
		CHILDMAPPING.put("birth_weight", "mpRaccA9dB9");
		CHILDMAPPING.put("birth_weight", "mpRaccA9dB9");
		
	}
	
	static Map<String, String> MOTHERIDMAPPING = new HashMap<String, String>();
	static {
		MOTHERIDMAPPING.put("firstName", "pzuh7zrs9Xx");
		MOTHERIDMAPPING.put("lastName", "VDWBOoLHJ8S");
		MOTHERIDMAPPING.put("registration_Date", "bWoFS5a23fo");
		MOTHERIDMAPPING.put("birthdate", "vzleM5DCHs0");
		MOTHERIDMAPPING.put("phone_Number", "wCom53wUTKf");
		MOTHERIDMAPPING.put("base_entity_id", "s67McYqu0lP");
		MOTHERIDMAPPING.put("NID", "gXDTMMEoPTO");
		MOTHERIDMAPPING.put("BRID", "IFotjKs2Hkw");
		MOTHERIDMAPPING.put("id_type", "NCAsqa38X5z");
		MOTHERIDMAPPING.put("houband_Name", "MGeiR6Vv1ma");
		MOTHERIDMAPPING.put("Mother_Guardian_DOB", "UhNq433oy7S");
		MOTHERIDMAPPING.put("Member_Registration_No", "TqA0pnQLHKt");
		MOTHERIDMAPPING.put("EPI_Card_Number", "AcfzB53w2JK");
		MOTHERIDMAPPING.put("Maritial_Status", "NkOtPre4iTm");
		MOTHERIDMAPPING.put("Couple_No", "DftawD2qqQJ");
		MOTHERIDMAPPING.put("pregnant", "HaFQAe7H67k");
		MOTHERIDMAPPING.put("FP_User", "ip2EZBFuVLk");
		MOTHERIDMAPPING.put("know_lmp", "ipQwd1NbKne");
		MOTHERIDMAPPING.put("know_edd_ultra", "rWW7UXlpsji");
		MOTHERIDMAPPING.put("EDD", "vA1g2eVeu1V");
		MOTHERIDMAPPING.put("ultrasound", "Bo40TwobZU7");
		MOTHERIDMAPPING.put("FP_Methods", "eg9vNyPsUlI");
		MOTHERIDMAPPING.put("LMP", "OULPvo7wDIB");
		MOTHERIDMAPPING.put("ultrasound_week", "KOG3eLApsYc");
	}
	
	static Map<String, String> HOUSEHOLDIDMAPPING = new HashMap<String, String>();
	static {
		HOUSEHOLDIDMAPPING.put("firstName", "pzuh7zrs9Xx");
		HOUSEHOLDIDMAPPING.put("lastName", "VDWBOoLHJ8S");
		HOUSEHOLDIDMAPPING.put("gender", "xDvyz0ezL4e");
		HOUSEHOLDIDMAPPING.put("Household_ID", "FUr13UGc7aC");
		HOUSEHOLDIDMAPPING.put("birthdate", "vzleM5DCHs0");
		HOUSEHOLDIDMAPPING.put("registration_Date", "bWoFS5a23fo");
		HOUSEHOLDIDMAPPING.put("phone_number", "wCom53wUTKf");
		HOUSEHOLDIDMAPPING.put("division", "Ho6ldK3TYSj");
		HOUSEHOLDIDMAPPING.put("district", "kNcYpo9UAGv");
		HOUSEHOLDIDMAPPING.put("upazilla", "AX3Xqhg80lo");
		HOUSEHOLDIDMAPPING.put("union", "y2jRcMEFybQ");
		HOUSEHOLDIDMAPPING.put("ward", "D6h2d4IBn0W");
		HOUSEHOLDIDMAPPING.put("sub_unit", "Midt8csUAUM");
		HOUSEHOLDIDMAPPING.put("vaccination_center", "rU1tZFJnEOh");
		HOUSEHOLDIDMAPPING.put("base_entity_id", "s67McYqu0lP");
	}
	
	static Map<String, String> VACCINATIONMAPPING = new HashMap<String, String>();
	static {
		VACCINATIONMAPPING.put("firstName", "pzuh7zrs9Xx");
		VACCINATIONMAPPING.put("lastName", "VDWBOoLHJ8S");
		VACCINATIONMAPPING.put("Mother_guardian_First_Name", "ra9rJm4IoD0");
		VACCINATIONMAPPING.put("Mother_Guardian_DOB", "UhNq433oy7S");
		VACCINATIONMAPPING.put("NID_BRID", "gXDTMMEoPTO");
		VACCINATIONMAPPING.put("Child_Birth_Certificate", "ZDWzVhjlgWK");
		VACCINATIONMAPPING.put("Vaccina_date", "FrU0Gy7JgDj");
		VACCINATIONMAPPING.put("Vaccina_dose", "angDB9as1Mm");
		VACCINATIONMAPPING.put("Vaccina_name", "xpcgeevTaHG");
		VACCINATIONMAPPING.put("child_id", "OWr9r1SlkZd");
		VACCINATIONMAPPING.put("base_entity_id", "s67McYqu0lP");
	}
	
	static Map<String, String> COMMONMAPPING = new HashMap<String, String>();
	static {
		COMMONMAPPING.put("client_type", "ps3fmHS873u");
		
	}
	
	static Map<String, Integer> VACCINATION = new HashMap<String, Integer>();
	static {
		VACCINATION.put("opv_0", 0);
		VACCINATION.put("opv_1", 1);
		VACCINATION.put("opv_2", 2);
		VACCINATION.put("opv_3", 3);
		VACCINATION.put("opv_4", 4);
		
		VACCINATION.put("bcg", 1);
		
		VACCINATION.put("penta_1", 1);
		VACCINATION.put("penta_2", 2);
		VACCINATION.put("penta_3", 3);
		
		VACCINATION.put("pcv_1", 1);
		VACCINATION.put("pcv_2", 2);
		VACCINATION.put("pcv_3", 3);
		
		VACCINATION.put("measles_1", 1);
		VACCINATION.put("measles_2", 2);
		
		VACCINATION.put("tt_1", 1);
		VACCINATION.put("tt_2", 2);
		VACCINATION.put("tt_3", 3);
		VACCINATION.put("tt_4", 4);
		VACCINATION.put("tt_5", 5);
		
	}
}
