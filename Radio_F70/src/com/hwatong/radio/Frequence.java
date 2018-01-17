package com.hwatong.radio;

import java.io.Serializable;
import java.text.DecimalFormat;

public class Frequence implements Serializable, Comparable<Frequence> {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public int frequence;
	public boolean isCollected = false;
	public int id;

	public Frequence() {
	}
	
	public Frequence(int frequence) {
		this.frequence = frequence;
	}

	public Frequence(int frequence, boolean isCollected) {
		this.frequence = frequence;
		this.isCollected = isCollected;
	}

	public Frequence(int frequence, boolean isCollected, int id) {
		this.frequence = frequence;
		this.isCollected = isCollected;
		this.id = id;
	}

	public String getString() {
		double f = frequence / 100.0;
		DecimalFormat df = new DecimalFormat("#.0");
		return df.format(f);
	}

	@Override
	public int compareTo(Frequence another) {
		return frequence - another.frequence;
	}
	 
	@Override
	public boolean equals(Object o) {
		if(o instanceof Frequence) {
			return frequence == ((Frequence)o).frequence;
		}
		return false;
	}
	

	@Override
	public String toString() {
		return frequence + "#" + isCollected;
	}

}
