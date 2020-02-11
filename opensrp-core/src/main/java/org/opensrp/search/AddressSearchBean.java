package org.opensrp.search;

import java.util.List;

import org.apache.commons.lang.StringUtils;

public class AddressSearchBean {
	
	private String addressType;
	
	private String country;
	
	private String stateProvince;
	
	private String cityVillage;
	
	private String countyDistrict;
	
	private String subDistrict;
	
	private String town;
	
	private String subTown;
	
	private List<String> address1;

	private List<String> address2;

	private List<String> address3;

	private List<Long> villageId;
	
	public String getAddressType() {
		return addressType;
	}
	
	public void setAddressType(String addressType) {
		this.addressType = addressType;
	}
	
	public String getCountry() {
		return country;
	}
	
	public void setCountry(String country) {
		this.country = country;
	}
	
	public String getStateProvince() {
		return stateProvince;
	}
	
	public void setStateProvince(String stateProvince) {
		this.stateProvince = stateProvince;
	}
	
	public String getCityVillage() {
		return cityVillage;
	}
	
	public void setCityVillage(String cityVillage) {
		this.cityVillage = cityVillage;
	}
	
	public String getCountyDistrict() {
		return countyDistrict;
	}
	
	public void setCountyDistrict(String countyDistrict) {
		this.countyDistrict = countyDistrict;
	}
	
	public String getSubDistrict() {
		return subDistrict;
	}
	
	public void setSubDistrict(String subDistrict) {
		this.subDistrict = subDistrict;
	}
	
	public String getTown() {
		return town;
	}
	
	public void setTown(String town) {
		this.town = town;
	}
	
	public String getSubTown() {
		return subTown;
	}
	
	public void setSubTown(String subTown) {
		this.subTown = subTown;
	}

	public List<String> getAddress1() {
		return address1;
	}

	public void setAddress1(List<String> address1) {
		this.address1 = address1;
	}

	public List<String> getAddress2() {
		return address2;
	}
	
	public void setAddress2(List<String> address2) {
		this.address2 = address2;
	}
	
	public List<String> getAddress3() {
		return address3;
	}
	
	public void setAddress3(List<String> address3) {
		this.address3 = address3;
	}

	public List<Long> getVillageId() {
		return villageId;
	}

	public void setVillageId(List<Long> villageId) {
		this.villageId = villageId;
	}

	public boolean isHasFilter() {
		return StringUtils.isNotEmpty(addressType) || StringUtils.isNotEmpty(country)
		        || StringUtils.isNotEmpty(stateProvince) || StringUtils.isNotEmpty(cityVillage)
		        || StringUtils.isNotEmpty(countyDistrict) || StringUtils.isNotEmpty(town) || StringUtils.isNotEmpty(subTown)
		        || !address2.isEmpty() || !address3.isEmpty();
	}
}
