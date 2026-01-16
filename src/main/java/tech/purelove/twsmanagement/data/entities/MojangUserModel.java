package tech.purelove.twsmanagement.data.entities;

import java.util.UUID;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import tech.purelove.twsmanagement.extensions.MojangUserModelDeserializer;

@JsonDeserialize(using = MojangUserModelDeserializer.class)
public class MojangUserModel {
	
	private UUID id;
	private String name;
	
	public UUID getId() {
		return this.id;
	}
	
	public String getName() {
		return this.name;
	}
	
	public void setId(UUID id) {
		this.id = id;
	}
	
	public void setName(String name) {
		this.name = name;
	}
}
