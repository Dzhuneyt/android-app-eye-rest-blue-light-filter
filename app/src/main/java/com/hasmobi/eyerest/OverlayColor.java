package com.hasmobi.eyerest;

public class OverlayColor {

	public int color;
	public String name;

	public OverlayColor(String name, int color){
		this.name = name;
		this.color = color;
	}

	@Override
	public String toString() {
		return name;
	}
}
