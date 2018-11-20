package cld.kcloud.custom.view;

public class Car {
	
	private String brand;
	private String modle;
	private String series;
	
	public Car(String brand, String modle, String series) {
		this.brand = brand;
		this.modle = modle;
		this.series = series;
	}

	public void setBrand(String brand) {
		this.brand = brand;
	}
	
	public String getBrand() {
		return this.brand;
	}
	
	public void setModle(String modle) {
		this.modle = modle;
	}
	
	public String getModle() {
		return this.modle;
	}
	
	public void setSeries(String series) {
		this.series = series;
	}
	
	public String getSeries() {
		return this.series;
	}
}
