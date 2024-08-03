package com.community.dogcat.repository.user;

import org.springframework.data.jpa.repository.JpaRepository;

import com.community.dogcat.domain.UsersVet;

public interface VetRepository extends JpaRepository<UsersVet, Long> {

	UsersVet findByVetNameAndVetLicense(String vetName, Long vetLicense);

}
