package com.community.dogcat.service.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.community.dogcat.domain.UsersVet;
import com.community.dogcat.repository.user.VetRepository;

@Service
public class VetService {

	@Autowired
	private VetRepository vetRepository;

	public UsersVet findByVetNameAndVetLicense(String vetName, Long vetLicense) {
		return vetRepository.findByVetNameAndVetLicense(vetName, vetLicense);
	}

	public UsersVet save(UsersVet vet) {
		return vetRepository.save(vet);
	}
}
