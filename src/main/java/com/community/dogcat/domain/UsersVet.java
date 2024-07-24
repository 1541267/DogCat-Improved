package com.community.dogcat.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.Size;

import lombok.Getter;

@Getter
@Entity
@Table(name = "users_vet", schema = "dogcat")
public class UsersVet {
	
	@Size(max = 50)
	@Column(name = "vet_name", nullable = false, length = 50)
	private String vetName;

	@Id
	@Column(name = "vet_license", nullable = false)
	private Long vetLicense;

}
